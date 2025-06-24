package com.nextspringkart.gatewayservice.authentication

import com.nextspringkart.gatewayservice.dto.response.AuthResult
import com.nextspringkart.gatewayservice.service.JwtService
import org.springframework.stereotype.Component

@Component
class JwtAuthenticationProvider(private val jwtService: JwtService) : AuthenticationProvider {

    override fun authenticate(token: String): AuthResult {
        return try {
            if (!jwtService.validateToken(token)) {
                return AuthResult(isAuthenticated = false)
            }

            val userId = jwtService.extractUserId(token)
            val username = jwtService.extractUsername(token)

            AuthResult(
                isAuthenticated = true,
                userId = userId,
                username = username
            )
        } catch (_: Exception) {
            AuthResult(isAuthenticated = false)
        }
    }
}