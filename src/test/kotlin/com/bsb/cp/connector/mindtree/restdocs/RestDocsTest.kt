package com.bsb.cp.connector.mindtree.restdocs

import com.bsb.cp.connector.mindtree.domain.Brand
import com.bsb.cp.connector.mindtree.response.AuthResponse
import com.bsb.cp.connector.mindtree.response.AuthorizationResponse
import com.bsb.cp.connector.mindtree.response.CustomerResponse
import com.bsb.cp.connector.mindtree.service.ConnectorService
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@RunWith(SpringRunner::class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs(outputDir = "build/generated-snippets", uriHost = "bsb-cp-mindtree-connector.example.com", uriScheme = "https", uriPort = 443)
class RestDocsTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var creditAccountService: ConnectorService

    @Test
    fun `generate rest docs for post auth`() {

        whenever(creditAccountService.postAuth(any())).thenReturn(AuthResponse(AuthorizationResponse.APPROVED, "1234567890"))

        val results = mockMvc.perform(
            RestDocumentationRequestBuilders.post("/payments/brand/{brand}/accounts/{cardNumber}/auth", "coolco2", "1234567890123456")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """{
                                      "paymentMethodNonce": "1234567890123456",
                                      "paymentMethodToken": "token",
                                      "customerId": "123",
                                      "orderNumber": "12345678",
                                      "amount": 12.34,
                                      "accountType": "PayPal",
                                      "address": {
                                        "firstName": "John",
                                        "middleInitial": "P",
                                        "lastName": "Doe",
                                        "address1": "123 Any Street",
                                        "address2": "1st Floor",
                                        "apartment": "3A",
                                        "city": "Anchorage",
                                        "state": "AK",
                                        "zipCode": "12345",
                                        "country": "US"
                                      }
                                    }"""
                )
        )

        results.andExpect(MockMvcResultMatchers.status().isOk)
            .andDo(
                MockMvcRestDocumentation.document(
                    "auth-post",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    requestFields(
                        fieldWithPath("paymentMethodNonce").description("mindtree nonce"),
                        fieldWithPath("paymentMethodToken").description("Payment method token"),
                        fieldWithPath("customerId").description("Customer Id"),
                        fieldWithPath("orderNumber").description("Order Number"),
                        fieldWithPath("amount").description("Dollar amount of the auth"),
                        fieldWithPath("accountType").description("The type of account: `PayPal`"),
                        fieldWithPath("address.firstName").description("First Name"),
                        fieldWithPath("address.middleInitial").description("Middle Initial"),
                        fieldWithPath("address.lastName").description("Last Name"),
                        fieldWithPath("address.address1").description("Address 1"),
                        fieldWithPath("address.address2").description("Address 2"),
                        fieldWithPath("address.apartment").description("Apartment"),
                        fieldWithPath("address.city").description("City"),
                        fieldWithPath("address.state").description("State"),
                        fieldWithPath("address.zipCode").description("Zip Code"),
                        fieldWithPath("address.country").description("Country")
                    ),
                    pathParameters(
                        parameterWithName("brand").description(validBrands()),
                        parameterWithName("cardNumber").description("Customer's PLCC card number or 'guest' for guest accounts")
                    ),
                    responseFields(
                        fieldWithPath("authorizationResponse").description("One of: `Approved` `Declined`"),
                        fieldWithPath("transactionId").description("PayPal transactionId to settle against"),
                        fieldWithPath("paymentInstrumentType").description("Payment instrument type"),
                        fieldWithPath("processorAuthorizationCode").description("Processor authorization code"),
                        fieldWithPath("processorResponseCode").description("Processor response code"),
                        fieldWithPath("processorResponseText").description("Processor response text"),
                        fieldWithPath("processorResponseType").description("Processor response type"),
                        fieldWithPath("processorSettlementResponseCode").description("Processor settlement response code"),
                        fieldWithPath("processorSettlementResponseText").description("Processor settlement response text"),
                        fieldWithPath("partialSettlementTransactionIds").description("Partial settlement transaction ids"),
                        fieldWithPath("amount").description("Amount"),
                        fieldWithPath("status").description("Status"),
                        fieldWithPath("authorizedTransactionId").description("Authorization transaction id"),
                        fieldWithPath("authorizationExpiresAt").description("Authorization expire at date"),
                        fieldWithPath("paypalCaptureId").description("PayPal capture Id"),
                        fieldWithPath("paypalRefundId").description("PayPal refund Id"),
                        fieldWithPath("errors").description("Errors")
                    )
                )
            )
    }

    @Test
    fun `generate rest docs for post settle`() {

        whenever(creditAccountService.postSettlement(any())).thenReturn(AuthResponse(AuthorizationResponse.APPROVED, "1234567890"))

        val results = mockMvc.perform(
            RestDocumentationRequestBuilders.post("/payments/brand/{brand}/accounts/{cardNumber}/auth/settle", "coolco3", "1234567890123456")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"transactionId": "123456", "paymentMethodNonce":"1234567890123456","paymentMethodToken":"token","customerId":"123","amount":12.34,"accountType":"PayPal"}""")
            // .header("Authorization", "Bearer 32de0aa7-58d0-4d92-962e-395a46c06671")
        )

        results.andExpect(MockMvcResultMatchers.status().isOk)
            .andDo(
                MockMvcRestDocumentation.document(
                    "settle-post",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    pathParameters(
                        parameterWithName("brand").description(validBrands()),
                        parameterWithName("cardNumber").description("Customer's PLCC card number or 'guest' for guest accounts")
                    ),
                    requestFields(
                        fieldWithPath("transactionId").description("transaction id from auth response"),
                        fieldWithPath("paymentMethodNonce").description("mindtree nonce"),
                        fieldWithPath("paymentMethodToken").description("Payment method token"),
                        fieldWithPath("customerId").description("Customer Id"),
                        fieldWithPath("amount").description("Dollar amount of the auth"),
                        fieldWithPath("accountType").description("The type of account: `PayPal`")
                    ),
                    responseFields(
                        fieldWithPath("authorizationResponse").description("One of: `Approved` `Declined`"),
                        fieldWithPath("transactionId").description("PayPal transactionId to settle against"),
                        fieldWithPath("paymentInstrumentType").description("Payment instrument type"),
                        fieldWithPath("processorAuthorizationCode").description("Processor authorization code"),
                        fieldWithPath("processorResponseCode").description("Processor response code"),
                        fieldWithPath("processorResponseText").description("Processor response text"),
                        fieldWithPath("processorResponseType").description("Processor response type"),
                        fieldWithPath("processorSettlementResponseCode").description("Processor settlement response code"),
                        fieldWithPath("processorSettlementResponseText").description("Processor settlement response text"),
                        fieldWithPath("partialSettlementTransactionIds").description("Partial settlement transaction ids"),
                        fieldWithPath("amount").description("Amount"),
                        fieldWithPath("status").description("Status"),
                        fieldWithPath("authorizedTransactionId").description("Authorization transaction id"),
                        fieldWithPath("authorizationExpiresAt").description("Authorization expire at date"),
                        fieldWithPath("paypalCaptureId").description("PayPal capture Id"),
                        fieldWithPath("paypalRefundId").description("PayPal refund Id"),
                        fieldWithPath("errors").description("Errors")
                    )
                )
            )
    }

    @Test
    fun `generate rest docs for payment auth reversal`() {
        val results = mockMvc.perform(
            RestDocumentationRequestBuilders.delete(
                "/payments/brand/{brand}/accounts/{cardNumber}/auth/transaction/{transactionId}",
                "Company",
                "1234567890123456",
                "456"
            )
        )

        results.andExpect(MockMvcResultMatchers.status().isOk)
            .andDo(
                MockMvcRestDocumentation.document(
                    "auth-reversal",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    pathParameters(
                        parameterWithName("transactionId").description("transaction id from auth response"),
                        parameterWithName("brand").description(validBrands()),
                        parameterWithName("cardNumber").description("customer's card number")
                    )
                )
            )
    }

    // FOR USE WITH REQUESTS RECEIVED FROM CWDirect

    @Ignore("payments/auth api not present in the application")
    @Test
    fun `generate rest docs for post cwdirect auth request`() {

        whenever(creditAccountService.postAuth(any())).thenReturn(AuthResponse(AuthorizationResponse.APPROVED, "1234567890"))

        val results = mockMvc.perform(
            RestDocumentationRequestBuilders.post("/payments/auth")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """{
                                      "paymentMethodNonce": "1234567890123456",
                                      "accountType": "PayPal",
                                      "paymentMethodToken": "token",
                                      "customerId": "123",
                                      "orderNumber": "12345678",
                                      "amount": 12.34,
                                      "address": {
                                        "firstName": "John",
                                        "middleInitial": "P",
                                        "lastName": "Doe",
                                        "address1": "123 Any Street",
                                        "address2": "",
                                        "apartment": "3A",
                                        "city": "Anchorage",
                                        "state": "AK",
                                        "zipCode": "12345",
                                        "country": "US"
                                      }
                                    }"""
                )
        )

        results.andExpect(MockMvcResultMatchers.status().isOk)
            .andDo(
                MockMvcRestDocumentation.document(
                    "cwd-auth-post",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    requestFields(
                        fieldWithPath("paymentMethodNonce").description("mindtree nonce"),
                        fieldWithPath("paymentMethodToken").description("Payment method token"),
                        fieldWithPath("customerId").description("Customer Id"),
                        fieldWithPath("orderNumber").description("Order Number"),
                        fieldWithPath("amount").description("Dollar amount of the auth"),
                        fieldWithPath("accountType").description("The type of account: `PayPal`"),
                        fieldWithPath("address.firstName").description("First Name"),
                        fieldWithPath("address.middleInitial").description("Middle Initial"),
                        fieldWithPath("address.lastName").description("Last Name"),
                        fieldWithPath("address.address1").description("Address 1"),
                        fieldWithPath("address.address2").description("Address 2"),
                        fieldWithPath("address.apartment").description("Apartment"),
                        fieldWithPath("address.city").description("City"),
                        fieldWithPath("address.state").description("State"),
                        fieldWithPath("address.zipCode").description("Zip Code"),
                        fieldWithPath("address.country").description("Country")
                    ),
                    responseFields(
                        fieldWithPath("authorizationResponse").description("One of: `Approved` `Declined`"),
                        fieldWithPath("transactionId").description("PayPal transactionId to settle against"),
                        fieldWithPath("paymentInstrumentType").description("Payment instrument type"),
                        fieldWithPath("processorAuthorizationCode").description("Processor authorization code"),
                        fieldWithPath("processorResponseCode").description("Processor response code"),
                        fieldWithPath("processorResponseText").description("Processor response text"),
                        fieldWithPath("processorResponseType").description("Processor response type"),
                        fieldWithPath("processorSettlementResponseCode").description("Processor settlement response code"),
                        fieldWithPath("processorSettlementResponseText").description("Processor settlement response text"),
                        fieldWithPath("partialSettlementTransactionIds").description("Partial settlement transaction ids"),
                        fieldWithPath("amount").description("Amount"),
                        fieldWithPath("status").description("Status"),
                        fieldWithPath("authorizedTransactionId").description("Authorization transaction id"),
                        fieldWithPath("authorizationExpiresAt").description("Authorization expire at date"),
                        fieldWithPath("paypalCaptureId").description("PayPal capture Id"),
                        fieldWithPath("paypalRefundId").description("PayPal refund Id"),
                        fieldWithPath("errors").description("Errors")
                    )
                )
            )
    }

    @Ignore("payments/auth/settle api not present in the application")
    @Test
    fun `generate rest docs for post cwdirect settle request`() {

        whenever(creditAccountService.postSettlement(any())).thenReturn(AuthResponse(AuthorizationResponse.APPROVED, "1234567890"))

        val results = mockMvc.perform(
            RestDocumentationRequestBuilders.post("/payments/auth/settle")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"transactionId": "123456", "paymentMethodNonce":"1234567890123456","paymentMethodToken":"token","customerId":"123","amount":12.34,"accountType":"PayPal"}""")
            // .header("Authorization", "Bearer 32de0aa7-58d0-4d92-962e-395a46c06671")
        )

        results.andExpect(MockMvcResultMatchers.status().isOk)
            .andDo(
                MockMvcRestDocumentation.document(
                    "cwd-settle-post",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    requestFields(
                        fieldWithPath("transactionId").description("transaction id from auth response"),
                        fieldWithPath("paymentMethodNonce").description("mindtree nonce"),
                        fieldWithPath("paymentMethodToken").description("Payment method token"),
                        fieldWithPath("customerId").description("Customer Id"),
                        fieldWithPath("amount").description("Dollar amount of the auth"),
                        fieldWithPath("accountType").description("The type of account: `PayPal`")
                    ),
                    responseFields(
                        fieldWithPath("authorizationResponse").description("One of: `Approved` `Declined`"),
                        fieldWithPath("transactionId").description("PayPal transactionId to settle against"),
                        fieldWithPath("paymentInstrumentType").description("Payment instrument type"),
                        fieldWithPath("processorAuthorizationCode").description("Processor authorization code"),
                        fieldWithPath("processorResponseCode").description("Processor response code"),
                        fieldWithPath("processorResponseText").description("Processor response text"),
                        fieldWithPath("processorResponseType").description("Processor response type"),
                        fieldWithPath("processorSettlementResponseCode").description("Processor settlement response code"),
                        fieldWithPath("processorSettlementResponseText").description("Processor settlement response text"),
                        fieldWithPath("partialSettlementTransactionIds").description("Partial settlement transaction ids"),
                        fieldWithPath("amount").description("Amount"),
                        fieldWithPath("status").description("Status"),
                        fieldWithPath("authorizedTransactionId").description("Authorization transaction id"),
                        fieldWithPath("authorizationExpiresAt").description("Authorization expire at date"),
                        fieldWithPath("paypalCaptureId").description("PayPal capture Id"),
                        fieldWithPath("paypalRefundId").description("PayPal refund Id"),
                        fieldWithPath("errors").description("Errors")
                    )
                )
            )
    }

    @Test
    fun `generate rest docs for reverse payment`() {
        val results = mockMvc.perform(
            RestDocumentationRequestBuilders.delete(
                "/payments/brand/{brand}/accounts/{cardNumber}/auth/transaction/{transactionId}",
                "Company",
                "1234567890123456",
                "456"
            )
        )

        results.andExpect(MockMvcResultMatchers.status().isOk)
            .andDo(
                MockMvcRestDocumentation.document(
                    "auth-void",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    pathParameters(
                        parameterWithName("transactionId").description("transaction id from auth response"),
                        parameterWithName("brand").description(validBrands()),
                        parameterWithName("cardNumber").description("customer's card number")
                    )
                )
            )
    }

    @Test
    fun `generate rest docs for post customer`() {

        whenever(creditAccountService.createCustomer(any())).thenReturn(CustomerResponse(paymentMethodToken = "1234656789"))

        val results = mockMvc.perform(
            RestDocumentationRequestBuilders.post("/customers/brand/{brand}", "coolco1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"paymentMethodNonce": "1234567890", "mappingToken": "8100000000000001"}""")
        )

        results.andExpect(MockMvcResultMatchers.status().isOk)
            .andDo(
                MockMvcRestDocumentation.document(
                    "customer-post",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                    pathParameters(
                        parameterWithName("brand").description(validBrands())
                    ),
                    requestFields(
                        fieldWithPath("paymentMethodNonce").description("nonce originating from mindtree/checkout front-end"),
                        fieldWithPath("mappingToken").description("mod10 mapping token that is used to provide compatibility with CW")
                    ),
                    responseFields(
                        fieldWithPath("paymentMethodToken").description("mindtree's payment method token"),
                        fieldWithPath("errors").optional().description("Errors")
                    )
                )
            )
    }
}

private fun validBrands() = "One of: `${Brand.validForService().joinToString(" ")}`"
