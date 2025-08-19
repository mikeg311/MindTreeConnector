package com.bsb.cp.connector.mindtree.extension

import com.mindtreegateway.ProcessorResponseType
import com.mindtreegateway.Transaction
import com.bsb.cp.connector.mindtree.domain.TransactionStatus
import com.bsb.cp.connector.mindtree.response.AuthResponse
import com.bsb.cp.connector.mindtree.response.AuthorizationResponse
import io.mockk.every
import io.mockk.mockk
import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.util.Calendar

internal class TransactionKtTest {

    @EnumSource(value = Transaction.Status::class, names = ["SETTLED", "SETTLING", "SUBMITTED_FOR_SETTLEMENT"])
    @ParameterizedTest
    fun `converts transaction to AuthResponse - settled`(status: Transaction.Status) {
        val cal = Calendar.getInstance()

        val transaction = mockk<Transaction>(relaxed = true)
        every { transaction.id } returns "100"
        every { transaction.paymentInstrumentType } returns "visa"
        every { transaction.payPalDetails.authorizationId } returns "tran-id"
        every { transaction.amount } returns "15.15".toBigDecimal()
        every { transaction.status } returns status
        every { transaction.processorResponseType } returns ProcessorResponseType.APPROVED
        every { transaction.partialSettlementTransactionIds } returns null
        every { transaction.authorizedTransactionId } returns "123"
        every { transaction.authorizationExpiresAt } returns cal
        every { transaction.payPalDetails.captureId } returns null
        every { transaction.payPalDetails.refundId } returns null

        val expectedResponse = AuthResponse(
            AuthorizationResponse.APPROVED,
            transactionId = "100",
            paymentInstrumentType = "visa",
            processorAuthorizationCode = "tran-id",
            processorResponseCode = "",
            processorResponseText = "",
            processorResponseType = "APPROVED",
            processorSettlementResponseCode = "",
            processorSettlementResponseText = "",
            partialSettlementTransactionIds = null,
            amount = "15.15".toBigDecimal(),
            status = TransactionStatus.SETTLED,
            authorizedTransactionId = "123",
            authorizationExpiresAt = cal,
            paypalCaptureId = null,
            paypalRefundId = null,
            errors = null
        )

        // when
        val actual = transaction.toAuthResponse()

        // then

        actual shouldEqual expectedResponse
    }

    @Test
    fun `converts transaction to AuthResponse`() {
        val cal = Calendar.getInstance()

        val transaction = mockk<Transaction>(relaxed = true)
        every { transaction.id } returns "100"
        every { transaction.paymentInstrumentType } returns "visa"
        every { transaction.payPalDetails.authorizationId } returns "tran-id"
        every { transaction.amount } returns "15.15".toBigDecimal()
        every { transaction.status } returns Transaction.Status.AUTHORIZED
        every { transaction.processorResponseType } returns ProcessorResponseType.APPROVED
        every { transaction.partialSettlementTransactionIds } returns null
        every { transaction.authorizedTransactionId } returns "123"
        every { transaction.authorizationExpiresAt } returns cal
        every { transaction.payPalDetails.captureId } returns null
        every { transaction.payPalDetails.refundId } returns null
        val expectedResponse = AuthResponse(
            AuthorizationResponse.APPROVED,
            transactionId = "100",
            paymentInstrumentType = "visa",
            processorAuthorizationCode = "tran-id",
            processorResponseCode = "",
            processorResponseText = "",
            processorResponseType = "APPROVED",
            processorSettlementResponseCode = "",
            processorSettlementResponseText = "",
            partialSettlementTransactionIds = null,
            amount = "15.15".toBigDecimal(),
            status = TransactionStatus.APPROVED,
            authorizedTransactionId = "123",
            authorizationExpiresAt = cal,
            paypalCaptureId = null,
            paypalRefundId = null,
            errors = null
        )

        // when
        val actual = transaction.toAuthResponse()

        // then

        actual shouldEqual expectedResponse
    }
}
