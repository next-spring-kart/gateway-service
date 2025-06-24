package com.nextspringkart.gatewayservice.controller

import com.nextspringkart.gatewayservice.dto.request.AuthRequest
import com.nextspringkart.gatewayservice.service.JwtService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/jwt")
class JwtController(private val jwtService: JwtService) {

    @PostMapping("/get-token")
    fun getJwtToken(@Valid @RequestBody request: AuthRequest): String {
        val claims = mutableMapOf<String, Any>(
            "userId" to request.userId!!,
            "userName" to request.userName!!,
            "email" to request.email!!,
            "roles" to request.roles!!
        )
        return jwtService.createToken(claims, request.userId)
    }
}
