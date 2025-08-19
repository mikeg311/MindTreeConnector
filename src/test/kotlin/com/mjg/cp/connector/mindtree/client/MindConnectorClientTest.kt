package com.mjg.cp.connector.mindtree.client

import com.mindtreegateway.mindtreeGateway
import com.mjg.cp.connector.mindtree.config.BrandHolder
import com.mjg.cp.connector.mindtree.config.MindTreeConnectorProperties
import com.mjg.cp.connector.mindtree.domain.Brand
import com.mjg.cp.connector.mindtree.service.DetourHeaderService
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class MindConnectorClientTest {

    val mindtreeGateway: mindtreeGateway = mockk()
    val brandHolder: BrandHolder = BrandHolder()
    val uatGateway: mindtreeGateway = mockk()
    val properties = MindTreeConnectorProperties()
    val detourHeaderService: DetourHeaderService = mockk()

    val mindtreeConnectorClient: MindtreeConnectorClient = MindtreeConnectorClient(
        mindtreeGateway = mindtreeGateway,
        properties = properties,
        brandHolder = brandHolder,
        mindtreeUatGateway = uatGateway,
        detourHeaderService = detourHeaderService
    )

    @BeforeEach
    fun setup() {
        clearAllMocks()
    }

    @Test
    fun `gets prod gateway when testprod header is not present`() {
        // given
        every { detourHeaderService.isTestProdRequest() } returns false

        // when
        val gateway = mindtreeConnectorClient.getGateway()

        // then
        gateway shouldBe mindtreeGateway
    }

    @Test
    fun `gets uat gateway when testprod header is present`() {
        // given
        every { detourHeaderService.isTestProdRequest() } returns true
        brandHolder.brand = Brand.COOL_COMPANY_DIVISION1

        // when
        val gateway = mindtreeConnectorClient.getGateway()

        // then
        gateway shouldBe uatGateway
    }

    @Nested
    inner class GetsMerchantAccountFromBrand {
        @BeforeEach
        fun setup() {
            brandHolder.setBrandFromString("coolco3")
            properties.merchantAccountId.set(Brand.COOL_COMPANY_DIVISION2, "coolUSD")
        }

        @Test
        fun `gets merchant account when no header`() {
            // given
            every { detourHeaderService.isTestProdRequest() } returns false

            val merchantAccount = mindtreeConnectorClient.getMerchantAccountId()

            merchantAccount shouldEqual "OPTUSD"
        }

        @Test
        fun `gets merchant account when there is testprod header`() {
            // given
            every { detourHeaderService.isTestProdRequest() } returns true

            val merchantAccount = mindtreeConnectorClient.getMerchantAccountId()

            merchantAccount shouldEqual "OPT"
        }
    }
}
