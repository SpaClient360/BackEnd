package com.htttql.crmmodule.crmrequest.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.htttql.crmmodule.common.web.GlobalExceptionHandler;
import com.htttql.crmmodule.crmrequest.dto.CustomerRequestCreateRequest;
import com.htttql.crmmodule.crmrequest.dto.CustomerRequestResponse;
import com.htttql.crmmodule.crmrequest.service.CustomerRequestService;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = CustomerRequestController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class CustomerRequestControllerValidationTest {

        @Autowired
        private MockMvc mockMvc;
        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private CustomerRequestService service;

        @Test
        void create_shouldReturn400_whenNameMissing() throws Exception {
                Map<String, Object> body = new HashMap<>();
                body.put("email", "valid@mail.com");

                mockMvc
                                .perform(
                                                post("/api/customer-requests")
                                                                .contentType(MediaType.APPLICATION_JSON)
                                                                .header("X-Forwarded-For", "1.2.3.4")
                                                                .content(objectMapper.writeValueAsString(body)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.errors.name").exists());
        }

        @Test
        void create_shouldReturn201_whenValid() throws Exception {
                Map<String, Object> body = new HashMap<>();
                body.put("name", "Khach Hang");
                body.put("phoneNumber", "0900000000");
                body.put("source", "landing");

                when(service.create(eq("1.2.3.4"), any(CustomerRequestCreateRequest.class)))
                                .thenReturn(
                                                CustomerRequestResponse.builder()
                                                                .id(UUID.randomUUID())
                                                                .name("Khach Hang")
                                                                .phoneNumber("0900000000")
                                                                .build());

                mockMvc
                                .perform(
                                                post("/api/customer-requests")
                                                                .contentType(MediaType.APPLICATION_JSON)
                                                                .header("X-Forwarded-For", "1.2.3.4")
                                                                .content(objectMapper.writeValueAsString(body)))
                                .andExpect(status().isCreated());

                verify(service).create(eq("1.2.3.4"), any(CustomerRequestCreateRequest.class));
        }
}
