package com.nextspringkart.gatewayservice.filter

import com.nextspringkart.gatewayservice.service.JwtService
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService
) : GlobalFilter, Ordered {

    private val excludedPaths = listOf(
        "/api/auth/",
        "/api/users/register",
        "/api/users/login",
        "/api/users/health",
        "/api/users/register",
        "/api/users/login",
        "/api/users/validate-token",
        "/api/products",
        "/api/categories",
        "/api/inventory",
        "/actuator"
    )

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val request = exchange.request
        val path = request.uri.path

        // Skip authentication for excluded paths
        if (excludedPaths.any { path.startsWith(it) }) {
            return chain.filter(exchange)
        }

        val authHeader = request.headers.getFirst(HttpHeaders.AUTHORIZATION)

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.response.statusCode = HttpStatus.UNAUTHORIZED
            return exchange.response.setComplete()
        }

        val token = authHeader.substring(7)

        return try {
            if (jwtService.validateToken(token)) {
                val userId = jwtService.extractUserId(token)
                val username = jwtService.extractUsername(token)
                val roles = jwtService.extractRoles(token)

                val mutatedRequest = request.mutate()
                    .header("X-User-Id", userId.toString())
                    .header("X-Username", username)
                    .header("X-User-Roles", roles.joinToString(","))
                    .build()

                val mutatedExchange = exchange.mutate()
                    .request(mutatedRequest)
                    .build()

                chain.filter(mutatedExchange)
            } else {
                exchange.response.statusCode = HttpStatus.UNAUTHORIZED
                exchange.response.setComplete()
            }
        } catch (_: Exception) {
            exchange.response.statusCode = HttpStatus.UNAUTHORIZED
            exchange.response.setComplete()
        }
    }

    override fun getOrder(): Int = -1
}