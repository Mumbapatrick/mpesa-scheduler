package com.fintech.mpesascheduler.service

import com.fintech.mpesascheduler.config.MpesaProperties
import com.fintech.mpesascheduler.dto.StkPushRequest
import com.fintech.mpesascheduler.entity.MpesaTransaction
import com.fintech.mpesascheduler.repository.MpesaTransactionRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class MpesaService(
    private val restTemplate: RestTemplate,
    private val props: MpesaProperties,
    private val repository: MpesaTransactionRepository,
    @Value("\${app.base-url}") private val appBaseUrl: String
) {

    private val logger = LoggerFactory.getLogger(MpesaService::class.java)

    fun initiateStkPush(request: StkPushRequest): String {
        val phoneNumber = request.phoneNumber.takeIf { it.isNotBlank() } ?: getSandboxTestNumber()
        val amount = request.amount.toInt()
        logger.info("🔄 Initiating Sandbox STK Push | Phone=$phoneNumber, Amount=$amount")

        val token = getAccessToken()
        val timestamp = generateTimestamp()
        val password = generatePassword(timestamp)

        val transaction = repository.save(
            MpesaTransaction(
                targetValue = phoneNumber,
                transactionType = "INDIVIDUAL",
                amount = request.amount,
                reference = request.accountReference,
                phoneNumber = phoneNumber,
                status = "PENDING",
                transactionDate = LocalDateTime.now()
            )
        )

        val callbackUrl = "$appBaseUrl/api/mpesa/callback/${transaction.id}"

        val stkBody = mapOf(
            "BusinessShortCode" to props.shortCode,
            "Password" to password,
            "Timestamp" to timestamp,
            "TransactionType" to "CustomerPayBillOnline",
            "Amount" to amount,
            "PartyA" to phoneNumber,
            "PartyB" to props.shortCode,
            "PhoneNumber" to phoneNumber,
            "CallBackURL" to callbackUrl,
            "AccountReference" to request.accountReference,
            "TransactionDesc" to request.transactionDesc
        )

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            set("Authorization", "Bearer $token")
        }

        return try {
            val url = "${props.baseUrl}/mpesa/stkpush/v1/processrequest"
            logger.info("Sending STK Push → $url")

            val response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                HttpEntity(stkBody, headers),
                object : ParameterizedTypeReference<Map<String, Any>>() {}
            )

            val body = response.body ?: emptyMap()

            transaction.apply {
                checkoutRequestId = body["CheckoutRequestID"]?.toString()
                responseCode = body["ResponseCode"]?.toString()
                responseDescription = body["ResponseDescription"]?.toString()?.take(1000)
                status = if (responseCode == "0") "REQUEST_SENT" else "FAILED"
            }

            repository.save(transaction)
            logger.info(" STK Push request completed for TxID: ${transaction.id}")
            "STK Push initiated successfully"

        } catch (ex: Exception) {
            logger.error(" STK Push Error: ${ex.message}", ex)

            transaction.apply {
                status = "FAILED"
                responseDescription = ex.message?.take(1000)
            }
            repository.save(transaction)

            "STK Push failed → ${ex.message}"
        }
    }

    private fun getAccessToken(): String {
        val url = "${props.baseUrl}/oauth/v1/generate?grant_type=client_credentials"
        return try {
            val headers = HttpHeaders().apply {
                setBasicAuth(props.consumerKey.trim(), props.consumerSecret.trim())
            }

            val response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                HttpEntity<String>(headers),
                object : ParameterizedTypeReference<Map<String, Any>>() {}
            )

            response.body?.get("access_token")?.toString()
                ?.also { logger.info(" Daraja Access Token Fetched") }
                ?: throw RuntimeException("No access_token returned")

        } catch (ex: Exception) {
            logger.error(" Failed to fetch token: ${ex.message}", ex)
            throw ex
        }
    }

    private fun generatePassword(timestamp: String): String {
        val raw = props.shortCode + props.passKey + timestamp
        logger.info(" Generated Password (sandbox) = $raw")
        return Base64.getEncoder().encodeToString(raw.toByteArray())
    }

    private fun generateTimestamp(): String =
        LocalDateTime.now(ZoneId.of("Africa/Nairobi")).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))

    fun getSandboxTestNumber(): String = "254708374149"
}
