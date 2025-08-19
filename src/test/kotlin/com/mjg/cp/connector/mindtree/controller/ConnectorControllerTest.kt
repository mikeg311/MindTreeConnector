package com.bsb.cp.connector.mindtree.controller

import com.bsb.common.exception.ApiException
import com.bsb.cp.connector.mindtree.address
import com.bsb.cp.connector.mindtree.authRequest
import com.bsb.cp.connector.mindtree.authResponse
import com.bsb.cp.connector.mindtree.domain.AccountType
import com.bsb.cp.connector.mindtree.domain.Brand
import com.bsb.cp.connector.mindtree.request.AuthRequest
import com.bsb.cp.connector.mindtree.request.CustomerRequest
import com.bsb.cp.connector.mindtree.request.RefundRequest
import com.bsb.cp.connector.mindtree.response.AuthResponse
import com.bsb.cp.connector.mindtree.response.AuthorizationResponse
import com.bsb.cp.connector.mindtree.response.CustomerResponse
import com.bsb.cp.connector.mindtree.service.ConnectorService
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotBeNull
import org.amshove.kluent.shouldThrow
import org.amshove.kluent.withMessage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class ConnectorControllerTest {
    private val service: ConnectorService = mockk()
    private val controller = ConnectorController(service)

    @AfterEach
    fun setup() {
        clearMocks(service)
    }

    @Test
    fun `auth endpoint returns 200`() {

        // Given

        val authRequest = AuthRequest(
            transactionId = "",
            paymentMethodNonce = "",
            paymentMethodToken = "6012345678901234",
            customerId = "",
            orderNumber = "12345678",
            amount = BigDecimal(12.34),
            accountType = AccountType.PalPay,
            address = address()
        )

        val authResponse = AuthResponse(AuthorizationResponse.APPROVED, "12345")

        every { service.postAuth(any()) } returns authResponse

        // When

        val response = controller.postAuth(Brand.COOL_COMPANY_DIVISION1, "6012345678901234", authRequest)

        // Then

        verify { service.postAuth(authRequest) }

        response.shouldNotBeNull()
        response.authorizationResponse shouldEqual AuthorizationResponse.APPROVED
        response.transactionId shouldEqual "12345"
    }

    @Test
    fun `auth endpoint invalid brand returns error`() {

        // Given

        val authRequest = AuthRequest(
            transactionId = "",
            paymentMethodNonce = "",
            paymentMethodToken = "6012345678901234",
            customerId = "",
            orderNumber = null,
            amount = BigDecimal(12.34),
            accountType = AccountType.PalPay,
            address = null
        )

        val authResponse = AuthResponse(AuthorizationResponse.APPROVED, "12345")

        every { service.postAuth(any()) } returns authResponse

        // When

        val func = { controller.postAuth(Brand.COOL_COMPANY_DIVISION2, "6012345678901234", authRequest) }

        // Then

        func shouldThrow ApiException::class withMessage "Brand not supported"

        verify(exactly = 0) { service.postAuth(authRequest) }
    }

    @Test
    fun `settle endpoint returns 200`() {

        // Given

        val settlementRequest = AuthRequest(
            transactionId = "12345",
            paymentMethodNonce = "",
            paymentMethodToken = "6012345678901234",
            customerId = "",
            orderNumber = "12345678",
            amount = BigDecimal(12.34),
            accountType = AccountType.PalPay
        )

        val settlementResponse = AuthResponse(AuthorizationResponse.APPROVED, "12345")

        every { service.postSettlement(any()) } returns settlementResponse

        // When

        val response = controller.postSettlement(Brand.COOL_COMPANY_DIVISION2, "6012345678901234", settlementRequest)

        // Then

        verify { service.postSettlement(settlementRequest) }

        response.shouldNotBeNull()
        response.authorizationResponse shouldEqual AuthorizationResponse.APPROVED
        response.transactionId shouldEqual "12345"
    }

    @Test
    fun `settle endpoint invalid brand returns error`() {

        // Given

        val settlementRequest = AuthRequest(
            transactionId = "12345",
            paymentMethodNonce = "",
            paymentMethodToken = "token",
            customerId = "",
            orderNumber = "",
            amount = BigDecimal(12.34),
            accountType = AccountType.PalPay,
            address = null
        )
        val settlementResponse = AuthResponse(AuthorizationResponse.DECLINED, "12345")

        every { service.postSettlement(any()) } returns settlementResponse

        // When

        val func = { controller.postSettlement(Brand.COOL_COMPANY_DIVISION2, "token", settlementRequest) }

        // Then

        func shouldThrow ApiException::class withMessage "Brand not supported"
        verify(exactly = 0) { service.postSettlement(settlementRequest) }
    }

    @Test
    fun `reverse an authorization`() {

        // Given

        every { service.reverseAuth(any()) } just runs

        // When

        controller.reverseAuth(Brand.COOL_COMPANY_DIVISION1, "6012345678901234", "12345")

        // Then

        verify { service.reverseAuth("12345") }
    }

    @Test
    fun `invalid brand handled when reversing an authorization`() {

        // When

        val func = { controller.reverseAuth(Brand.COOL_COMPANY_DIVISION1, "6012345678901234", "12345") }

        // Then

        func shouldThrow ApiException::class

        verify(exactly = 0) { service.reverseAuth(any()) }
    }

    @Test
    fun `refunds a payment`() {

        // Given

        val request = RefundRequest(transactionId = "123", amount = "10.01".toBigDecimal())
        val refundResponse = AuthResponse(AuthorizationResponse.APPROVED, "123", amount = "10.01".toBigDecimal())

        every { service.refund(any()) } returns refundResponse

        // When

        val response = controller.postRefund(request)

        // Then

        verify { service.refund(request) }

        response.transactionId shouldEqual "123"
        response.amount shouldEqual "10.01".toBigDecimal()
    }

    @Test
    fun `creates a customer`() {
        // given
        val customerRequest = CustomerRequest(paymentMethodNonce = "123abcd", mappingToken = "456")
        val brand = "coolco1"
        val customerResponse = CustomerResponse(paymentMethodToken = "123456")
        every { service.createCustomer(any()) } returns customerResponse

        // when

        val actual = controller.postCustomer(brand, customerRequest)

        // then

        verify { service.createCustomer(customerRequest) }
        actual shouldEqual customerResponse
    }

    @Test
    fun `auths and settles`() {
        // given
        val expected = authResponse(transactionId = "123")
        every { service.postAuthAndSettle(any()) } returns expected
        val authRequest = authRequest(settle = true)

        // when
        val actual = controller.postAuth(
            brand = Brand.COOL_COMPANY_DIVISION2,
            authRequest = authRequest,
            cardNumber = "123"
        )

        // then
        actual shouldEqual expected
        verify { service.postAuthAndSettle(authRequest) }
    }
}
