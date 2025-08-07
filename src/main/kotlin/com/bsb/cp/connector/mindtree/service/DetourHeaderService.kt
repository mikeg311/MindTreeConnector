package com.bsb.cp.connector.mindtree.service

import com.bsb.common.headerforwarding.HeaderHolder
import org.springframework.stereotype.Component

@Component
class DetourHeaderService(private val headerHolder: HeaderHolder?) {

    fun getDetourHeaderValues(): List<String>? {
        return headerHolder?.let {
            it.headers["d2r-scenarios"]
        } ?: listOf()
    }

    fun isTestProdRequest(): Boolean {
        return getDetourHeaderValues()?.any { it.matches(Regex(".*testprod.*")) } ?: false
    }
}
