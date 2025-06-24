package com.nextspringkart.gatewayservice.service

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.MacAlgorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.SecretKey

@Service
class JwtService(
    @Value("\${jwt.secret}")
    private val secret: String,

    @Value("\${jwt.expiration}")
    private val expiration: Long,

    private val algorithm: MacAlgorithm = Jwts.SIG.HS256,
    private val key: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())
) {

    fun createToken(claims: MutableMap<String, Any>, subject: String): String {
        val now = Date()
        val expiryDate = Date(now.time + expiration)

        return Jwts.builder()
            .claims()
            .add(claims)
            .subject(subject)
            .issuedAt(now)
            .expiration(expiryDate)
            .and()
            .signWith(key, algorithm)
            .compact()
    }

    fun validateToken(token: String) = !isTokenExpired(token)


    private fun <T> extractClaim(token: String, claimsResolver: (Claims) -> T): T {
        val claims = extractAllClaims(token)
        return claimsResolver(claims)
    }

    private fun extractAllClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    fun extractRoles(token: String): List<String> {
        val claims = extractAllClaims(token)
        val roles = claims["roles"]
        return when (roles) {
            is List<*> -> roles.filterIsInstance<String>()
            is String -> roles.split(",")
            else -> emptyList()
        }
    }

    fun extractUserId(token: String) = extractClaim(token) { it["userId"] as String }
    fun extractRole(token: String) = extractClaim(token) { it["role"] as String }
    fun extractUsername(token: String): String? = extractClaim(token, Claims::getSubject)
    fun getExpirationTime() = expiration
    private fun isTokenExpired(token: String) = extractExpiration(token).before(Date())
    private fun extractExpiration(token: String) = extractClaim(token, Claims::getExpiration)
}
