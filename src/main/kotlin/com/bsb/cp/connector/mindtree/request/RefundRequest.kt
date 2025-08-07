package com.bsb.cp.connector.mindtree.request

import java.math.BigDecimal

data class RefundRequest(
    val transactionId: String,
    val amount: BigDecimal? = null
)
