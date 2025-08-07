package com.bsb.cp.connector.mindtree.domain

import com.bsb.cp.connector.mindtree.domain.Brand.Companion.validForService

enum class Brand constructor(val brandName: String) {
    COOL_COMPANY_DIVISION1("cool-co-1"),
    COOL_COMPANY_DIVISION2("cool-co-2");

    override fun toString(): String {
        return brandName
    }

    companion object {
        fun getByPathParameterValue(name: String): Brand? = Brand.values().find {
            it.brandName == name
        }
        fun validForService() = listOf(
            Brand.COOL_COMPANY_DIVISION1,
            Brand.COOL_COMPANY_DIVISION2
        )
    }
}
fun Brand.isSupportedInService() = validForService().contains(this)
