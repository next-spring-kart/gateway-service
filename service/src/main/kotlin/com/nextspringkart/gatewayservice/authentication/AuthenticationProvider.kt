package com.nextspringkart.gatewayservice.authentication

import com.nextspringkart.gatewayservice.dto.response.AuthResult

interface AuthenticationProvider {
    fun authenticate(token: String): AuthResult
}
