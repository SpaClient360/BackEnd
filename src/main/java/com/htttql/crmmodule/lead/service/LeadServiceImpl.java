package com.htttql.crmmodule.lead.service;

import com.htttql.crmmodule.common.enums.LeadStatus;
import com.htttql.crmmodule.common.exception.BadRequestException;
import com.htttql.crmmodule.common.exception.ResourceNotFoundException;
import com.htttql.crmmodule.core.entity.Customer;
import com.htttql.crmmodule.core.repository.ICustomerRepository;
import com.htttql.crmmodule.lead.dto.LeadRequest;
import com.htttql.crmmodule.lead.dto.LeadResponse;
import com.htttql.crmmodule.lead.dto.LeadStatusRequest;
import com.htttql.crmmodule.lead.dto.LeadStats;
import com.htttql.crmmodule.lead.entity.Lead;
import com.htttql.crmmodule.lead.repository.ILeadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service("leadService")
@RequiredArgsConstructor
public class LeadServiceImpl implements ILeadService {

    private final ILeadRepository leadRepository;
    private final ICustomerRepository customerRepository;
    private final ModelMapper modelMapper;
    private final RedisTemplate<String, String> redisTemplate;

    // Tiền tố cho Redis key
    private static final String LEAD_CACHE_PREFIX = "LEAD:";
    private static final String LEAD_RATE_LIMIT_PREFIX = "LEAD_RATE_LIMIT:";
    private static final String LEAD_TEMP_PREFIX = "LEAD_TEMP:";
    private static final String LEAD_STATS_PREFIX = "LEAD_STATS:";

    // Hằng số giới hạn tốc độ
    private static final int MAX_LEADS_PER_HOUR = 10;
    private static final int MAX_LEADS_PER_DAY = 100;
    private static final int CACHE_TTL_HOURS = 24;
    private static final int TEMP_STORAGE_TTL_HOURS = 48;

    @Override
    @Transactional(readOnly = true)
    public Page<LeadResponse> getAllLeads(Pageable pageable) {
        String cacheKey = LEAD_CACHE_PREFIX + "ALL:" + pageable.getPageNumber() + ":" + pageable.getPageSize();
        String cachedData = redisTemplate.opsForValue().get(cacheKey);

        if (cachedData != null) {
            log.debug("Retrieved leads from cache: {}", cacheKey);
        }

        Page<Lead> leads = leadRepository.findAll(pageable);
        Page<LeadResponse> response = leads.map(this::toResponse);

        try {
            redisTemplate.opsForValue().set(cacheKey, "cached", Duration.ofHours(CACHE_TTL_HOURS));
            log.debug("Cached leads data: {}", cacheKey);
        } catch (Exception e) {
            log.warn("Failed to cache leads data: {}", e.getMessage());
        }

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public LeadResponse getLeadById(Long id) {
        String cacheKey = LEAD_CACHE_PREFIX + id;
        String cachedData = redisTemplate.opsForValue().get(cacheKey);

        if (cachedData != null) {
            log.debug("Retrieved lead from cache: {}", cacheKey);
        }

        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead", "id", id));

        LeadResponse response = toResponse(lead);

        try {
            redisTemplate.opsForValue().set(cacheKey, "cached", Duration.ofHours(CACHE_TTL_HOURS));
            log.debug("Cached lead data: {}", cacheKey);
        } catch (Exception e) {
            log.warn("Failed to cache lead data: {}", e.getMessage());
        }

        return response;
    }

    @Override
    @Transactional
    public LeadResponse createLead(LeadRequest request) {
        String ipAddress = getClientIpAddress();
        String userAgent = getUserAgent();

        if (!checkRateLimit(ipAddress)) {
            throw new BadRequestException("Rate limit exceeded. Please try again later.");
        }

        String tempKey = LEAD_TEMP_PREFIX + System.currentTimeMillis() + ":" + ipAddress;
        try {
            redisTemplate.opsForValue().set(tempKey, request.getPhone(), Duration.ofHours(TEMP_STORAGE_TTL_HOURS));
        } catch (Exception e) {
        }

        Customer existingCustomer = customerRepository.findByPhone(request.getPhone()).orElse(null);

        Lead lead = Lead.builder()
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .note(request.getNote())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();
        if (existingCustomer != null) {
            lead.setCustomerId(existingCustomer.getCustomerId());
            lead.setIsExistingCustomer(true);

        }
        lead = leadRepository.save(lead);

        LeadResponse response = toResponse(lead);

        updateLeadStats();

        clearLeadCache(lead.getLeadId());

        return response;
    }

    @Override
    @Transactional
    public LeadResponse updateLead(Long id, LeadRequest request) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead", "id", id));

        lead.setFullName(request.getFullName());
        lead.setPhone(request.getPhone());
        lead.setNote(request.getNote());

        lead = leadRepository.save(lead);

        // Xóa cache cho lead này
        clearLeadCache(id);

        log.info("Updated lead: {}", lead.getLeadId());
        return toResponse(lead);
    }

