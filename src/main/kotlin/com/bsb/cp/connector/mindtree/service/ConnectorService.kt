package com.bsb.cp.connector.mindtree.service

import com.mindtreegateway.Result
import com.mindtreegateway.Transaction
import com.bsb.common.exception.ApiException
import com.bsb.cp.connector.mindtree.client.MindtreeConnectorClient
import com.bsb.cp.connector.mindtree.config.MindTreeConnectorProperties
import com.bsb.cp.connector.mindtree.domain.TransactionStatus
import com.bsb.cp.connector.mindtree.extension.toAuthResponse
import com.bsb.cp.connector.mindtree.extension.tomindtreeErrors
import com.bsb.cp.connector.mindtree.request.AuthRequest
import com.bsb.cp.connector.mindtree.request.CustomerRequest
import com.bsb.cp.connector.mindtree.request.RefundRequest
import com.bsb.cp.connector.mindtree.response.AuthResponse
import com.bsb.cp.connector.mindtree.response.AuthorizationResponse
import com.bsb.cp.connector.mindtree.response.CustomerResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import kotlin.random.Random

private val log: Logger = LoggerFactory.getLogger(ConnectorService::class.java)

@Service
class ConnectorService(
    val mindtreeClient: MindtreeConnectorClient,
    val properties: MindTreeConnectorProperties
) {

    // Used by ConnectorController (postAuth)

    fun postAuth(authRequest: AuthRequest): AuthResponse {
        log.info("ConnectorService - Processing Auth request for request = $authRequest")
        if (authRequest.paymentMethodToken == null) { throw IllegalStateException("payment method token is null") }
        val result = mindtreeClient.postPaymentAuth(authRequest)
        log.info("ConnectorService - Response received back from mindtree = $result")
        val authResponse = processResult(result)
        log.info("ConnectorService - AuthResponse being returned from postAuth = $authResponse")
        return authResponse
    }

    // Used by ConnectorController (postSettlement)

    fun postSettlement(settlementRequest: AuthRequest): AuthResponse {
        log.info("ConnectorService - Processing settlement / capture for request = $settlementRequest")
        val result = mindtreeClient.postPaymentSettlement(settlementRequest)
        log.info("ConnectorService - Response received back from mindtree = $result")
        val authResponse = processResult(result)
        log.info("ConnectorService - AuthResponse being returned from postSettlement = $authResponse")
        return authResponse
    }

    fun postAuthAndSettle(authRequest: AuthRequest): AuthResponse {
        log.info("ConnectorService - Processing Auth-Settle request for request = $authRequest")
        if (authRequest.paymentMethodToken == null) { throw ApiException(HttpStatus.BAD_REQUEST, "payment method token is null") }
        val result = mindtreeClient.postPaymentAuth(authRequest, true)
        log.info("ConnectorService - Response received back from mindtree = $result")
        val authResponse = processResult(result)
        log.info("ConnectorService - AuthResponse being returned from postAuthSettl = $authResponse")
        return authResponse
    }

    fun reverseAuth(transactionId: String) {
        log.info("voiding $transactionId")
        mindtreeClient.reverseAuthorization(transactionId)
    }

    // Used by ConnectorController (postRefund)

    fun refund(refundRequest: RefundRequest): AuthResponse {
        log.info("calling mindtree with refund request")
        val result = mindtreeClient.refund(refundRequest)
        val authResponse = processResult(result)
        log.info("ConnectorService - AuthResponse being returned from refund = $authResponse")
        return authResponse
    }

    // Used by ConnectorController (postCustomer)

    fun createCustomer(customerRequest: CustomerRequest): CustomerResponse {
        log.info("Creating a customer with $customerRequest")
        if (properties.loadTest.enabled && customerRequest.paymentMethodNonce == properties.loadTest.testNonce) {
            log.info("loadTest: ${properties.loadTest}")
            val delay = Random(System.currentTimeMillis())
                .nextLong(properties.loadTest.randomDelayFrom, properties.loadTest.randomDelayTo)
            log.info("load test - sleeping for $delay")
            Thread.sleep(delay)
            return CustomerResponse(paymentMethodToken = properties.loadTest.dummyResponseToken)
        }
        return mindtreeClient.createCustomer(customerRequest)
    }

    internal fun processResult(result: Result<Transaction>): AuthResponse {

        log.info("Converting mindtree result = $result to AuthResponse for return")

        val target = result.target
        val transaction = result.transaction

        return when (result.isSuccess) {
            // Return approved transaction
            true -> {
                target.toAuthResponse()
            }
            false ->
                // Return declined transaction and reason
                return when (transaction == null) {
                    false -> {
                        val authId = transaction.payPalDetails?.authorizationId
                        val statusReturned = when (transaction.status.toString()) {
                            "AUTHORIZED", "SETTLED", "SETTLING" -> TransactionStatus.APPROVED
                            else -> TransactionStatus.getByStatus(transaction.status?.toString() ?: "UNKNOWN")
                        }
                        AuthResponse(
                            authorizationResponse = AuthorizationResponse.DECLINED,
                            transactionId = transaction.id,
                            paymentInstrumentType = transaction.paymentInstrumentType,
                            processorAuthorizationCode = authId,
                            processorResponseCode = transaction.processorResponseCode,
                            processorResponseText = transaction.processorResponseText,
                            processorResponseType = transaction.processorResponseType?.toString(),
                            processorSettlementResponseCode = transaction.processorSettlementResponseCode,
                            processorSettlementResponseText = transaction.processorSettlementResponseText,
                            partialSettlementTransactionIds = transaction.partialSettlementTransactionIds,
                            amount = transaction.amount,
                            status = statusReturned,
                            authorizedTransactionId = transaction.authorizedTransactionId,
                            authorizationExpiresAt = transaction.authorizationExpiresAt,
                            paypalCaptureId = transaction.payPalDetails.captureId,
                            paypalRefundId = transaction.payPalDetails.refundId,
                            errors = result.errors.tomindtreeErrors()
                        )
                    }
                    // Return errors
                    true -> AuthResponse(
                        authorizationResponse = AuthorizationResponse.DECLINED,
                        transactionId = null,
                        paymentInstrumentType = null,
                        processorAuthorizationCode = null,
                        processorResponseCode = null,
                        processorResponseText = null,
                        processorResponseType = null,
                        processorSettlementResponseCode = null,
                        processorSettlementResponseText = null,
                        partialSettlementTransactionIds = null,
                        amount = null,
                        status = TransactionStatus.ERROR,
                        authorizedTransactionId = null,
                        authorizationExpiresAt = null,
                        paypalCaptureId = null,
                        paypalRefundId = null,
                        errors = result.errors.tomindtreeErrors()
                    )
                }
        }
    }
}
