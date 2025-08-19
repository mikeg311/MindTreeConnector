package com.mjg.cp.connector.mindtree.config

import com.mjg.cp.connector.mindtree.domain.Brand
import io.mockk.mockk
import io.mockk.verify
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.mock.web.MockHttpServletRequest
import javax.servlet.FilterChain

internal class BrandFilterTest {
    val brandHolder = BrandHolder()
    val filter = BrandFilter(brandHolder)

    @ParameterizedTest
    @ValueSource(
        strings = [
            "/mindtree/brand/coolco2",
            "/mindtree/brand/coolco3/foo",
            "/brand/coolco2/",
            "/brand/coolco3"
        ]
    )
    fun `sets brand from path`(path: String) {
        val chain: FilterChain = mockk(relaxUnitFun = true)
        val request = MockHttpServletRequest("GET", "http://localhost")
        request.servletPath = path

        // when
        filter.doFilter(request, null, chain)

        // then
        brandHolder.brand shouldEqual Brand.COOL_COMPANY_DIVISION1
        verify { chain.doFilter(request, null) }
    }

    @Test
    fun `ignores brand processing on paths that don't contain brand`() {
        val chain: FilterChain = mockk(relaxUnitFun = true)
        val request = MockHttpServletRequest("GET", "http://localhost")
        request.servletPath = "/actuator/info"

        // when
        filter.doFilter(request, null, chain)

        // then
        brandHolder.brand.shouldBeNull()
        verify { chain.doFilter(request, null) }
    }

    @Test
    fun `ignores exceptions on brand`() {
        val chain: FilterChain = mockk(relaxUnitFun = true)
        val request = MockHttpServletRequest("GET", "http://localhost")
        request.servletPath = "/brand/not-a-brand"

        // when
        filter.doFilter(request, null, chain)

        // then
        brandHolder.brand.shouldBeNull()
        verify { chain.doFilter(request, null) }
    }
}
