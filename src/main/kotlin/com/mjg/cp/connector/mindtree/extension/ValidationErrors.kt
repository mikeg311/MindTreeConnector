package com.mjg.cp.connector.mindtree.extension

import com.mindtreegateway.ValidationErrors
import com.mjg.cp.connector.mindtree.response.mindtreeError

// Convert mindtree's Validation

fun ValidationErrors.tomindtreeErrors(): List<mindtreeError> =
    this.allDeepValidationErrors.map {
        mindtreeError(
            message = it.message,
            code = it.code.code,
            codeName = it.code.name,
            attribute = it.attribute
        )
    }
