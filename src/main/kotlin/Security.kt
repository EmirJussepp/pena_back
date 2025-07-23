package com.example

import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.event.*

fun Application.configureSecurity() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Patch)      // <--- agregar PUT
        // <--- agregar PUT
        allowMethod(HttpMethod.Delete)   // <--- agregar DELET
        allowHeader(HttpHeaders.ContentType)
        allowCredentials = true

        // Permitir solo el origen de tu frontend (ajusta el puerto si cambia)
        allowHost("localhost:8080", schemes = listOf("http"))
        // O para permitir cualquier origen (no recomendado para producción)
        // anyHost()
    }
}
