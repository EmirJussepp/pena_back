// src/main/kotlin/com/example/infraestructure/http/routes/AlquileresSalonesRoutes.kt
package com.example.infraestructure.http.routes

import com.example.application.command.AlquilerSalon.ActualizarAlquilerCommand
import com.example.application.command.AlquilerSalon.CreateAlquilerSalonCommand
import com.example.application.commandhandler.AlquilerSalon.ActualizarAlquilerSalonHandler
import com.example.application.commandhandler.AlquilerSalon.CreateAlquilerSalonHandler
import com.example.infraestructure.persistence.AlquilerSalonesRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Define las rutas de alquileres **recibiendo** el repositorio ya inicializado.
 * No abre conexiones nuevas ni usa connectToMySql.
 */
fun Route.alquileresSalonesRoutes(
    alquilerRepository: AlquilerSalonesRepository
) {
    val crearHandler = CreateAlquilerSalonHandler(alquilerRepository)

    route("/alquileres") {

        post {
            try {
                val body = call.receive<CreateAlquilerSalonCommand>()
                crearHandler.handle(body)
                call.respond(HttpStatusCode.Created, mapOf("message" to "Alquiler registrado exitosamente"))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Datos inválidos")))
            } catch (e: Exception) {
                call.application.log.error("Error creando alquiler", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error en el servidor"))
            }
        }

        get {
            try {
                val alquileres = alquilerRepository.findAll()
                call.respond(HttpStatusCode.OK, alquileres)
            } catch (e: Exception) {
                call.application.log.error("Error listando alquileres", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error en el servidor"))
            }
        }

        delete("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID inválido"))

            try {
                alquilerRepository.eliminarPorId(id)
                call.respond(HttpStatusCode.OK, mapOf("message" to "Alquiler eliminado con éxito"))
            } catch (e: Exception) {
                call.application.log.error("Error eliminando alquiler $id", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error en el servidor"))
            }
        }

        patch("{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@patch call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID inválido"))

                val datos = call.receive<ActualizarAlquilerCommand>()

                if (datos.alquilerId != id) {
                    return@patch call.respond(HttpStatusCode.BadRequest, mapOf("error" to "El ID no coincide"))
                }

                val actualizarHandler = ActualizarAlquilerSalonHandler(alquilerRepository)
                actualizarHandler.handle(datos)

                call.respond(HttpStatusCode.OK, mapOf("message" to "Alquiler actualizado correctamente"))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Datos inválidos")))
            } catch (e: Exception) {
                call.application.log.error("Error actualizando alquiler", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno al actualizar alquiler"))
            }
        }
    }
}
