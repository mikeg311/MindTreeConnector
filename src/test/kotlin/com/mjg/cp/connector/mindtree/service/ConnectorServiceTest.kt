package com.mjg.cp.connector.mindtree.service

import com.mindtreegateway.ProcessorResponseType
import com.mindtreegateway.Result
import com.mindtreegateway.Transaction
import com.mindtreegateway.ValidationErrors
import com.mjg.common.exception.ApiException
import com.mjg.cp.connector.mindtree.authRequest
import com.mjg.cp.connector.mindtree.authResponse
import com.mjg.cp.connector.mindtree.client.MindtreeConnectorClient
import com.mjg.cp.connector.mindtree.config.MindTreeConnectorProperties
import com.mjg.cp.connector.mindtree.domain.AccountType
import com.mjg.cp.connector.mindtree.domain.TransactionStatus
import com.mjg.cp.connector.mindtree.request.AuthRequest
import com.bsb.cp.connector.mindtree.request.CustomerRequest
import com.bsb.cp.connector.mindtree.request.RefundRequest
import com.bsb.cp.connector.mindtree.response.AuthResponse
import com.bsb.cp.connector.mindtree.response.AuthorizationResponse
import com.bsb.cp.connector.mindtree.response.CustomerResponse
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifyAll
import org.amshove.kluent.shouldBeGreaterOrEqualTo
import org.amshove.kluent.shouldBeLessOrEqualTo
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotBeNull
import org.amshove.kluent.shouldThrow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.math.BigDecimal
import java.util.Calendar

internal class ConnectorServiceTest {

    private val mindtreeClient: MindtreeConnectorClient = mockk()
    private val properties = MindTreeConnectorProperties()
    private val connectorService = ConnectorService(mindtreeClient, properties)

    @BeforeEach
    fun setup() {
        clearAllMocks()
        with(properties) {
            loadTest.enabled = true
            loadTest.testNonce = "loadtest"
            loadTest.dummyResponseToken = "1000000000000000"
            loadTest.randomDelayFrom = 500
            loadTest.randomDelayTo = 1000
        }
    }

    @Test
    fun `post auth returns approved`() {

        // Given

        val authRequest = AuthRequest(
            transactionId = "123",
            paymentMethodNonce = "fake-valid-nonce",
            paymentMethodToken = "fake-token",
            customerId = "123",
            amount = BigDecimal("15.15"),
            accountType = AccountType.PalPay
        )

        val transaction: Transaction = mockk(relaxed = true)

        val cal = Calendar.getInstance()

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

        every { mindtreeClient.postPaymentAuth(any()) } returns Result(transaction)

        // When

        val actualResponse = connectorService.postAuth(authRequest)

        // Then

        verifyAll {
            mindtreeClient.postPaymentAuth(authRequest)
        }

        actualResponse.shouldNotBeNull()
        actualResponse shouldEqual expectedResponse
    }

    @Test
    fun `post auth returns existing auth`() {

        // Given

        val authRequest = AuthRequest(
            transactionId = "123",
            paymentMethodNonce = "fake-valid-nonce",
            paymentMethodToken = "fake-token",
            customerId = "123",
            amount = BigDecimal("15.15"),
            accountType = AccountType.PalPay
        )
        val cal = Calendar.getInstance()
        val transaction: Transaction = mockk(relaxed = true)
        val result = Result(transaction)

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

        every { transaction.id } returns "100"
        every { transaction.paymentInstrumentType } returns "visa"
        every { transaction.payPalDetails.authorizationId } returns "tran-id"
        every { transaction.amount } returns "15.15".toBigDecimal()
        every { transaction.status } returns Transaction.Status.AUTHORIZED
        every { transaction.processorResponseType } returns ProcessorResponseType.APPROVED
        every { transaction.partialSettlementTransactionIds } returns null
        every { transaction.authorizedTransactionId } returns "123"
        every { transaction.authorizationExpiresAt } returns cal

        every { mindtreeClient.postPaymentAuth(any()) } returns result

        // When

        val actualResponse = connectorService.postAuth(authRequest)

        // Then

        verify { mindtreeClient.postPaymentAuth(authRequest) }

        actualResponse.shouldNotBeNull()
        actualResponse.authorizationResponse shouldEqual expectedResponse.authorizationResponse
        actualResponse.transactionId shouldEqual expectedResponse.transactionId
        actualResponse.paymentInstrumentType shouldEqual expectedResponse.paymentInstrumentType
        actualResponse.processorAuthorizationCode shouldEqual expectedResponse.processorAuthorizationCode
        actualResponse.processorResponseCode shouldEqual expectedResponse.processorResponseCode
        actualResponse.processorResponseText shouldEqual expectedResponse.processorResponseText
        actualResponse.processorResponseType shouldEqual expectedResponse.processorResponseType
        actualResponse.processorSettlementResponseCode shouldEqual expectedResponse.processorSettlementResponseCode
        actualResponse.processorSettlementResponseText shouldEqual expectedResponse.processorSettlementResponseText
    }

