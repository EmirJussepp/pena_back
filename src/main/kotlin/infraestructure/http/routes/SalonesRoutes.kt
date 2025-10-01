// src/main/kotlin/com/example/infraestructure/http/routes/SalonesRoutes.kt
package com.example.infraestructure.http.routes

import com.example.application.command.Salones.ActualzarSalones // <- ¿tal vez ActualizarSalones?
import com.example.application.command.Salones.CreateSalonCommand
import com.example.application.commandhandler.Salones.ActualizarSalonesHandler
import com.example.application.commandhandler.Salones.SalonCommandHandler
import com.example.infraestructure.persistence.SalonesRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Rutas de salones recibiendo el repositorio ya inicializado.
 * No abre conexiones ni usa connectToMySql.
 */
fun Route.salonesRoutes(
    salonRepository: SalonesRepository
) {
    val salonCommandHandler = SalonCommandHandler(salonRepository)

    route("/salones") {

        // POST /salones
        post {
            try {
                val body = call.receive<CreateSalonCommand>()
                salonCommandHandler.handle(body)
                call.respond(HttpStatusCode.Created, mapOf("message" to "Salón creado exitosamente"))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Datos inválidos")))
            } catch (e: Exception) {
                call.application.log.error("Error creando salón", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error en el servidor"))
            }
        }

        // GET /salones
        get {
            try {
                val salones = salonRepository.findAll()
                call.respond(HttpStatusCode.OK, salones)
            } catch (e: Exception) {
                call.application.log.error("Error obteniendo salones", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "No se pudo obtener la lista de salones"))
            }
        }

        // DELETE /salones/{id}
        delete("{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID inválido"))

                val existente = salonRepository.findById(id)
                    ?: return@delete call.respond(HttpStatusCode.NotFound, mapOf("error" to "No existe salón con id $id"))

                salonRepository.delete(id)
                call.respond(HttpStatusCode.OK, mapOf("message" to "Salón eliminado con éxito"))
            } catch (e: Exception) {
                call.application.log.error("Error eliminando salón", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al eliminar salón"))
            }
        }

        // PATCH /salones/{id}
        patch("{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@patch call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID inválido"))

                val datos = call.receive<ActualzarSalones>() // si el DTO correcto es ActualizarSalones, cambialo aquí
                val handler = ActualizarSalonesHandler(salonRepository)
                handler.handle(id, datos)

                call.respond(HttpStatusCode.OK, mapOf("message" to "Salón actualizado con éxito"))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Datos inválidos")))
            } catch (e: Exception) {
                call.application.log.error("Error actualizando salón", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno al actualizar"))
            }
        }
    }
}
