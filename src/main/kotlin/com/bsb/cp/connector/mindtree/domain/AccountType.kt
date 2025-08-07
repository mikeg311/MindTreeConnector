package com.bsb.cp.connector.mindtree.domain

enum class AccountType constructor(val type: String) {
    PalPay("palpay");

    companion object {
        fun getByPathParameterValue(name: String): AccountType? = AccountType.values().find { it.type.equals(name) }
    }
}
