package com.bsb.cp.connector.mindtree

import com.mindtreegateway.mindtreeGateway
import com.bsb.cp.connector.mindtree.config.MindTreeConnectorProperties
import com.bsb.cp.connector.mindtree.domain.Brand
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotBeNull
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import java.math.BigDecimal

@RunWith(SpringRunner::class)
@SpringBootTest
class MindtreeConnectorApplicationTests {

    @Autowired
    lateinit var properties: MindTreeConnectorProperties

    @Autowired
    lateinit var mindtreeGateway: mindtreeGateway

    @Autowired
    var mindtreeUatGateway: mindtreeGateway? = null

    @Test
    fun contextLoads() {
        properties.environment.shouldNotBeNull()
        properties.merchantid.shouldNotBeNull()
        properties.privatekey.shouldNotBeNull()
        properties.publickey.shouldNotBeNull()
    }

    @Test
    fun `loads uat merchant account id`() {
        properties.merchantAccountId[Brand.COOL_COMPANY_DIVISION1] shouldEqual "coolco1"
        properties.merchantAccountId[Brand.COOL_COMPANY_DIVISION1] shouldEqual "coolco2"
    }

    @Test
    fun `loads uat config for brand split`() {
        mindtreeUatGateway?.shouldNotBeNull()
    }

    @Ignore("use to test existing transactions by looking up by id")
    @Test
    fun `searches transaction`() {
        mindtreeGateway.transaction().refund("3ahhc8yw", BigDecimal("1.00"))
    }
}
