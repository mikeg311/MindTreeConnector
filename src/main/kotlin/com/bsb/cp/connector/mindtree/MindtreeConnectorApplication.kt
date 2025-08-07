package com.bsb.cp.connector.mindtree

import com.mindtreegateway.mindtreeGateway
import com.mindtreegateway.Environment
import com.bsb.cp.connector.mindtree.config.MindTreeConnectorProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
@EnableConfigurationProperties(MindTreeConnectorProperties::class)
class BsbCpMindtreeConnectorApplication(val properties: MindTreeConnectorProperties) {

    @Bean
    fun mindtreeUatGateway() = mindtreeGateway(
        properties.uatRouting.environment,
        properties.uatRouting.merchantid,
        properties.uatRouting.publickey,
        properties.uatRouting.privatekey
    )

    @Bean
    fun mindtreeGateway() = mindtreeGateway(
        Environment.parseEnvironment(properties.environment),
        properties.merchantid,
        properties.publickey,
        properties.privatekey,
    )
}

fun main(args: Array<String>) {
    runApplication<BsbCpmindtreeConnectorApplication>(*args)
}
