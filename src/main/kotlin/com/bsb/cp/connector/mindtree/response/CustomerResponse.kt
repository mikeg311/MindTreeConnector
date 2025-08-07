package com.bsb.cp.connector.mindtree.response

data class CustomerResponse(
    val paymentMethodToken: String?,
    val errors: List<mindtreeError>? = null
)
