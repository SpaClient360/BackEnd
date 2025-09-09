package com.htttql.crmmodule.billing.repository;

import com.htttql.crmmodule.billing.entity.Payment;
import com.htttql.crmmodule.common.enums.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IPaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByInvoice_InvoiceId(Long invoiceId);

    List<Payment> findByMethod(PaymentMethod method);

    List<Payment> findByPaidBy_StaffId(Long staffId);
}
