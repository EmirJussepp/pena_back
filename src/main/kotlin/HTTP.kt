package com.example

import io.ktor.http.*

import io.ktor.server.application.*

import io.ktor.server.plugins.cors.routing.*


fun Application.configureHTTP() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader("MyCustomHeader")

        allowHeader(HttpHeaders.Authorization)
        allowHeader("X-User-Id") // tu header personalizado

        allowCredentials = true
        allowNonSimpleContentTypes = true

        // Origenes permitidos
        anyHost() // ⚠️ Solo para desarrollo, en producción limitar a tu frontend real
    }
}