    @Override
    @Transactional
    public void deleteLead(Long id) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead", "id", id));

        if (lead.getStatus() != LeadStatus.NEW) {
            throw new BadRequestException("Can only delete leads with NEW status");
        }

        leadRepository.deleteById(id);

        // Xóa cache cho lead này
        clearLeadCache(id);

        log.info("Deleted lead: {}", id);
    }

    @Override
    @Transactional
    public LeadResponse updateLeadStatus(Long id, LeadStatusRequest request) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead", "id", id));

        validateStatusTransition(lead.getStatus(), request.getStatus());
        lead.setStatus(request.getStatus());

        if (request.getNote() != null) {
            String existingNote = lead.getNote() != null ? lead.getNote() + "\n" : "";
            lead.setNote(existingNote + "[Status Update] " + request.getNote());
        }

        lead = leadRepository.save(lead);

        // Xóa cache cho lead này
        clearLeadCache(id);

        log.info("Updated lead status: {} to {}", lead.getLeadId(), request.getStatus());
        return toResponse(lead);
    }

    private void validateStatusTransition(LeadStatus currentStatus, LeadStatus newStatus) {
        boolean isValid = switch (currentStatus) {
            case NEW -> newStatus == LeadStatus.IN_PROGRESS || newStatus == LeadStatus.LOST;
            case IN_PROGRESS -> newStatus == LeadStatus.WON || newStatus == LeadStatus.LOST;
            case WON, LOST -> false;
        };

        if (!isValid) {
            throw new BadRequestException(
                    String.format("Invalid status transition from %s to %s", currentStatus, newStatus));
        }
    }

    private LeadResponse toResponse(Lead lead) {
        LeadResponse response = modelMapper.map(lead, LeadResponse.class);
        return response;
    }

    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.warn("Could not get client IP address", e);
        }
        return "unknown";
    }

    private String getUserAgent() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return request.getHeader("User-Agent");
            }
        } catch (Exception e) {
            log.warn("Could not get User-Agent", e);
        }
        return "unknown";
    }

    /**
     * Kiểm tra giới hạn tốc độ cho địa chỉ IP sử dụng Redis
     */
    private boolean checkRateLimit(String ipAddress) {
        String hourlyKey = LEAD_RATE_LIMIT_PREFIX + "HOURLY:" + ipAddress;
        String dailyKey = LEAD_RATE_LIMIT_PREFIX + "DAILY:" + ipAddress;

        try {
            // Kiểm tra giới hạn theo giờ
            String hourlyCount = redisTemplate.opsForValue().get(hourlyKey);
            int currentHourlyCount = hourlyCount != null ? Integer.parseInt(hourlyCount) : 0;

            if (currentHourlyCount >= MAX_LEADS_PER_HOUR) {
                log.warn("Hourly rate limit exceeded for IP: {}", ipAddress);
                return false;
            }

            // Kiểm tra giới hạn theo ngày
            String dailyCount = redisTemplate.opsForValue().get(dailyKey);
            int currentDailyCount = dailyCount != null ? Integer.parseInt(dailyCount) : 0;

            if (currentDailyCount >= MAX_LEADS_PER_DAY) {
                log.warn("Daily rate limit exceeded for IP: {}", ipAddress);
                return false;
            }

            // Tăng bộ đếm
            redisTemplate.opsForValue().increment(hourlyKey);
            redisTemplate.opsForValue().increment(dailyKey);

            // Đặt thời gian hết hạn cho bộ đếm theo giờ (1 giờ)
            redisTemplate.expire(hourlyKey, 1, TimeUnit.HOURS);

            // Đặt thời gian hết hạn cho bộ đếm theo ngày (24 giờ)
            redisTemplate.expire(dailyKey, 24, TimeUnit.HOURS);

            return true;
        } catch (Exception e) {
            log.warn("Rate limiting check failed: {}", e.getMessage());
            // Nếu Redis thất bại, cho phép request tiếp tục
            return true;
        }
    }

    /**
     * Cập nhật thống kê lead trong Redis
     */
    private void updateLeadStats() {
        String todayKey = LEAD_STATS_PREFIX + "TODAY:" + java.time.LocalDate.now();
        String totalKey = LEAD_STATS_PREFIX + "TOTAL";

        try {
            // Tăng bộ đếm hôm nay
            redisTemplate.opsForValue().increment(todayKey);
            redisTemplate.expire(todayKey, 48, TimeUnit.HOURS); // Giữ trong 2 ngày

            // Tăng bộ đếm tổng
            redisTemplate.opsForValue().increment(totalKey);

            log.debug("Updated lead statistics in Redis");
        } catch (Exception e) {
            log.warn("Failed to update lead statistics: {}", e.getMessage());
        }
    }

    /**
     * Xóa cache cho một lead cụ thể
     */
    private void clearLeadCache(Long leadId) {
        try {
            String cacheKey = LEAD_CACHE_PREFIX + leadId;
            redisTemplate.delete(cacheKey);
            log.debug("Cleared cache for lead: {}", leadId);
        } catch (Exception e) {
            log.warn("Failed to clear cache for lead {}: {}", leadId, e.getMessage());
        }
    }

    /**
     * Lấy thống kê lead từ Redis
     */
    public LeadStats getLeadStats() {
        try {
            String todayKey = LEAD_STATS_PREFIX + "TODAY:" + java.time.LocalDate.now();
            String totalKey = LEAD_STATS_PREFIX + "TOTAL";

            String todayCount = redisTemplate.opsForValue().get(todayKey);
            String totalCount = redisTemplate.opsForValue().get(totalKey);
            return LeadStats.builder()
                    .todayCount(todayCount != null ? Integer.parseInt(todayCount) : 0)
                    .totalCount(totalCount != null ? Integer.parseInt(totalCount) : 0)
                    .lastUpdated(java.time.LocalDateTime.now().toString())
                    .build();
        } catch (Exception e) {
            log.warn("Failed to get lead statistics: {}", e.getMessage());
            return LeadStats.builder()
                    .todayCount(0)
                    .totalCount(0)
                    .lastUpdated(java.time.LocalDateTime.now().toString())
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<LeadResponse> getLeadsByStatus(LeadStatus status) {
        String cacheKey = LEAD_CACHE_PREFIX + "STATUS:" + status.name();
        String cachedData = redisTemplate.opsForValue().get(cacheKey);

        if (cachedData != null) {
            log.debug("Retrieved leads by status from cache: {}", cacheKey);
        }

        java.util.List<Lead> leads = leadRepository.findByStatus(status);
        java.util.List<LeadResponse> response = leads.stream()
                .map(this::toResponse)
                .collect(java.util.stream.Collectors.toList());

        try {
            redisTemplate.opsForValue().set(cacheKey, "cached", Duration.ofHours(CACHE_TTL_HOURS));
            log.debug("Cached leads by status data: {}", cacheKey);
        } catch (Exception e) {
            log.warn("Failed to cache leads by status data: {}", e.getMessage());
        }

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeadResponse> getLeadsByStatus(LeadStatus status, Pageable pageable) {
        String cacheKey = LEAD_CACHE_PREFIX + "STATUS:" + status.name() + ":" + pageable.getPageNumber() + ":"
                + pageable.getPageSize();
        String cachedData = redisTemplate.opsForValue().get(cacheKey);

        if (cachedData != null) {
            log.debug("Retrieved leads by status with pagination from cache: {}", cacheKey);
        }

        Page<Lead> leads = leadRepository.findByStatusWithPagination(status, pageable);
        Page<LeadResponse> response = leads.map(this::toResponse);

        try {
            redisTemplate.opsForValue().set(cacheKey, "cached", Duration.ofHours(CACHE_TTL_HOURS));
            log.debug("Cached leads by status with pagination data: {}", cacheKey);
        } catch (Exception e) {
            log.warn("Failed to cache leads by status with pagination data: {}", e.getMessage());
        }

        return response;
    }
}
