// src/main/kotlin/com/example/infraestructure/http/routes/SocioBocaRoutes.kt
package com.example.infraestructure.http.routes

import com.example.application.command.tipoSocioBoca.CreateSocioBocaCommand
import com.example.application.command.tipoSocioBoca.UpdateTipoSocioBocaCommand
import com.example.application.commandhandler.tipoSocioBoca.CreateTipoSocioBocaCommandHandler
import com.example.application.commandhandler.tipoSocioBoca.UpdateTipoSocioBocaHandler
import com.example.infraestructure.persistence.TipoSocioBocaRepository
import com.example.infraestructure.persistence.connectToMySql
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database

fun Route.socioBocaRoutes() {
    val database: Database = application.connectToMySql()
        ?: error("Error connecting to MySQL database")

    val tipoSocioBocaRepository   = TipoSocioBocaRepository(database)
    val createTipoSocioBocaHandler = CreateTipoSocioBocaCommandHandler(tipoSocioBocaRepository)
    val updateTipoSocioBocaHandler = UpdateTipoSocioBocaHandler(tipoSocioBocaRepository)

    route("/sociosboca") {

        // Crear un nuevo tipo de socio boca
        post {
            try {
                val body = call.receive<CreateSocioBocaCommand>()
                println("Recibido: $body")
                createTipoSocioBocaHandler.handle(body)
                println("TipoSocioBoca insertado correctamente")
                call.respond(HttpStatusCode.Created, mapOf("message" to "Tipo de Socio Boca creado exitosamente"))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                println("Error: ${e.message}")
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error en el servidor"))
            }
        }

        // Actualizar
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
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                println("❌ Error: ${e.message}")
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno al actualizar el tipo"))
            }
        }

        // Listar
        get {
            try {
                val tipos = tipoSocioBocaRepository.obtenerTodos()
                println("📤 Enviando lista de tipos de socio boca: $tipos")
                call.respond(HttpStatusCode.OK, tipos)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                println("❌ Error al obtener tipos de socio boca: ${e.message}")
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error en el servidor"))
            }
        }

        // Eliminar
        delete("{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID inválido"))

                tipoSocioBocaRepository.eliminar(id)
                call.respond(HttpStatusCode.OK, mapOf("message" to "Tipo de socio eliminado con éxito"))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                println("❌ Error al eliminar tipo socio boca: ${e.message}")
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno al eliminar el tipo"))
            }
        }
    }
}
