package com.example.apigateway.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.auth0.jwt.interfaces.DecodedJWT
import com.example.apigateway.exception.InvalidVertificationException
import com.example.apigateway.exception.JWTokenExpiredException
import com.example.apigateway.jwt.JWTProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

object JwtUtils {

    private val log : Logger = LoggerFactory.getLogger(JwtUtils::class.java)

    fun createToken(jwtClaim: JwtClaim, jwtProperties: JWTProperties, type : String) =
        with(jwtProperties){
            val expiredTime  = when(type) {
                "accessToken" -> jwtProperties.expired
                "refreshToken" -> jwtProperties.refreshExpired
                else -> jwtProperties.refreshExpired
            }
            return@with JWT.create()
                .withIssuer(issuer)
                .withIssuedAt(Date())
                .withExpiresAt(Date(Date().time + expiredTime * 100))
                .withSubject(subject)
                .withClaim("email", jwtClaim.email)
                .withClaim("userId", jwtClaim.userId)
                .withClaim("username", jwtClaim.username)
                .withClaim("role", jwtClaim.role)
                .sign(Algorithm.HMAC256(secret))
        }


    fun isExpiredToken(token : String, jwtProperties: JWTProperties) : Boolean {
        with(jwtProperties){
            try {
                decodeToken(token, secret, issuer)
            }catch (e : JWTokenExpiredException) {
                return true
            }
            return false
        }
    }

    fun decodeToken(token : String, secret : String, issuer : String) : DecodedJWT {
        val algorithm = Algorithm.HMAC256(secret)
        val verifier = JWT.require(algorithm)
            .withIssuer(issuer)
            .build()
        try{
            return verifier.verify(token)
        }
        catch (e : TokenExpiredException) {
            throw JWTokenExpiredException()
        }
        catch (e : JWTVerificationException) {
            throw InvalidVertificationException(e.message!!)
        }
    }
}