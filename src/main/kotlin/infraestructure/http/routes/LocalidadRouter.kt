package com.example.infraestructure.http.routes

import com.example.application.command.localidad.CreateLocalidadCommand
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

import com.example.application.commandhandler.localidad.CreateLocalidadHandler
import com.example.infraestructure.persistence.LocalidadRepository
import com.example.infraestructure.persistence.connectToMySql
import org.jetbrains.exposed.sql.Database
import kotlinx.serialization.Serializable // <-- agregado

// === DTOs para /localidades/sync ===
@Serializable
data class LocalidadSyncReq(
    val nombre: String,
    val provincia: String,
    val codigoPostal: String? = ""
)

@Serializable
data class LocalidadSyncRes(val localidadId: Int)

fun Application.localidadRoutes() {
    val database: Database = connectToMySql() ?: error("Error connecting to MySQL database")
    val localidadRepository = LocalidadRepository(database)
    val createLocalidadHandler = CreateLocalidadHandler(localidadRepository)

    routing {

        // Crear localidad “manual” (si lo usás en otro lado)
        post("/localidades") {
            try {
                val body = call.receive<CreateLocalidadCommand>()
                println("Recibido: $body")
                createLocalidadHandler.handle(body)
                println("Localidad insertada correctamente")
                call.respond(HttpStatusCode.Created, mapOf("message" to "Localidad creada exitosamente"))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                println("Error: ${e.message}")
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error en el servidor"))
            }
        }

        // Búsqueda para el autocomplete (BD)
        get("/localidades") {
            try {
                val q = call.request.queryParameters["q"]?.trim().orEmpty()
                val limit = (call.request.queryParameters["limit"] ?: "50").toInt().coerceIn(1, 200)

                val localidades = if (q.isBlank()) {
                    localidadRepository.obtenerTodos(limit)
                } else {
                    localidadRepository.buscar(q, limit)
                }

                call.respond(HttpStatusCode.OK, localidades)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error en el servidor"))
            }
        }

        // === NUEVO: alta on-demand desde Georef ===
        // El front llama a este endpoint sólo si el item elegido no existe en tu BD
        post("/localidades/sync") {
            try {
                val body = call.receive<LocalidadSyncReq>()
                if (body.nombre.isBlank() || body.provincia.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "nombre y provincia son obligatorios"))
                    return@post
                }

                // upsert y devolver el id final
                val id = localidadRepository.upsertAndGetId(
                    nombre = body.nombre,
                    provincia = body.provincia,
                    codigoPostal = body.codigoPostal ?: ""
                )


                call.respond(HttpStatusCode.OK, LocalidadSyncRes(localidadId = id))
            } catch (e: Exception) {
                println("Error /localidades/sync: ${e.message}")
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error en el servidor"))
            }
        }
    }
}
