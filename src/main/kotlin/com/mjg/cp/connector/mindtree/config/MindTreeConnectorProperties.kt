package com.mjg.cp.connector.mindtree.config

import com.mjg.cp.connector.mindtree.domain.Brand
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "settings.mindtree")

class MindTreeConnectorProperties {
    val loadTest: LoadTest = LoadTest()
    val vaulting: Vaulting = Vaulting()
    lateinit var merchantid: String
    lateinit var publickey: String
    lateinit var privatekey: String
    lateinit var environment: String
    val merchantAccountId: HashMap<Brand, String> = hashMapOf(
        Brand.COOL_COMPANY_DIVISION1 to "coolco1",
        Brand.COOL_COMPANY_DIVISION2 to "coolco2"
    )

    val uatRouting = UatRouting()

    class LoadTest {
        var enabled: Boolean = true
        lateinit var testNonce: String
        lateinit var dummyResponseToken: String
        var randomDelayFrom: Long = 1
        var randomDelayTo: Long = 1000

        override fun toString(): String {
            return "enabled=[$enabled] testNonce=[$testNonce] dummyResponseToken=[$dummyResponseToken] randomDelay=[$randomDelayFrom, $randomDelayTo]"
        }
    }

    /**
     * Route certain brands to UAT
     */
    class UatRouting {
        lateinit var merchantid: String
        lateinit var publickey: String
        lateinit var privatekey: String
        lateinit var environment: String
    }

    class Vaulting {
        var enabled: Boolean = false
    }
}