    @Test
    fun `post Auth returns transaction negative`() {

        // Given

        val authRequest = AuthRequest(
            transactionId = "123",
            paymentMethodNonce = "fake-valid-nonce",
            paymentMethodToken = "fake-token",
            customerId = "123",
            amount = BigDecimal("-1.15"),
            accountType = AccountType.PalPay
        )

        every { mindtreeClient.postPaymentAuth(any()) } returns Result(
            ValidationErrors()
        )

        // When

        val actualResponse = connectorService.postAuth(authRequest)

        // Then

        verifyAll {
            mindtreeClient.postPaymentAuth(authRequest)
        }

        actualResponse.shouldNotBeNull()
        actualResponse.authorizationResponse shouldEqual AuthorizationResponse.DECLINED
    }

    @Test
    fun `post Settlement returns approved`() {

        // Given

        val settlementRequest = AuthRequest(
            transactionId = "123",
            paymentMethodNonce = "fake-valid-nonce",
            paymentMethodToken = "fake-token",
            customerId = "123",
            amount = BigDecimal("15.15"),
            accountType = AccountType.PalPay
        )

        val transaction: Transaction = mockk(relaxed = true)

        every { mindtreeClient.postPaymentSettlement(any()) } returns Result(
            transaction
        )

        // When

        val actualResponse = connectorService.postSettlement(settlementRequest)

        // Then

        verifyAll {
            mindtreeClient.postPaymentSettlement(settlementRequest)
        }

        actualResponse.shouldNotBeNull()
        actualResponse.authorizationResponse shouldEqual AuthorizationResponse.APPROVED
    }

    @Test
    fun `post Settlement returns transaction negative`() {

        // Given

        val settlementRequest = AuthRequest(
            transactionId = "123",
            paymentMethodNonce = "fake-valid-nonce",
            paymentMethodToken = "fake-token",
            customerId = "123",
            amount = BigDecimal("-1.15"),
            accountType = AccountType.PalPay
        )

        every { mindtreeClient.postPaymentSettlement(any()) } returns Result<Transaction>(ValidationErrors())

        // When

        val actualResponse = connectorService.postSettlement(settlementRequest)

        // Then

        verifyAll {
            mindtreeClient.postPaymentSettlement(settlementRequest)
        }

        actualResponse.shouldNotBeNull()
        actualResponse.authorizationResponse shouldEqual AuthorizationResponse.DECLINED
    }

    @Test
    fun `reverses transaction`() {

        // Given

        val transactionResult: Result<Transaction> = mockk()

        every { mindtreeClient.reverseAuthorization(any()) } returns transactionResult

        // When

        connectorService.reverseAuth("123")

        // Then

        verify { mindtreeClient.reverseAuthorization("123") }
    }

    @Test
    fun `refunds payment`() {

        val request = RefundRequest(transactionId = "123", amount = BigDecimal("10.00"))

        every { mindtreeClient.refund(any()) } returns Result<Transaction>(ValidationErrors())

        // When

        val actualResponse = connectorService.refund(request)

        // Then

        verify { mindtreeClient.refund(request) }

        actualResponse.shouldNotBeNull()
        actualResponse.authorizationResponse shouldEqual AuthorizationResponse.DECLINED
    }

    @Test
    fun `creates customer`() {
        // given
        val customerRequest = CustomerRequest(paymentMethodNonce = "123", mappingToken = "456")
        val customerResponse = CustomerResponse(paymentMethodToken = "789")
        every { mindtreeClient.createCustomer(any()) } returns customerResponse

        // when
        val actual = connectorService.createCustomer(customerRequest)

        // then
        actual shouldEqual customerResponse
        verify { mindtreeClient.createCustomer(customerRequest) }
    }

    @Test
    fun `creates customer - mocks with load test`() {
        // given
        val customerRequest = CustomerRequest(paymentMethodNonce = "loadtest", mappingToken = "456")
        val customerResponse = CustomerResponse(paymentMethodToken = "1000000000000000")
        val start = System.currentTimeMillis()

        // when
        val actual = connectorService.createCustomer(customerRequest)
        val timeInMs = System.currentTimeMillis() - start

        // then
        timeInMs shouldBeGreaterOrEqualTo 500
        timeInMs shouldBeLessOrEqualTo 1500 // leave some time for processing
        actual shouldEqual customerResponse
        verify(exactly = 0) { mindtreeClient.createCustomer(customerRequest) }
    }

    @Test
    fun `auths and settles enforces payment method token`() {
        val authRequest = authRequest(paymentMethodToken = null)

        // when
        val func = { connectorService.postAuthAndSettle(authRequest) }

        // then
        func.shouldThrow(ApiException::class).exception.httpStatus shouldEqual HttpStatus.BAD_REQUEST
    }

    @Test
    fun `auths and settles happy path`() {
        val spy = spyk(connectorService)
        val authRequest = authRequest(paymentMethodToken = "xyz")
        val expected = authResponse()
        val transactionResult: Result<Transaction> = mockk()
        every { spy.processResult(any()) } returns expected
        every { mindtreeClient.postPaymentAuth(any(), any()) } returns transactionResult

        // when
        val result = spy.postAuthAndSettle(authRequest)

        // then
        result shouldEqual expected
        verify { spy.processResult(transactionResult) }
        verify { mindtreeClient.postPaymentAuth(authRequest, true) }
    }
}
