package com.fintech.mpesascheduler.dto

data class PaymentLogRequest(
    val transactionId: Long? = null,
    val message: String = ""
)
