package com.mjg.cp.connector.mindtree

import com.mjg.cp.connector.mindtree.domain.AccountType
import com.mjg.cp.connector.mindtree.domain.Address
import com.mjg.cp.connector.mindtree.domain.TransactionStatus
import com.mjg.cp.connector.mindtree.request.AuthRequest
import com.mjg.cp.connector.mindtree.response.AuthResponse
import com.mjg.cp.connector.mindtree.response.AuthorizationResponse
import com.mjg.cp.connector.mindtree.response.mindtreeError
import java.math.BigDecimal
import java.util.Calendar

/**
 * Test builder functions that let you focus on the data needed for a test, while setting everything else to defaults.
 */

fun authRequest(
    transactionId: String? = "123",
    paymentMethodNonce: String = "fake-nonce",
    paymentMethodToken: String? = "fake-token",
    customerId: String? = "123",
    orderNumber: String? = "12345678",
    amount: BigDecimal = "10.15".toBigDecimal(),
    accountType: AccountType = AccountType.PalPay,
    address: Address? = address(),
    settle: Boolean? = null
): AuthRequest {
    return AuthRequest(
        transactionId = transactionId,
        paymentMethodNonce = paymentMethodNonce,
        paymentMethodToken = paymentMethodToken,
        customerId = customerId,
        orderNumber = orderNumber,
        amount = amount,
        accountType = accountType,
        address = address,
        settle = settle
    )
}

fun authResponse(
    authorizationResponse: AuthorizationResponse = AuthorizationResponse.APPROVED,
    transactionId: String? = null,
    paymentInstrumentType: String? = null,
    processorAuthorizationCode: String? = "YNZ123",
    processorResponseCode: String? = null,
    processorResponseText: String? = null,
    processorResponseType: String? = null,
    processorSettlementResponseCode: String? = null,
    processorSettlementResponseText: String? = null,
    partialSettlementTransactionIds: List<String>? = null,
    amount: BigDecimal? = "10.15".toBigDecimal(),
    status: TransactionStatus? = null,
    authorizedTransactionId: String? = "123",
    authorizationExpiresAt: Calendar? = null,
    errors: List<mindtreeError>? = null
): AuthResponse {
    return AuthResponse(
        authorizationResponse = authorizationResponse,
        transactionId = transactionId,
        paymentInstrumentType = paymentInstrumentType,
        processorAuthorizationCode = processorAuthorizationCode,
        processorResponseCode = processorResponseCode,
        processorResponseText = processorResponseText,
        processorResponseType = processorResponseType,
        processorSettlementResponseCode = processorSettlementResponseCode,
        processorSettlementResponseText = processorSettlementResponseText,
        partialSettlementTransactionIds = partialSettlementTransactionIds,
        amount = amount,
        status = status,
        authorizedTransactionId = authorizedTransactionId,
        authorizationExpiresAt = authorizationExpiresAt,
        paypalCaptureId = null,
        paypalRefundId = null,
        errors = errors
    )
}

fun address(
    firstName: String = "John",
    middleInitial: String = "P",
    lastName: String = "Doe",
    company: String? = "",
    address1: String = "123 Any Street",
    address2: String? = "1st Floor",
    apartment: String? = "3A",
    city: String? = "Anchorage",
    state: String? = "AK",
    zipCode: String? = "12345",
    country: String? = "US"
): Address {
    return Address(
        firstName,
        middleInitial,
        lastName,
        company,
        address1,
        address2,
        apartment,
        city,
        state,
        zipCode,
        country
    )
}
