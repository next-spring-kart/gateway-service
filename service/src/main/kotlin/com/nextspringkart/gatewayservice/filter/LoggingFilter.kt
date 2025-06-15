
package com.nextspringkart.gatewayservice.filter

import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class LoggingFilter : AbstractGatewayFilterFactory<LoggingFilter.Config>() {

    private val logger = LoggerFactory.getLogger(LoggingFilter::class.java)

    data class Config(val message: String = "Logging Filter")

    override fun apply(config: Config): GatewayFilter {
        return GatewayFilter { exchange, chain ->
            val request = exchange.request
            val startTime = System.currentTimeMillis()

            logger.info("Request: {} {} from {}",
                request.method,
                request.uri,
                request.remoteAddress?.address?.hostAddress ?: "unknown"
            )

            chain.filter(exchange).then(
                Mono.fromRunnable {
                    val endTime = System.currentTimeMillis()
                    val duration = endTime - startTime

                    logger.info("Response: {} {} - Status: {} - Duration: {}ms",
                        request.method,
                        request.uri,
                        exchange.response.statusCode,
                        duration
                    )
                }
            )
        }
    }

    override fun getConfigClass(): Class<Config> = Config::class.java
}
