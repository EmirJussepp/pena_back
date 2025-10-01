// src/main/kotlin/com/example/infraestructure/http/routes/SocioBocaRoutes.kt
package com.example.infraestructure.http.routes

import com.example.application.command.tipoSocioBoca.CreateSocioBocaCommand
import com.example.application.command.tipoSocioBoca.UpdateTipoSocioBocaCommand
import com.example.application.commandhandler.tipoSocioBoca.CreateTipoSocioBocaCommandHandler
import com.example.application.commandhandler.tipoSocioBoca.UpdateTipoSocioBocaHandler
import com.example.infraestructure.persistence.TipoSocioBocaRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Rutas de tipos de socio Boca recibiendo el repositorio ya inicializado.
 * No abre conexiones ni usa connectToMySql.
 */
fun Route.socioBocaRoutes(
    tipoSocioBocaRepository: TipoSocioBocaRepository
) {
    val createTipoSocioBocaHandler = CreateTipoSocioBocaCommandHandler(tipoSocioBocaRepository)
    val updateTipoSocioBocaHandler = UpdateTipoSocioBocaHandler(tipoSocioBocaRepository)

    route("/sociosboca") {

        // POST /sociosboca
        post {
            try {
                val body = call.receive<CreateSocioBocaCommand>()
                createTipoSocioBocaHandler.handle(body)
                call.respond(HttpStatusCode.Created, mapOf("message" to "Tipo de Socio Boca creado exitosamente"))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Datos inválidos")))
            } catch (e: Exception) {
                call.application.log.error("Error creando tipo socio Boca", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error en el servidor"))
            }
        }

        // PATCH /sociosboca/{id}
        patch("{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@patch call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID inválido"))

                val datos = call.receive<UpdateTipoSocioBocaCommand>()
                val cmd = UpdateTipoSocioBocaCommand(
                    tipoSocioBocaId = id,
                    nombre = datos.nombre
                )
                updateTipoSocioBocaHandler.handle(cmd)
                call.respond(HttpStatusCode.OK, mapOf("message" to "Tipo de socio actualizado con éxito"))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Datos inválidos")))
            } catch (e: Exception) {
                call.application.log.error("Error actualizando tipo socio Boca", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno al actualizar el tipo"))
            }
        }

        // GET /sociosboca
        get {
            try {
                val tipos = tipoSocioBocaRepository.obtenerTodos()
                call.respond(HttpStatusCode.OK, tipos)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Parámetros inválidos")))
            } catch (e: Exception) {
                call.application.log.error("Error listando tipos socio Boca", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error en el servidor"))
            }
        }

        // DELETE /sociosboca/{id}
        delete("{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID inválido"))

                tipoSocioBocaRepository.eliminar(id)
                call.respond(HttpStatusCode.OK, mapOf("message" to "Tipo de socio eliminado con éxito"))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Datos inválidos")))
            } catch (e: Exception) {
                call.application.log.error("Error eliminando tipo socio Boca", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno al eliminar el tipo"))
            }
        }
    }
}
