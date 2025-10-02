package com.example


import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*


fun Application.configureSecurity() {
    install(Authentication) {
        jwt("auth-jwt") {
            realm = JwtConfig.realm
            verifier(
                com.auth0.jwt.JWT
                    .require(JwtConfig.algorithm())
                    .withIssuer(JwtConfig.issuer)
                    .withAudience(JwtConfig.audience)
                    .build()
            )
            validate { credential ->
                credential.payload.getClaim("sub")?.asInt()?.let {
                    JWTPrincipal(credential.payload)
                }
            }
        }
    }
}

