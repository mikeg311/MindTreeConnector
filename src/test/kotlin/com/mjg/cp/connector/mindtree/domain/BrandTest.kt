package com.mjg.cp.connector.mindtree.domain

import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeTrue
import org.amshove.kluent.shouldEqual
import org.junit.Test

class BrandTest {

    @Test
    fun `test valid brands`() {
        // when
        Brand.validForService() shouldEqual listOf(Brand.COOL_COMPANY_DIVISION1, Brand.COOL_COMPANY_DIVISION2)
    }

    @Test
    fun `test brand is supported`() {
        Brand.COOL_COMPANY_DIVISION1.isSupportedInService().shouldBeTrue()
        Brand.COOL_COMPANY_DIVISION2.isSupportedInService().shouldBeTrue()

        (Brand.values().toList() - Brand.validForService()).forEach {
            it.isSupportedInService().shouldBeFalse()
        }
    }
}
