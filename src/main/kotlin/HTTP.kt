package com.example

import io.ktor.http.*

import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*



fun Application.configureHTTP() {
    install(CORS) {
        allowHost("boquensesocios.up.railway.app", schemes = listOf("https"))

        // Métodos que realmente usás
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)

        // Headers comunes + custom
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader("X-User-Id")

        allowCredentials = true
        allowNonSimpleContentTypes = true

        if (this@configureHTTP.environment.developmentMode) {
            anyHost() // Solo en dev
        }
    }
}
