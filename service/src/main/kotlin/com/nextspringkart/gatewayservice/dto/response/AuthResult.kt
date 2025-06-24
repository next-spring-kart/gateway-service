package com.nextspringkart.gatewayservice.dto.response

data class AuthResult(
    val isAuthenticated: Boolean,
    val userId: String? = null,
    val username: String? = null,
    val attributes: Map<String, String> = emptyMap()
)