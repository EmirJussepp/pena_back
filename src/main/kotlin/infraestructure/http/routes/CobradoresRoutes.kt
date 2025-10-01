// src/main/kotlin/com/example/infraestructure/http/routes/CobradorRoutes.kt
package com.example.infraestructure.http.routes

import com.example.application.command.cobrador.ActualizarCobrador
import com.example.application.command.cobrador.CreateCobradorCommand
import com.example.application.commandhandler.cobrador.ActualizarCobradoresHandler
import com.example.application.commandhandler.cobrador.CreateCobradorHandler
import com.example.infraestructure.persistence.CobradorRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Define las rutas de cobradores recibiendo dependencias ya inicializadas.
 * No abre conexiones ni usa connectToMySql.
 */
fun Route.cobradorRoutes(
    cobradorRepository: CobradorRepository
) {
    val createCobradorHandler = CreateCobradorHandler(cobradorRepository)

    route("/cobradores") {

        post {
            try {
                val body = call.receive<CreateCobradorCommand>()
                createCobradorHandler.handle(body)
                call.respond(HttpStatusCode.Created, mapOf("message" to "Cobrador creado exitosamente"))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Datos inválidos")))
            } catch (e: Exception) {
                call.application.log.error("Error creando cobrador", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error en el servidor"))
            }
        }

        get {
            try {
                val cobradores = cobradorRepository.obtenerTodos()
                call.respond(HttpStatusCode.OK, cobradores)
            } catch (e: Exception) {
                call.application.log.error("Error obteniendo cobradores", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "No se pudo obtener la lista"))
            }
        }

        delete("{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID inválido"))

                val existente = cobradorRepository.findById(id)
                    ?: return@delete call.respond(HttpStatusCode.NotFound, mapOf("error" to "No existe id $id"))

                cobradorRepository.eliminarPorId(id)
                call.respond(HttpStatusCode.OK, mapOf("message" to "Cobrador eliminado con éxito"))
            } catch (e: Exception) {
                call.application.log.error("Error eliminando cobrador", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al eliminar cobrador"))
            }
        }

        patch("{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@patch call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID inválido"))

                val datos = call.receive<ActualizarCobrador>()
                val actualizarHandler = ActualizarCobradoresHandler(cobradorRepository)
                actualizarHandler.handle(id, datos)

                call.respond(HttpStatusCode.OK, mapOf("message" to "Cobrador actualizado con éxito"))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Datos inválidos")))
            } catch (e: Exception) {
                call.application.log.error("Error actualizando cobrador", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno al actualizar"))
            }
        }
    }
}
