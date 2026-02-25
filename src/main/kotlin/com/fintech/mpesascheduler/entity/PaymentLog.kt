package com.fintech.mpesascheduler.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "payment_log")
data class PaymentLog(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    // JSON payloads for request/response
    @Column(name = "request_payload", columnDefinition = "jsonb", nullable = true)
    val requestPayload: String? = null,

    @Column(name = "response_payload", columnDefinition = "jsonb", nullable = true)
    val responsePayload: String? = null,

    // Link to MpesaTransaction
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    val transaction: MpesaTransaction? = null,

    @Column(name = "log_time", nullable = false)
    val logTime: LocalDateTime = LocalDateTime.now(),

    // Optional fields for logging actions
    @Column(name = "action")
    val action: String? = null,

    @Column(name = "status")
    val status: String? = null,

    @Column(name = "account_reference")
    val accountReference: String? = null,

    @Column(name = "transaction_desc")
    val transactionDesc: String? = null,

    @Column(name = "details")
    val details: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
