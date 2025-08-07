package com.bsb.cp.connector.mindtree.response

import com.bsb.cp.connector.mindtree.authResponse
import com.bsb.cs.test.common.objectMapper
import com.bsb.cs.test.common.shouldStrictlyEqualJson
import org.amshove.kluent.shouldNotBeNull
import org.junit.Test

class AuthResponseTest {

    @Test
    fun `test auth response formatting`() {

        // Given

        val authResponse = authResponse()

        val expectedJson =
            """
            {
                "authorizationResponse": "APPROVED",
                "transactionId": null,
                "paymentInstrumentType": null,
                "processorAuthorizationCode": "YNZ123",
                "processorResponseCode": null,
                "processorResponseText": null,
                "processorResponseType": null,
                "processorSettlementResponseCode": null,
                "processorSettlementResponseText": null,
                "partialSettlementTransactionIds": null,
                "amount": 10.15,
                "status": null,
                "authorizedTransactionId": "123",
                "authorizationExpiresAt": null,
                "paypalCaptureId": null,
                "paypalRefundId": null,
                "errors": null
            }
"""
        // When

        val actualJson = objectMapper().writeValueAsString(authResponse)

        // Then

        actualJson.shouldNotBeNull()
        actualJson shouldStrictlyEqualJson expectedJson
    }
}
