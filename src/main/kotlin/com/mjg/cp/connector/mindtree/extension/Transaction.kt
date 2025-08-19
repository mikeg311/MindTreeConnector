package com.mjg.cp.connector.mindtree.extension

import com.mindtreegateway.Transaction
import com.mjg.cp.connector.mindtree.domain.TransactionStatus
import com.mjg.cp.connector.mindtree.response.AuthResponse
import com.mjg.cp.connector.mindtree.response.AuthorizationResponse

fun Transaction.toAuthResponse(): AuthResponse {

    val transaction = this
    val authId = transaction.payPalDetails?.authorizationId // paypal specific

    val status = when (transaction.status) {
        /*
         * Normalize settlement statuses
         * From mindtree:
         * If you receive a successful response you can treat the
         * response of ‘SETTLING’ or ‘’SUBMITTED_FOR_SETTLEMENT’ as approved.
         * On our side, we use a background process to update the status to ‘SETTLED’ several seconds after the call.
         */
        Transaction.Status.SUBMITTED_FOR_SETTLEMENT, Transaction.Status.SETTLING -> Transaction.Status.SETTLED
        else -> transaction.status
    }

    return AuthResponse(
        authorizationResponse = AuthorizationResponse.APPROVED,
        transactionId = transaction.id,
        paymentInstrumentType = transaction.paymentInstrumentType,
        processorAuthorizationCode = authId,
        processorResponseCode = transaction.processorResponseCode,
        processorResponseText = transaction.processorResponseText,
        processorResponseType = transaction.processorResponseType.toString(),
        processorSettlementResponseCode = transaction.processorSettlementResponseCode,
        processorSettlementResponseText = transaction.processorSettlementResponseText,
        partialSettlementTransactionIds = transaction.partialSettlementTransactionIds,
        amount = transaction.amount,
        status = when (status) {
            Transaction.Status.AUTHORIZED -> TransactionStatus.APPROVED
            else -> TransactionStatus.getByStatus(status.toString())
        },
        authorizedTransactionId = transaction.authorizedTransactionId,
        authorizationExpiresAt = transaction.authorizationExpiresAt,
        paypalCaptureId = transaction.payPalDetails.captureId,
        paypalRefundId = transaction.payPalDetails.refundId,
        errors = null
    )
}
