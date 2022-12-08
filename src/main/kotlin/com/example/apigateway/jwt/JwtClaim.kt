package com.example.apigateway.jwt

data class JwtClaim(
    val username: String,
    val email: String,
    val userId: Long,
    val role : String
)
