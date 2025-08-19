package com.mjg.cp.connector.mindtree.config

import com.mjg.cp.connector.mindtree.domain.Brand
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest

/**
 * Look for brand/{brand} and set a request level variable for the brand so we can use it in the client
 * Doing this so we can split between mindtree uat and prod based on brand.
 */
@Component
@Order(1) // make sure this runs before spring boot filters
class BrandFilter(val brandHolder: BrandHolder) : Filter {
    private val log = LoggerFactory.getLogger(BrandFilter::class.java)

    override fun doFilter(request: ServletRequest?, response: ServletResponse?, chain: FilterChain?) {
        if (request is HttpServletRequest) {
            val path = request.servletPath
            val regex = Regex(""".*/brand/([A-Za-z]*).*""")
            val matchResult = regex.matchEntire(path)
            matchResult?.let {
                try {
                    val brandStr = matchResult.groupValues[1]
                    log.debug("setting brand to $brandStr")
                    brandHolder.setBrandFromString(brandStr)
                } catch (ex: Exception) {
                    log.warn("error capturing brand from input, $path")
                    // continue processing so we don't blow up against prod requests
                }
            }
        }
        chain?.doFilter(request, response)
    }
}

@Component
@RequestScope // make this bean live for one web request, instead of default singleton lifecycle
class BrandHolder {
    var brand: Brand? = null

    fun setBrandFromString(str: String) {
        brand = Brand.getByPathParameterValue(str)
    }
}
