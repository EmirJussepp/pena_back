package com.example

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureSecurity() {

    // CORS
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Delete)

        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)    // <-- necesario para Bearer tokens
        allowCredentials = true
        allowHeader(HttpHeaders.Authorization)
        allowHeader("X-User-Id") // tu header personalizado



        // Origenes permitidos
        anyHost() // ⚠️ Solo para desarrollo, en producción limitar a tu frontend real
        // Dev: permití tu front
        allowHost("localhost:8080", schemes = listOf("http"))

        allowNonSimpleContentTypes = true
    }

    // JWT Auth
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
                // Aceptamos si trae un "sub" (userId) válido
                credential.payload.getClaim("sub")?.asInt()?.let { JWTPrincipal(credential.payload) }
            }
        }
    }
}
