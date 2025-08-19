package com.mjg.cp.connector.mindtree.response

import com.mjg.cp.connector.mindtree.domain.TransactionStatus
import java.math.BigDecimal
import java.util.Calendar

data class AuthResponse(
    val authorizationResponse: AuthorizationResponse,
    val transactionId: String? = null,
    val paymentInstrumentType: String? = null,
    val processorAuthorizationCode: String? = null,
    val processorResponseCode: String? = null,
    val processorResponseText: String? = null,
    val processorResponseType: String? = null,
    val processorSettlementResponseCode: String? = null,
    val processorSettlementResponseText: String? = null,
    val partialSettlementTransactionIds: List<String>? = null,
    val amount: BigDecimal? = null,
    val status: TransactionStatus? = null,
    val authorizedTransactionId: String? = null,
    val authorizationExpiresAt: Calendar? = null,
    val paypalCaptureId: String? = null,
    val paypalRefundId: String? = null,
    val errors: List<mindtreeError>? = null
)

data class mindtreeError(val message: String?, val code: String?, val codeName: String?, val attribute: String?)
enum class AuthorizationResponse { APPROVED, DECLINED }

/*
   NOTE:  The Transaction statuses returned from mindtree could potentially be any one of the following:

   AUTHORIZED
   AUTHORIZATION_EXPIRED
   AUTHORIZING
   FAILED
   GATEWAY_REJECTED
   PROCESSOR_DECLNED
   SETTLED
   SETTLEMENT_CONFIRMED
   SETTLEMENT_DECLINED
   SETTLEMENT_PENDING
   SETTLING
   SUBMITTED_FOR_SETTLEMENT
   UNRECOGNIZED
   VOIDED

 */
