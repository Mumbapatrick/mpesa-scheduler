package com.fintech.mpesascheduler.dto

data class BatchTransactionRequest(
    val batchName: String = "",
    val totalAmount: Double = 0.0,
    val transactionCount: Int = 0,
    val splitType: String = "EQUAL",
    val userId: Long? = null  // just send the user ID
)
