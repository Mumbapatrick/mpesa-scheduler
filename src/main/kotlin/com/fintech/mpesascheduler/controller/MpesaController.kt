package com.fintech.mpesascheduler.controller

import com.fintech.mpesascheduler.dto.BatchTransactionRequest
import com.fintech.mpesascheduler.dto.StkPushRequest
import com.fintech.mpesascheduler.dto.PaymentLogRequest
import com.fintech.mpesascheduler.dto.ScheduledPaymentRequest
import com.fintech.mpesascheduler.entity.*
import com.fintech.mpesascheduler.enums.PaymentStatus
import com.fintech.mpesascheduler.repository.*
import com.fintech.mpesascheduler.service.MpesaService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/mpesa")
class MpesaController(
    private val mpesaService: MpesaService,
    private val mpesaTransactionRepository: MpesaTransactionRepository,
    private val batchTransactionRepository: BatchTransactionRepository,
    private val scheduledPaymentRepository: ScheduledPaymentRepository,
    private val paymentLogRepository: PaymentLogRepository,
    private val userRepository: UserRepository
) {

    // --------------------------
    // STK PUSH
    // --------------------------
    @PostMapping("/stkpush")
    fun initiateStkPush(@RequestBody request: StkPushRequest): ResponseEntity<String> {
        val response = mpesaService.initiateStkPush(request)
        return ResponseEntity.ok(response)
    }

    // --------------------------
    // MPESA TRANSACTIONS
    // --------------------------
    @GetMapping("/transactions")
    fun getAllTransactions(): List<MpesaTransaction> =
        mpesaTransactionRepository.findAll()

    @GetMapping("/transactions/{id}")
    fun getTransaction(@PathVariable id: Long): ResponseEntity<MpesaTransaction> =
        mpesaTransactionRepository.findById(id)
            .map { ResponseEntity.ok(it) }
            .orElse(ResponseEntity.notFound().build())

    @PostMapping("/transactions")
    fun createTransaction(@RequestBody transaction: MpesaTransaction): ResponseEntity<MpesaTransaction> {
        val user = transaction.user?.id?.let { userRepository.findById(it).orElse(null) }
        val batch = transaction.batchTransaction?.id?.let {
            batchTransactionRepository.findById(it).orElse(null)
        }

        val saved = mpesaTransactionRepository.save(
            transaction.copy(
                user = user,
                batchTransaction = batch,
                transactionDate = LocalDateTime.now()
            )
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(saved)
    }

    // --------------------------
    // BATCH TRANSACTIONS
    // --------------------------
    @GetMapping("/batches")
    fun getAllBatches(): List<BatchTransaction> =
        batchTransactionRepository.findAll()

    @GetMapping("/batches/{id}")
    fun getBatch(@PathVariable id: Long): ResponseEntity<BatchTransaction> =
        batchTransactionRepository.findById(id)
            .map { ResponseEntity.ok(it) }
            .orElse(ResponseEntity.notFound().build())

    @PostMapping("/batches")
    fun createBatch(@RequestBody request: BatchTransactionRequest): ResponseEntity<BatchTransaction> {
        val user = request.userId?.let { userRepository.findById(it).orElse(null) }

        val batch = BatchTransaction(
            batchName = request.batchName,
            totalAmount = request.totalAmount,
            transactionCount = request.transactionCount,
            splitType = request.splitType,
            status = "PENDING",
            user = user,
            createdAt = LocalDateTime.now()
        )

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(batchTransactionRepository.save(batch))
    }

    // --------------------------
    // SCHEDULED PAYMENTS
    // --------------------------
    @GetMapping("/scheduled")
    fun getAllScheduledPayments(): List<ScheduledPayment> =
        scheduledPaymentRepository.findAll()

    @PostMapping("/scheduled")
    fun schedulePayment(
        @RequestBody request: ScheduledPaymentRequest
    ): ResponseEntity<ScheduledPayment> {

        val user = request.userId?.let { userRepository.findById(it).orElse(null) }

        val payment = ScheduledPayment(
            phoneNumber = request.phoneNumber,
            amount = request.amount,
            reference = request.reference,
            scheduleTime = request.scheduleTime,
            status = PaymentStatus.PENDING,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            user = user
        )

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(scheduledPaymentRepository.save(payment))
    }

    // --------------------------
    // PAYMENT LOGS
    // --------------------------
    @GetMapping("/logs")
    fun getAllPaymentLogs(): List<PaymentLog> =
        paymentLogRepository.findAll()

    @GetMapping("/logs/transaction/{transactionId}")
    fun getLogsByTransaction(@PathVariable transactionId: Long): List<PaymentLog> =
        paymentLogRepository.findByTransactionId(transactionId)

    @PostMapping("/logs")
    fun createPaymentLog(
        @RequestBody request: PaymentLogRequest
    ): ResponseEntity<PaymentLog> {

        val transaction = request.transactionId?.let {
            mpesaTransactionRepository.findById(it).orElse(null)
        }

        val log = PaymentLog(
            transaction = transaction,
            action = "MANUAL_LOG",
            status = "INFO",
            details = request.message,
            logTime = LocalDateTime.now(),
            createdAt = LocalDateTime.now()
        )

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(paymentLogRepository.save(log))
    }
}
