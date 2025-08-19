package com.mjg.cp.connector.mindtree.domain

enum class TransactionStatus constructor(val status: String) {
    APPROVED("APPROVED"),
    AUTHORIZED("AUTHORIZED"),
    AUTHORIZATION_EXPIRED("AUTHORIZATION_EXPIRED"),
    AUTHORIZING("AUTHORIZING"),
    ERROR("ERROR"),
    FAILED("FAILED"),
    GATEWAY_REJECTED("GATEWAY_REJECTED"),
    OPEN("OPEN"),
    PARTIALLY_SETTLED("PARTIALLY_SETTLED"),
    PROCESSOR_DECLINED("PROCESSOR_DECLINED"),
    DECLINED("DECLINED"),
    REVERSED("REVERSED"),
    REFUNDED("REFUNDED"),
    SENT("SENT"),
    SETTLED("SETTLED"),
    SETTLEMENT_CONFIRMED("SETTLEMENT_CONFIRMED"),
    SETTLEMENT_DECLINED("SETTLEMENT_DECLINED"),
    SETTLEMENT_PENDING("SETTLEMENT_PENDING"),
    SETTLING("SETTLING"),
    SUBMITTED_FOR_SETTLEMENT("SUBMITTED_FOR_SETTLEMENT"),
    UNKNOWN("UNKNOWN"),
    UNRECOGNIZED("UNRECOGNIZED"),
    VOIDED("VOIDED");
    override fun toString(): String {
        return status
    }
    companion object {
        fun getByStatus(status: String): TransactionStatus {
            return TransactionStatus.values().find { it.status.uppercase() == status.uppercase() } ?: UNKNOWN
        }
    }
}
