package com.bsb.cp.connector.mindtree.rest

import com.bsb.cp.connector.mindtree.domain.Brand
import org.springframework.core.convert.converter.Converter
import org.springframework.web.bind.annotation.ControllerAdvice

@ControllerAdvice
class StringToBrandEnumConverter : Converter<String, Brand> {
    override fun convert(source: String): Brand? {
        if (source.isEmpty()) {
            // It's an empty enum identifier: reset the enum value to null.
            return null
        }
        return Brand.getByPathParameterValue(source.trim { it <= ' ' })
    }
}
