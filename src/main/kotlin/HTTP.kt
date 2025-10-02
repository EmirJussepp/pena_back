package com.example

import io.ktor.http.*

import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*



fun Application.configureHTTP() {
    install(CORS) {
        allowHost("boquensesocios.up.railway.app", schemes = listOf("https"))
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader("X-User-Id")

        allowCredentials = true
        allowNonSimpleContentTypes = true

        // Solo permitir todo si estoy en modo dev
        if (this@configureHTTP.environment.developmentMode) {
            anyHost()
        }
    }
}
