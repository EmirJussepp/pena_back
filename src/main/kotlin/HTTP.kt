package com.example

import io.ktor.http.*

import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*



fun Application.configureHTTP() {
    install(CORS) {
        allowHost("boquensesocios.up.railway.app", schemes = listOf("https"))
        allowHost("www.boquensesocios.up.railway.app", schemes = listOf("https"))

        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)

        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader("X-User-Id")
        allowHeader(HttpHeaders.Accept)          // <- útil
        allowHeader("X-Requested-With")          // <- si lo usás

        allowCredentials = true
        allowNonSimpleContentTypes = true

        if (this@configureHTTP.environment.developmentMode) {
            anyHost() // Solo en dev
        }
    }
}
