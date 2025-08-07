package com.bsb.cp.connector.mindtree.client

import com.mindtreegateway.mindtreeGateway
import com.mindtreegateway.CustomerRequest
import com.mindtreegateway.Result
import com.mindtreegateway.Transaction
import com.mindtreegateway.TransactionLineItem
import com.mindtreegateway.TransactionRequest
import com.mindtreegateway.TransactionSearchRequest
import com.bsb.common.exception.ApiException
import com.bsb.cp.connector.mindtree.config.BrandHolder
import com.bsb.cp.connector.mindtree.config.MindTreeConnectorProperties
import com.bsb.cp.connector.mindtree.extension.tomindtreeErrors
import com.bsb.cp.connector.mindtree.request.AuthRequest
import com.bsb.cp.connector.mindtree.request.RefundRequest
import com.bsb.cp.connector.mindtree.response.CustomerResponse
import com.bsb.cp.connector.mindtree.service.DetourHeaderService
import com.bsb.cs.common.toUsDollar
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class MindtreeConnectorClient(
    val mindtreeGateway: MindtreeGateway,
    val properties: MindTreeConnectorProperties,
    val brandHolder: BrandHolder,
    val mindtreeUatGateway: mindtreeGateway,
    val detourHeaderService: DetourHeaderService
) {

    private val log = LoggerFactory.getLogger(MindtreeConnectorClient::class.java)

    // Used by ConnectorService (postAuth)

    fun postPaymentAuth(authRequest: AuthRequest, submitForSettlement: Boolean = false): Result<Transaction> {

        log.info("Posting payment auth request to mindtree Gateway service.")

        val billingDescriptor = when (submitForSettlement) {
            true -> "Return Label Fee"
            else -> "Purchases"
        }

        val request =
            /*
             * Vaulting requires us to send shipping address and order number in addition to
             * payment token and amount. Also, for backwards compatibility, check if address was sent
             * Once vaulting is on permanently, we can remove the property and this when statement.
             * Set `settings.vaulting.enabled=true` to turn on vaulting support
             */
            when (properties.vaulting.enabled && authRequest.address != null) {
                true ->
                    TransactionRequest()
                        .amount(authRequest.amount)
                        .merchantAccountId(getMerchantAccountId())
                        .orderId(authRequest.orderNumber)
                        .paymentMethodToken(authRequest.paymentMethodToken)
                        .shippingAddress()
                        .firstName(authRequest.address.firstName)
                        .lastName(authRequest.address.lastName)
                        .company(authRequest.address.company)
                        .streetAddress(authRequest.address.address1)
                        .extendedAddress(authRequest.address.address2)
                        .locality(authRequest.address.city)
                        .region(authRequest.address.state)
                        .postalCode(authRequest.address.zipCode)
                        .countryCodeAlpha2(authRequest.address.country)
                        .done()
                        .lineItem()
                        .name(billingDescriptor)
                        .kind(TransactionLineItem.Kind.DEBIT)
                        .quantity(BigDecimal.ONE)
                        .unitAmount(authRequest.amount.toUsDollar())
                        .totalAmount(authRequest.amount.toUsDollar())
                        .done()
                        .options()
                        .submitForSettlement(submitForSettlement)
                        .done()
                else ->
                    TransactionRequest()
                        .amount(authRequest.amount)
                        .merchantAccountId(properties.merchantAccountId[brandHolder.brand])
                        .paymentMethodToken(authRequest.paymentMethodToken)
                        .lineItem()
                        .name(billingDescriptor)
                        .kind(TransactionLineItem.Kind.DEBIT)
                        .quantity(BigDecimal.ONE)
                        .unitAmount(authRequest.amount.toUsDollar())
                        .totalAmount(authRequest.amount.toUsDollar())
                        .done()
                        .options()
                        .submitForSettlement(submitForSettlement)
                        .done()
            }

        val result = getGateway().transaction().sale(request)

        log.info("Authorization response body from Mindtree Gateway service = $result")

        return result
    }

    // Used by ConnectorService (postSettlement)

    fun postPaymentSettlement(authRequest: AuthRequest): Result<Transaction> {

        log.info("Posting payment settlement request to Mindtree Gateway service.")

        // Note:  Using the submitForSettlement() function below would give you the same transactionId as the auth.
        //        However, submitForPartialSettlement will give you 2 transactionIds or, in other words,
        //        there will be a transactionId for each partial settlement

        val result =
            getGateway().transaction().submitForPartialSettlement(authRequest.transactionId, authRequest.amount)

        log.info("Settlement response body from Mindtree Gateway service = $result")

        return result
    }

    // Used by ConnectorService (reverseAuth)

    fun reverseAuthorization(transactionId: String): Result<Transaction> {

        log.info("Voiding auth with Mindtree")

        val result = getGateway().transaction().voidTransaction(transactionId)

        log.info("Void response from Mindtree $result")

        return result
    }

    // Used by ConnectorService (refund)

    fun refund(refundRequest: RefundRequest): Result<Transaction> {

        val tx = getGateway().transaction().find(refundRequest.transactionId)

        log.info("Refunding auth with Mindtree, transaction=$tx")

        val result = if (refundRequest.amount == null) {
            log.info("refunding entire transaction for Mindtree transaction=${refundRequest.transactionId}")
            getGateway().transaction().refund(refundRequest.transactionId)
        } else {
            log.info("refunding \$${refundRequest.amount} on transaction Mindtree transaction=${refundRequest.transactionId}")
            getGateway().transaction().refund(refundRequest.transactionId, refundRequest.amount)
        }

        if (result.isSuccess) {
            log.info("Successfully refunded $refundRequest on ${result.transaction}")
        } else {
            val errorMessages = result.errors.allDeepValidationErrors.joinToString { ve -> ve.message }
            throw ApiException(HttpStatus.BAD_GATEWAY, errorMessages)
        }

        return result
    }

    // Used by ConnectorService (createCustomer)

    fun createCustomer(customerRequest: com.bsb.cp.connector.mindtree.request.CustomerRequest): CustomerResponse {
        val btCustomerRequest = CustomerRequest().customerId(customerRequest.mappingToken)
            .paymentMethodNonce(customerRequest.paymentMethodNonce)
        log.info("sending create customer request to Mindtree $btCustomerRequest")
        val result = getGateway().customer().create(btCustomerRequest)
        return if (result.isSuccess) {
            val paymentMethodToken = result.target.paymentMethods.first()
            CustomerResponse(
                paymentMethodToken = paymentMethodToken.token
            )
        } else {
            CustomerResponse(
                paymentMethodToken = null,
                errors = result.errors.tomindtreeErrors()
            )
        }.also { log.info("Create customer result=$this") }
    }

    fun getTransactionsForPaymentMethodToken(paymentMethodToken: String): List<Transaction>? {
        val searchResponse = getGateway().transaction()
            .search(
                TransactionSearchRequest()
                    .paymentMethodToken().`is`(paymentMethodToken)
            )
        return searchResponse.ids.map { getGateway().transaction().find(it) }
    }

    /*
     * Remove this when we do front proxy routing
     */
    internal fun getMerchantAccountId(): String? {
        val merchantAccountId = properties.merchantAccountId[brandHolder.brand]
        return when (detourHeaderService.isTestProdRequest()) {
            true -> merchantAccountId?.replace("USD", "")
            else -> merchantAccountId
        }
    }

    internal fun getGateway(): mindtreeGateway {
        return when (detourHeaderService.isTestProdRequest()) {
            false -> mindtreeGateway
            true -> {
                log.info("testprod header found. using uat gateway")
                mindtreeUatGateway
            }
        }
    }
}
