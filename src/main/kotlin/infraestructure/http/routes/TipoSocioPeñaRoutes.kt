// src/main/kotlin/com/example/infraestructure/http/routes/SociosPenaRoutes.kt
package com.example.infraestructure.http.routes

import com.example.application.command.SociosPeña.ActualizarPrecioPeñaCommand
import com.example.application.command.SociosPeña.CreateSociosPeñaCommand
import com.example.application.commandhandler.SociosPeña.ActualizarPrecioHandler
import com.example.application.commandhandler.SociosPeña.CreateSociosPeñaCommandHandler
import com.example.infraestructure.persistence.SociosPeñaRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Rutas de tipos de socio de peña recibiendo el repositorio ya inicializado.
 * No abre conexiones ni usa connectToMySql.
 */
fun Route.socioPeñaRoutes(
    sociosPeñaRepository: SociosPeñaRepository
) {
    val createSocioPeñaHandler = CreateSociosPeñaCommandHandler(sociosPeñaRepository)
    val actualizarPrecioHandler = ActualizarPrecioHandler(sociosPeñaRepository)

    // Path sin ñ para evitar problemas en URL/proxies
    route("/sociospena") {

        // POST /sociospena
        post {
            try {
                val body = call.receive<CreateSociosPeñaCommand>()
                createSocioPeñaHandler.handle(body)
                call.respond(HttpStatusCode.Created, mapOf("message" to "Socio de Peña creado exitosamente"))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Datos inválidos")))
            } catch (e: Exception) {
                call.application.log.error("Error creando tipo de socio peña", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error en el servidor"))
            }
        }

        // PATCH /sociospena/precio/{id}
        patch("/precio/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@patch call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID inválido"))

                val datos = call.receive<ActualizarPrecioPeñaCommand>()
                val command = ActualizarPrecioPeñaCommand(
                    tipoSocioPeñaId = id,
                    nombre = datos.nombre,
                    precio = datos.precio
                )
                actualizarPrecioHandler.handle(command)
                call.respond(HttpStatusCode.OK, mapOf("message" to "Tipo de socio actualizado con éxito"))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Datos inválidos")))
            } catch (e: Exception) {
                call.application.log.error("Error actualizando precio socio peña", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno al actualizar el tipo"))
            }
        }

        // GET /sociospena
        get {
            try {
                val lista = sociosPeñaRepository.obtenerTodos()
                call.respond(HttpStatusCode.OK, lista)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Parámetros inválidos")))
            } catch (e: Exception) {
                call.application.log.error("Error listando tipos de socio peña", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error en el servidor"))
            }
        }

        // DELETE /sociospena/{id}
        delete("{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID inválido"))

                sociosPeñaRepository.eliminar(id)
                call.respond(HttpStatusCode.OK, mapOf("message" to "Tipo de socio eliminado con éxito"))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Datos inválidos")))
            } catch (e: Exception) {
                call.application.log.error("Error eliminando tipo socio peña", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno al eliminar el tipo"))
            }
        }
    }
}
