package com.example.apigateway.filter

import com.auth0.jwt.interfaces.DecodedJWT
import com.example.apigateway.config.WhiteListHeader
import com.example.apigateway.exception.NotFoundAuthorizationException
import com.example.apigateway.jwt.JWTProperties
import com.example.apigateway.jwt.JwtUtils
import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Component

@Component
class AuthServerFilter(
    private val jwtProperties: JWTProperties
) : AbstractGatewayFilterFactory<AuthServerFilter.Config>(Config::class.java){

    private val log = LoggerFactory.getLogger(AuthServerFilter::class.java)

    data class Config (
        var role : String ?= null,
        val map : Map<String, Long> = hashMapOf()
    )

    override fun apply(config: Config?): GatewayFilter {
        return GatewayFilter { exchange, chain ->
            val req = exchange.request
            val res = exchange.response

            val token: String = req.headers["Authorization"]?.get(0)?.split(" ")?.get(1)
                ?: kotlin.run{
                    req.mutate().header("X-Authorization-role", "GUEST")
                    return@GatewayFilter chain.filter(exchange)
                }
            val decodeToken = JwtUtils.decodeToken(
                token = token,
                issuer = jwtProperties.issuer,
                secret = jwtProperties.secret,
            )
            getClaims(decodeToken).apply {
                addUserInfo(this, req)
            }
            destroyHeader(req)
            chain.filter(exchange)
        }
    }
    private fun getClaims(decodeToken : DecodedJWT)
            = with(decodeToken.claims) {
        val userId = get("userId")!!.asLong().toString()
        val role = get("role")!!.asString()
        val email = get("email")!!.asString()
        arrayOf(userId, role, email)
    }
    private fun addUserInfo(claims : Array<String>, req : ServerHttpRequest) {
        req.mutate()
            .headers {
                it.add("X-Authorization-Id", claims[0])
                it.add("X-Authorization-role", claims[1])
                it.add("X-Authorization-email", claims[2])
            }
    }

    private fun destroyHeader(req : ServerHttpRequest) {
        req.headers.forEach {
            if (!WhiteListHeader.whiteList.contains(it.key)) {
                req.mutate().header(it.key, null)
            }
        }

    }

}