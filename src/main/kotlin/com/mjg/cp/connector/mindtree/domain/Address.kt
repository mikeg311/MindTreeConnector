package com.mjg.cp.connector.mindtree.domain

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class Address(
    val firstName: String? = null,
    val middleInitial: String? = null,
    val lastName: String? = null,
    val company: String? = null,
    val address1: String? = null,
    val address2: String? = null,
    val apartment: String? = null,
    val city: String? = null,
    val state: String? = null,
    val zipCode: String? = null,
    val country: String? = null
)
