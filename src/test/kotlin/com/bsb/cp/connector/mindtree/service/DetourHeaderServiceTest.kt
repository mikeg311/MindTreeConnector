package com.bsb.cp.connector.mindtree.service

import com.bsb.common.headerforwarding.HeaderHolder
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeTrue
import org.amshove.kluent.shouldContain
import org.junit.jupiter.api.Test

internal class DetourHeaderServiceTest {

    val headerHolder = HeaderHolder().apply {
        headers.add("d2r-scenarios", "_testprod_")
        headers.add("d2r-scenarios", "another test")
    }
    val detour = DetourHeaderService(headerHolder)

    @Test
    fun `gets headers`() {
        val result = detour.getDetourHeaderValues()!!
        result.size shouldBe 2
        result shouldContain "_testprod_"
        result shouldContain "another test"
    }

    @Test
    fun `handles null headers`() {
        headerHolder.headers.clear()

        val result = detour.getDetourHeaderValues()!!
        result.size shouldBe 0
    }

    @Test
    fun `when testprod header is present`() {
        detour.isTestProdRequest().shouldBeTrue()
    }

    @Test
    fun `when header is not present`() {
        headerHolder.headers.clear()
        detour.isTestProdRequest().shouldBeFalse()
    }

    @Test
    fun `when testprod header value is not present`() {
        headerHolder.headers.clear()
        headerHolder.headers.add("d2r-scenarios", "a different header")
        detour.isTestProdRequest().shouldBeFalse()
    }
}
