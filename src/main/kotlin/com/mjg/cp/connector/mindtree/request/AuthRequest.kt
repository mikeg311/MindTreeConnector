package com.mjg.cp.connector.mindtree.request

import com.mjg.cp.connector.mindtree.domain.AccountType
import com.mjg.cp.connector.mindtree.domain.Address
import java.math.BigDecimal

data class AuthRequest(
    val transactionId: String? = null,
    val paymentMethodNonce: String,
    val paymentMethodToken: String? = null,
    val customerId: String? = null,
    val orderNumber: String? = null,
    val amount: BigDecimal,
    val accountType: AccountType,
    val address: Address? = null,
    val settle: Boolean? = null
)
