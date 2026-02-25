package com.fintech.mpesascheduler.dto

import java.time.LocalDateTime

data class ScheduledPaymentRequest(

    val phoneNumber: String,

    val amount: Double,

    val reference: String? = null,

    val scheduleTime: LocalDateTime,

    val userId: Long? = null
)
