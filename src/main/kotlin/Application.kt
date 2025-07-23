package com.example


import com.example.domain.entities.BigDecimalSerializer
import io.ktor.server.application.*
import com.example.infraestructure.persistence.configureDatabases
import io.ktor.serialization.kotlinx.json.*

import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import java.math.BigDecimal

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
                encodeDefaults = true
                serializersModule = SerializersModule {
                    contextual(BigDecimal::class, BigDecimalSerializer)
                }
            }
        )
    }

    configureSecurity()
    configureDatabases()
    configureRouting() // Importante: Llamar a las rutas aquí
}