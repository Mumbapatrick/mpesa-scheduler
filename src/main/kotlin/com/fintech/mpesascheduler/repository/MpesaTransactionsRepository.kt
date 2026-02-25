package com.fintech.mpesascheduler.repository

import com.fintech.mpesascheduler.entity.*
import com.fintech.mpesascheduler.enums.PaymentStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

// -----------------------------
// USER & ROLE REPOSITORIES
// -----------------------------

@Repository
interface UserRepository : JpaRepository<UserAccount, Long> {
    fun findByEmail(email: String): UserAccount?
}

@Repository
interface RoleRepository : JpaRepository<Role, Long> {
    fun findByName(name: String): Role?
}

// -----------------------------
// TRANSACTION REPOSITORIES
// -----------------------------

@Repository
interface MpesaTransactionRepository : JpaRepository<MpesaTransaction, Long> {
    fun findByStatus(status: String): List<MpesaTransaction>
    fun findByUserId(userId: Long): List<MpesaTransaction>
}

@Repository
interface BatchTransactionRepository : JpaRepository<BatchTransaction, Long> {
    fun findByUserId(userId: Long): List<BatchTransaction>
}


// -----------------------------
// PAYMENT LOGS & SCHEDULED PAYMENTS
// -----------------------------

@Repository
interface PaymentLogRepository : JpaRepository<PaymentLog, Long> {
    fun findByTransactionId(transactionId: Long): List<PaymentLog>
}

@Repository
interface ScheduledPaymentRepository : JpaRepository<ScheduledPayment, Long> {

    fun findByUserId(userId: Long): List<ScheduledPayment>

    fun findByStatus(status: PaymentStatus): List<ScheduledPayment>

    // Used by the scheduler to pick up pending jobs that should run now
    fun findByScheduleTimeBeforeAndStatus(
        time: LocalDateTime,
        status: PaymentStatus = PaymentStatus.PENDING
    ): List<ScheduledPayment>
}
