package com.bsb.cp.connector.mindtree.extension

import com.mindtreegateway.ValidationError
import com.mindtreegateway.ValidationErrorCode
import com.mindtreegateway.ValidationErrors
import com.bsb.cp.connector.mindtree.response.mindtreeError
import org.amshove.kluent.shouldEqual
import org.junit.Test

class ValidationErrorsKtTest {

    @Test
    fun `converts validation errors to custom json object`() {
        // given
        val validationErrors: ValidationErrors = ValidationErrors().apply {
            addError(
                ValidationError(
                    "a",
                    ValidationErrorCode.DISPUTE_EVIDENCE_CATEGORY_NOT_FOR_REASON_CODE,
                    "m"
                )
            )
        }
        val expected = listOf(mindtreeError("m", "95713", "DISPUTE_EVIDENCE_CATEGORY_NOT_FOR_REASON_CODE", "a"))

        // when
        val actual = validationErrors.tomindtreeErrors()

        // then
        actual shouldEqual expected
    }

    @Test
    fun `converts validation errors to custom json object empty list`() {
        // given
        val validationErrors = ValidationErrors()

        // when
        val actual = validationErrors.tomindtreeErrors()

        // then
        actual shouldEqual listOf()
    }
}
