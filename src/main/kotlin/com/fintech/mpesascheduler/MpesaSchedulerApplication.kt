package com.fintech.mpesascheduler

import com.fintech.mpesascheduler.config.MpesaProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(MpesaProperties::class)
class MpesaSchedulerApplication

fun main(args: Array<String>) {
    runApplication<MpesaSchedulerApplication>(*args)
}
