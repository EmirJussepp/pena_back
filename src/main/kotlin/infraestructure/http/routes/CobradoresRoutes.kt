package com.example.infraestructure.http.routes


import com.example.application.command.cobrador.ActualizarCobrador
import com.example.application.command.cobrador.CreateCobradorCommand
import com.example.application.commandhandler.cobrador.ActualizarCobradoresHandler
import com.example.application.commandhandler.cobrador.CreateCobradorHandler
import com.example.infraestructure.persistence.CobradorRepository
import com.example.infraestructure.persistence.connectToMySql
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database

fun Application.cobradorRoutes() {
    val database: Database = connectToMySql() ?: error("Error connecting to MySQL database")
    val cobradorRepository = CobradorRepository(database)
    val createCobradorHandler = CreateCobradorHandler(cobradorRepository)

    routing {
        post("/cobradores") {
            try {
                val body = call.receive<CreateCobradorCommand>()

                // Log para verificar el cuerpo recibido
                println("📥 Recibido: $body")

                createCobradorHandler.handle(body)

                // Log para confirmar la inserción
                println("✅ Cobrador insertado correctamente")

                call.respond(HttpStatusCode.Created, mapOf("message" to "Cobrador creado exitosamente"))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error en el servidor"))
                // Log para depuración
                println("❌ Error: ${e.message}")
            }
        }
        get("/cobradores") {
            try {
                val cobradores = cobradorRepository.obtenerTodos()

                // Log para depuración
                println("📤 Enviando lista de cobradores: $cobradores")

                call.respond(HttpStatusCode.OK, cobradores)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "No se pudo obtener la lista de cobradores"))
                println("❌ Error al obtener cobradores: ${e.message}")
            }
        }
        delete("/cobradores/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID inválido"))

                val existente = cobradorRepository.findById(id)
                    ?: return@delete call.respond(HttpStatusCode.NotFound, mapOf("error" to "No existe cobrador con id $id"))

                cobradorRepository.eliminarPorId(id)
                call.respond(HttpStatusCode.OK, mapOf("message" to "Cobrador eliminado con éxito"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al eliminar cobrador"))
                println("❌ Error al eliminar: ${e.message}")
            }
        }
        patch("/cobradores/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@patch call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID inválido"))

                val datos = call.receive<ActualizarCobrador>()
                val handler = ActualizarCobradoresHandler(cobradorRepository)
                handler.handle(id, datos)

                call.respond(HttpStatusCode.OK, mapOf("message" to "Cobrador actualizado con éxito"))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno al actualizar"))
                println("❌ Error: ${e.message}")
            }
        }



    }
}
