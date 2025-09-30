// src/main/kotlin/com/example/infraestructure/http/routes/SociosPenaRoutes.kt
package com.example.infraestructure.http.routes

import com.example.application.command.SociosPeña.ActualizarPrecioPeñaCommand
import com.example.application.command.SociosPeña.CreateSociosPeñaCommand
import com.example.application.commandhandler.SociosPeña.ActualizarPrecioHandler
import com.example.application.commandhandler.SociosPeña.CreateSociosPeñaCommandHandler
import com.example.infraestructure.persistence.SociosPeñaRepository
import com.example.infraestructure.persistence.connectToMySql
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database

// 👇 con ñ y extendiendo Route
fun Route.socioPeñaRoutes() {
    val database: Database = application.connectToMySql()
        ?: error("Error connecting to MySQL database")

    val sociosPeñaRepository   = SociosPeñaRepository(database)
    val createSocioPeñaHandler = CreateSociosPeñaCommandHandler(sociosPeñaRepository)
    val actualizarPrecioHandler = ActualizarPrecioHandler(sociosPeñaRepository)

    // dejo el path sin ñ para evitar problemas de URL y proxies
    route("/sociospena") {

        // Crear tipo socio peña
        post {
            try {
                val body = call.receive<CreateSociosPeñaCommand>()
                println("Recibido: $body")
                createSocioPeñaHandler.handle(body)
                println("SocioPeña insertado correctamente")
                call.respond(HttpStatusCode.Created, mapOf("message" to "Socio de Peña creado exitosamente"))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                println("Error: ${e.message}")
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error en el servidor"))
            }
        }

        // Actualizar precio
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
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                println("❌ Error: ${e.message}")
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno al actualizar el tipo"))
            }
        }

        // Listar
        get {
            try {
                val lista = sociosPeñaRepository.obtenerTodos()
                println("📤 Enviando lista de tipos de socio peña: $lista")
                call.respond(HttpStatusCode.OK, lista)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                println("❌ Error al obtener tipos de socio peña: ${e.message}")
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error en el servidor"))
            }
        }

        // Eliminar
        delete("{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID inválido"))

                sociosPeñaRepository.eliminar(id)
                call.respond(HttpStatusCode.OK, mapOf("message" to "Tipo de socio eliminado con éxito"))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                println("❌ Error al eliminar tipo socio peña: ${e.message}")
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno al eliminar el tipo"))
            }
        }
    }
}
