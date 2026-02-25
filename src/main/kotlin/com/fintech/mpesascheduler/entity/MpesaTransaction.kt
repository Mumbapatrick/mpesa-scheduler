package com.fintech.mpesascheduler.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "mpesa_transaction")
data class MpesaTransaction(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "target_value", nullable = false, length = 255)
    val targetValue: String = "",

    @Column(name = "transaction_type", nullable = false, length = 50)
    val transactionType: String = "INDIVIDUAL",

    @Column(nullable = false)
    val amount: Double = 0.0,

    @Column(length = 255)
    val reference: String? = null,

    @Column(name = "checkout_request_id", length = 200)
    var checkoutRequestId: String? = null,

    @Column(nullable = false, length = 50)
    var status: String = "PENDING",

    @Column(name = "response_code", length = 50)
    var responseCode: String? = null,

    @Column(name = "response_description", columnDefinition = "TEXT")
    var responseDescription: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_transaction_id")
    val batchTransaction: BatchTransaction? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: UserAccount? = null,

    @Column(name = "phone_number", length = 20)
    val phoneNumber: String? = null,

    @Column(name = "scan_type", length = 50)
    val scanType: String? = null,

    @Column(name = "transaction_date")
    val transactionDate: LocalDateTime = LocalDateTime.now()
)
