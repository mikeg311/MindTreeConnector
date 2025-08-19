package com.mjg.cp.connector.mindtree.controller

import com.mjg.common.exception.ApiException
import com.mjg.cp.connector.mindtree.domain.Brand
import com.mjg.cp.connector.mindtree.domain.isSupportedInService
import com.mjg.cp.connector.mindtree.request.AuthRequest
import com.mjg.cp.connector.mindtree.request.CustomerRequest
import com.mjg.cp.connector.mindtree.request.RefundRequest
import com.mjg.cp.connector.mindtree.response.AuthResponse
import com.mjg.cp.connector.mindtree.response.CustomerResponse
import com.mjg.cp.connector.mindtree.service.ConnectorService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ConnectorController(val connectorService: ConnectorService) {

    private val log: Logger = LoggerFactory.getLogger(ConnectorController::class.java)

    // Put Guest in for card for guest checkout

    @PostMapping("/payments/brand/{brand}/accounts/{cardNumber}/auth")
    fun postAuth(
        @PathVariable("brand") brand: Brand,
        @PathVariable("cardNumber") cardNumber: String,
        @RequestBody authRequest: AuthRequest
    ): AuthResponse {
        log.info("ConnectorController postAuth for request = $authRequest")
        return when {
            brand.isSupportedInService() -> {
                if (authRequest.settle ?: false) {
                    connectorService.postAuthAndSettle(authRequest)
                } else {
                    connectorService.postAuth(authRequest)
                }
            }

            else -> throw ApiException(HttpStatus.BAD_REQUEST, "Brand not supported")
        }
    }

    // Put Guest in for card for guest checkout

    @DeleteMapping("/payments/brand/{brand}/accounts/{cardNumber}/auth/transaction/{transactionId}")
    fun reverseAuth(
        @PathVariable("brand") brand: Brand,
        @PathVariable("cardNumber") cardNumber: String,
        @PathVariable transactionId: String
    ) {
        log.info("ConnectorController reverseAuth for brand=$brand, cardNumber=$cardNumber, transaction=$transactionId")
        when {
            brand.isSupportedInService() -> connectorService.reverseAuth(transactionId)
            else -> throw ApiException(HttpStatus.BAD_REQUEST, "Brand not supported")
        }
    }

    @PostMapping("/payments/brand/{brand}/accounts/{cardNumber}/auth/settle")
    fun postSettlement(
        @PathVariable("brand") brand: Brand,
        @PathVariable("cardNumber") cardNumber: String,
        @RequestBody settlementRequest: AuthRequest
    ): AuthResponse {
        log.info("ConnectorController postSettlement for request = $settlementRequest")
        return when {
            brand.isSupportedInService() -> connectorService.postSettlement(settlementRequest)
            else -> throw ApiException(HttpStatus.BAD_REQUEST, "Brand not supported")
        }
    }

    // FOR USE WITH REQUESTS RECEIVED FROM CWDirect

    @PostMapping("/payments/refund")
    fun postRefund(@RequestBody refundRequest: RefundRequest): AuthResponse {
        log.info("ConnectorController postRefund for request = $refundRequest")
        return connectorService.refund(refundRequest)
    }

    @PostMapping("/payments/auth/reverse/transaction/{transactionId}")
    fun postCWDirectReversalRequest(@PathVariable transactionId: String): ResponseEntity<String> {
        log.info("ConnectorController postCWDirectReversalRequest for transactionId = $transactionId")
        connectorService.reverseAuth(transactionId)
        return ResponseEntity(HttpStatus.OK)
    }

    /**
     * Create a `customer` at mindtree. In the checkout workflow, we create a customer to get their payment method, but
     * do not vault the customer.
     */
    @PostMapping("/customers/brand/{brand}")
    fun postCustomer(@PathVariable brand: String, @RequestBody customerRequest: CustomerRequest): CustomerResponse {
        log.info("Create customer for brand=$brand and request=$customerRequest")
        return connectorService.createCustomer(customerRequest)
    }
}
