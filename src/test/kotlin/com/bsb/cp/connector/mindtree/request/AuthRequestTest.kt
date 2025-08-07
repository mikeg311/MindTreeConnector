package com.bsb.cp.connector.mindtree.request

import com.bsb.cp.connector.mindtree.authRequest
import com.bsb.cs.test.common.objectMapper
import com.bsb.cs.test.common.shouldStrictlyEqualJson
import org.amshove.kluent.shouldNotBeNull
import org.junit.Test

class AuthRequestTest {

    @Test
    fun `test auth request formatting`() {

        // Given

        val authRequest = authRequest()

        val expectedJson =
            """
            {
              "transactionId":"123",
              "paymentMethodNonce":"fake-nonce",
              "paymentMethodToken":"fake-token",
              "customerId":"123",
              "orderNumber":"12345678",
              "amount":10.15,
              "accountType":"PayPal",
              "settle": null,
              "address":{
                "firstName":"John",
                "middleInitial":"P",
                "lastName":"Doe",
                "address1":"123 Any Street",
                "address2":"1st Floor",
                "apartment":"3A",
                "city":"Anchorage",
                "state":"AK",
                "zipCode":"12345",
                "country":"US"}
            }
        """

        // When

        val actualJson = objectMapper().writeValueAsString(authRequest)

        // Then

        actualJson.shouldNotBeNull()
        actualJson shouldStrictlyEqualJson expectedJson
    }
}
