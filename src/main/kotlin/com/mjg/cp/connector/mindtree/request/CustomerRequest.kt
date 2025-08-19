package com.mjg.cp.connector.mindtree.request

data class CustomerRequest(
    val paymentMethodNonce: String,
    val mappingToken: String
)
