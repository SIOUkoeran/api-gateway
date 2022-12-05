package com.example.apigateway.filter

import com.auth0.jwt.interfaces.DecodedJWT
import com.example.apigateway.exception.NotFoundAuthorizationException
import com.example.apigateway.jwt.JWTProperties
import com.example.apigateway.jwt.JwtUtils
import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Component

@Component
class GlobalCustomFilter(
    private val jwtProperties : JWTProperties,
) :
    AbstractGatewayFilterFactory<GlobalCustomFilter.Config>(Config::class.java) {

    data class Config (
        val role : String,
        val map : Map<String, Long> = hashMapOf()
            )

    private val log = LoggerFactory.getLogger(GlobalCustomFilter::class.java)
    override fun apply(config: Config): GatewayFilter {
        return GatewayFilter { exchange, chain ->
            val req: ServerHttpRequest = exchange.request
            val res = exchange.response

            val token: String = req.headers["Authorization"]?.get(0)?.split(" ")?.get(1)
                ?: throw NotFoundAuthorizationException()
            val decodeToken = JwtUtils.decodeToken(
                token = token,
                issuer = jwtProperties.issuer,
                secret = jwtProperties.secret,
                )

            val claims = getClaims(decodeToken).apply {
                addUserInfo(this, req)
            }
            chain.filter(exchange)
        }
     }

    private fun getClaims(decodeToken : DecodedJWT)
        = with(decodeToken.claims) {
            val userId = get("userId")!!.asString()
            log.info("request api userId : [${userId}]")
            val username = get("username")!!.asString()
            val email = get("email")!!.asString()
            arrayOf(userId, username, email)
        }

    private fun addUserInfo(claims : Array<String>, req : ServerHttpRequest) {
        req.mutate()
            .headers {
                it.add("X-Authorization-Id", claims[0])
                it.add("X-Authorization-username", claims[1])
                it.add("X-Authorization-email", claims[2])
            }
    }

}