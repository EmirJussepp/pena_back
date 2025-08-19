package com.example

import io.ktor.server.application.*

import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Application.configureSerialization() {
//    install(ContentNegotiation) {
//        gson {
//            }
//
//        json()
//    }
    routing {
        get("/json/gson") {
                call.respond(mapOf("hello" to "world"))
            }
        get("/json/kotlinx-serialization") {
                call.respond(mapOf("hello" to "world"))
            }
    }
}
