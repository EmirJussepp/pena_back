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

fun Application.metodoPagoRoutes() {
    val database: Database = connectToMySql() ?: error("Error connecting to MySQL database")
    val metodoPagoRepository = MetodoPagoRepository(database)
    val createMetodoPagoHandler = CreateMetodoPagoHandler(metodoPagoRepository)

    routing {
        post("/metodos-pago") {
            try {
                val body = call.receive<CreateMetodoPagoCommand>()

                // Agrega un log para ver el cuerpo recibido
                println("Recibido: $body")

                createMetodoPagoHandler.handle(body)

                // Agrega un log para confirmar la inserción
                println("Método de pago insertado correctamente")

                call.respond(HttpStatusCode.Created, mapOf("message" to "Método de pago creado exitosamente"))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error en el servidor"))
                // Log para el error
                println("Error: ${e.message}")
            }
        }
        get("/metodopago"){
            try {
                val metodo = metodoPagoRepository.findAll()
                call.respond(metodo)
            } catch (e: Exception) {
                println("❌ Error al obtener metodos de pago: ${e.message}")
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al obtener metodo de pago"))
            }
        }
    }
}