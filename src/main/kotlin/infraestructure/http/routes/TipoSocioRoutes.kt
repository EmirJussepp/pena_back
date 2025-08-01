package com.example.infraestructure.http.routes
import io.ktor.http.*
import com.example.infraestructure.persistence.TipoSocioBocaRepository
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.example.infraestructure.persistence.connectToMySql
import org.jetbrains.exposed.sql.Database
import com.example.application.command.tipoSocioBoca.CreateSocioBocaCommand
import com.example.application.command.tipoSocioBoca.UpdateTipoSocioBocaCommand

import com.example.application.commandhandler.tipoSocioBoca.CreateTipoSocioBocaCommandHandler
import com.example.application.commandhandler.tipoSocioBoca.UpdateTipoSocioBocaHandler

fun Application.socioBocaRoutes() {
    val database: Database = connectToMySql() ?: error("Error connecting to MySQL database")
    val tipoSocioBocaRepository = TipoSocioBocaRepository(database)
    val createTipoSocioBocaHandler = CreateTipoSocioBocaCommandHandler(tipoSocioBocaRepository)

    routing {
        // Ruta para crear un nuevo tipo de socio boca
        post("/sociosboca") {
            try {
                // Recibe el cuerpo del comando
                val body = call.receive<CreateSocioBocaCommand>()

                // Agrega un log para ver el cuerpo recibido
                println("Recibido: $body")

                // Llama al handler para manejar la creación
                createTipoSocioBocaHandler.handle(body)

                // Agrega un log para confirmar la inserción
                println("TipoSocioBoca insertado correctamente")

                // Responde con un mensaje de éxito
                call.respond(HttpStatusCode.Created, mapOf("message" to "Tipo de Socio Boca creado exitosamente"))
            } catch (e: IllegalArgumentException) {
                // Si hay un error de validación, responde con un BadRequest
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                // Si hay un error inesperado, responde con un InternalServerError
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error en el servidor"))
                // Log para el error
                println("Error: ${e.message}")
            }
        }
        patch("/sociosboca/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@patch call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID inválido"))

                val datos = call.receive<UpdateTipoSocioBocaCommand>()

                val command = UpdateTipoSocioBocaCommand(
                    tipoSocioBocaId = id,
                    nombre = datos.nombre
                )

                val handler = UpdateTipoSocioBocaHandler(tipoSocioBocaRepository)
                handler.handle(command)

                call.respond(HttpStatusCode.OK, "Tipo de socio actualizado con éxito")
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno al actualizar el tipo"))
                println("❌ Error: ${e.message}")
            }
        }

        get("sociosboca"){
            try {
                val tipoBoca = tipoSocioBocaRepository.obtenerTodos()

                // Log para depuración
                println("📤 Enviando lista de cobradores: $tipoBoca")

                call.respond(HttpStatusCode.OK, tipoBoca)

            }catch (e: IllegalArgumentException){
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }
        delete("/sociosboca/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID inválido"))
                tipoSocioBocaRepository.eliminar(id)

                call.respond(HttpStatusCode.OK, mapOf("message" to "Tipo de socio eliminado con éxito"))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno al eliminar el tipo"))
                println("❌ Error: ${e.message}")
            }
        }
    }
}
