package com.fintech.mpesascheduler.service

import com.fintech.mpesascheduler.entity.MpesaTransaction
import com.fintech.mpesascheduler.entity.PaymentLog
import com.fintech.mpesascheduler.repository.PaymentLogRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PaymentLogService(
    private val paymentLogRepository: PaymentLogRepository
) {

    /**
     * Log an action with custom details (for ScheduledPaymentService)
     */
    @Transactional
    fun logAction(
        action: String,
        status: String,
        accountReference: String,
        transactionDesc: String,
        details: String
    ): PaymentLog {
        val log = PaymentLog(
            action = action,
            status = status,
            accountReference = accountReference,
            transactionDesc = transactionDesc,
            details = details,
        )
        return paymentLogRepository.save(log)
    }

    /**
     * Save a new payment log entry for auditing or debugging.
     */
    @Transactional
    fun logPayment(
        transaction: MpesaTransaction?,
        requestPayload: String,
        responsePayload: String
    ): PaymentLog {
        val log = PaymentLog(
            requestPayload = requestPayload,
            responsePayload = responsePayload,
            transaction = transaction,
        )
        return paymentLogRepository.save(log)
    }

    /**
     * Retrieve all logs
     */
    fun getAllLogs(): List<PaymentLog> =
        paymentLogRepository.findAll().sortedByDescending { it.createdAt }
}
