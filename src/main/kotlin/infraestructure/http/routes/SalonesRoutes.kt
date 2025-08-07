package com.example.infraestructure.http.routes

import com.example.application.command.Salones.ActualzarSalones
import com.example.application.command.Salones.CreateSalonCommand

import com.example.application.commandhandler.Salones.ActualizarSalonesHandler
import com.example.application.commandhandler.Salones.SalonCommandHandler

import com.example.infraestructure.persistence.SalonesRepository
import com.example.infraestructure.persistence.connectToMySql
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database


import io.ktor.server.response.*

fun Application.salonesRoutes() {
    val database: Database = connectToMySql() ?: error("Error connecting to MySQL database")
    val salonRepository = SalonesRepository(database)
    val salonCommandHandler = SalonCommandHandler(salonRepository)

    routing {
        post("/salones") {
            try {
                val body = call.receive<CreateSalonCommand>()

                // Log para verificar el cuerpo recibido
                println("📥 Recibido: $body")

                salonCommandHandler.handle(body)

                // Log para confirmar la inserción
                println("✅ Salón creado correctamente")

                call.respond(HttpStatusCode.Created, mapOf("message" to "Salón creado exitosamente"))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error en el servidor"))
                // Log para depuración
                println("❌ Error: ${e.message}")
            }
        }
        get("/salones") {
            try {
                val salones = salonRepository.findAll()

                // Log para depuración
                println("📤 Enviando lista de salones: $salones")

                call.respond(HttpStatusCode.OK, salones)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "No se pudo obtener la lista de salones"))
                println("❌ Error al obtener salones: ${e.message}")
            }
        }
        delete("/salones/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID inválido"))

                val existente = salonRepository.findById(id)
                    ?: return@delete call.respond(HttpStatusCode.NotFound, mapOf("error" to "No existe Salon con id $id"))

                salonRepository.delete(id)
                call.respond(HttpStatusCode.OK, mapOf("message" to "Salon eliminado con éxito"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al eliminar Salon"))
                println("❌ Error al eliminar: ${e.message}")
            }
        }
        patch("/salones/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@patch call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID inválido"))

                val datos = call.receive<ActualzarSalones>()
                val handler = ActualizarSalonesHandler(salonRepository)
                handler.handle(id, datos)

                call.respond(HttpStatusCode.OK, mapOf("message" to "Salon actualizado con éxito"))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno al actualizar"))
                println("❌ Error: ${e.message}")
            }
        }
    }

}
