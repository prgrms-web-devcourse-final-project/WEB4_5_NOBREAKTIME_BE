package com.mallang.mallang_backend.domain.payment.repository;

import com.mallang.mallang_backend.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
