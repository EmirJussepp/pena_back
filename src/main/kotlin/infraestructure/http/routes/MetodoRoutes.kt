// src/main/kotlin/com/example/infraestructure/http/routes/MetodoPagoRoutes.kt
package com.example.infraestructure.http.routes

import com.example.application.command.metodoPago.CreateMetodoPagoCommand
import com.example.application.commandhandler.metodoPago.CreateMetodoPagoHandler
import com.example.infraestructure.persistence.MetodoPagoRepository
import com.example.infraestructure.persistence.connectToMySql
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database

fun Route.metodoPagoRoutes() {
    val database: Database = application.connectToMySql()
        ?: error("Error connecting to MySQL database")

    val metodoPagoRepository = MetodoPagoRepository(database)
    val createMetodoPagoHandler = CreateMetodoPagoHandler(metodoPagoRepository)

    route("/metodos-pago") {

        // POST /metodos-pago
        post {
            try {
                val body = call.receive<CreateMetodoPagoCommand>()
                println("📥 Recibido: $body")

                createMetodoPagoHandler.handle(body)
                println("✅ Método de pago insertado correctamente")

                call.respond(HttpStatusCode.Created, mapOf("message" to "Método de pago creado exitosamente"))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                println("❌ Error: ${e.message}")
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error en el servidor"))
            }
        }

        // GET /metodos-pago
        get {
            try {
                val metodos = metodoPagoRepository.findAll()
                call.respond(HttpStatusCode.OK, metodos)
            } catch (e: Exception) {
                println("❌ Error al obtener métodos de pago: ${e.message}")
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Error al obtener métodos de pago")
                )
            }
        }
    }
}
