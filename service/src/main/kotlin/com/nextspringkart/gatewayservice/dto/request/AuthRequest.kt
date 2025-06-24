package com.nextspringkart.gatewayservice.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class AuthRequest(
    @field:Email("Invalid email format")
    @field:NotBlank("Email cannot be blank")
    val email: String?,

    @field:NotBlank("Username cannot be blank")
    val userName: String?,

    @field:NotBlank("UserId cannot be blank")
    val userId: String?,

    @field:NotNull(message = "Roles cannot be null")
    @field:NotEmpty(message = "Roles list cannot be empty")
    val roles: List<String>?
)