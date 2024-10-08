package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Payment;
import com.example.demo.enums.PaymentMethod;
import com.example.demo.enums.PaymentStatus;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
	@Query("FROM Payment p WHERE p.user.userId = :userId")
	List<Payment> findAllByUserId(int userId);
	List<Payment> findAllByPaymentStatus(PaymentStatus paymentStatus);
	List<Payment> findAllByPaymentMethod(PaymentMethod paymentMethod);
}
