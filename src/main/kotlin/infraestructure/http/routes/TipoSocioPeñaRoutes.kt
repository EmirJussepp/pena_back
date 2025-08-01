package com.example.infraestructure.http.routes

import com.example.application.command.SociosPeña.ActualizarPrecioPeñaCommand
import io.ktor.http.*
import com.example.infraestructure.persistence.SociosPeñaRepository
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.example.infraestructure.persistence.connectToMySql
import org.jetbrains.exposed.sql.Database
import com.example.application.command.SociosPeña.CreateSociosPeñaCommand
import com.example.application.commandhandler.SociosPeña.ActualizarPrecioHandler
import com.example.application.commandhandler.SociosPeña.CreateSociosPeñaCommandHandler // ✅ Corrige el import

fun Application.socioPeñaRoutes() {
    val database: Database = connectToMySql() ?: error("Error connecting to MySQL database")
    val sociosPeñaRepository = SociosPeñaRepository(database)
    val createSocioPeñaHandler = CreateSociosPeñaCommandHandler(sociosPeñaRepository)  // ✅ Usa el handler correcto
    val actualizarPrecioHandler = ActualizarPrecioHandler(sociosPeñaRepository)
    routing {
        // Ruta para crear un nuevo socio de peña
        post("/sociospeña") {
            try {
                // Recibe el cuerpo del comando
                val body = call.receive<CreateSociosPeñaCommand>()

                // Agrega un log para ver el cuerpo recibido
                println("Recibido: $body")

                // Llama al handler para manejar la creación
                createSocioPeñaHandler.handle(body)

                // Agrega un log para confirmar la inserción
                println("SocioPeña insertado correctamente")

                // Responde con un mensaje de éxito
                call.respond(HttpStatusCode.Created, mapOf("message" to "Socio de Peña creado exitosamente"))
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
        patch("/sociospeña/precio/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@patch call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID inválido"))

                val datos = call.receive<ActualizarPrecioPeñaCommand>()

                val command = ActualizarPrecioPeñaCommand(
                    tipoSocioPeñaId = id,
                    nombre = datos.nombre,
                    precio = datos.precio
                )

                actualizarPrecioHandler.handle(command)  // Usar la instancia ya creada
                call.respond(HttpStatusCode.OK, "Tipo de socio actualizado con éxito")
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno al actualizar el tipo"))
                println("❌ Error: ${e.message}")
            }
        }

        get("sociosPeña"){
            try {
                val sociosPeña = sociosPeñaRepository.obtenerTodos()

                // Log para depuración
                println("📤 Enviando lista de cobradores: $sociosPeña")

                call.respond(HttpStatusCode.OK, sociosPeña)

            }catch (e: IllegalArgumentException){
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }
        delete("/sociospena/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID inválido"))

                sociosPeñaRepository.eliminar(id)

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
