package com.htttql.crmmodule.billing.repository;

import com.htttql.crmmodule.billing.entity.Payment;
import com.htttql.crmmodule.common.enums.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Payment entity
 */
@Repository
public interface IPaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Find payments by invoice ID
     */
    List<Payment> findByInvoice_InvoiceId(Long invoiceId);

    /**
     * Find payments by method
     */
    List<Payment> findByMethod(PaymentMethod method);

    /**
     * Find payments by paid by staff user
     */
    List<Payment> findByPaidBy_StaffId(Long staffId);
}
