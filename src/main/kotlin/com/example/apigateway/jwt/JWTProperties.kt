package com.example.apigateway.jwt

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jwt")
data class JWTProperties (
    val issuer : String,
    val expired : Long,
    val secret : String,
    val subject : String,
    val refreshExpired : Long,
)