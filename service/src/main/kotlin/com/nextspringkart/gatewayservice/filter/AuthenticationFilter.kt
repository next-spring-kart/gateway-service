package com.nextspringkart.gatewayservice.filter

import com.nextspringkart.gatewayservice.authentication.AuthenticationProvider
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class AuthenticationFilter(
    private val authProvider: AuthenticationProvider,
) : AbstractGatewayFilterFactory<AuthenticationFilter.Config>() {

    data class Config(val message: String = "Authentication Filter")

    override fun apply(config: Config): GatewayFilter {
        return GatewayFilter { exchange, chain ->
            val request = exchange.request
            val authHeader = request.headers.getFirst(HttpHeaders.AUTHORIZATION)

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                exchange.response.statusCode = HttpStatus.UNAUTHORIZED
                return@GatewayFilter exchange.response.setComplete()
            }

            val token = authHeader.substring(7)

            try {
                val authResult = authProvider.authenticate(token)
                if (!authResult.isAuthenticated)
                    throw IllegalArgumentException("Invalid token")
                chain.filter(exchange)
            } catch (_: Exception) {
                exchange.response.statusCode = HttpStatus.UNAUTHORIZED
                return@GatewayFilter exchange.response.setComplete()
            }
        }
    }

    override fun getConfigClass(): Class<Config> = Config::class.java
}
