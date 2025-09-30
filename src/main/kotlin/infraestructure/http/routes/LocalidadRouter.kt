// src/main/kotlin/com/example/infraestructure/http/routes/LocalidadRoutes.kt
package com.example.infraestructure.http.routes

import com.example.domain.dto.LocalidadSyncRes
import com.example.domain.dto.LocalidadSyncReq
import com.example.infraestructure.persistence.LocalidadRepository
import com.example.infraestructure.persistence.connectToMySql
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import org.jetbrains.exposed.sql.Database

fun Route.localidadRoutes() {
    val database: Database = application.connectToMySql()
        ?: error("Error connecting to MySQL database")
    val localidadRepository = LocalidadRepository(database)

    route("/localidades") {

        // === Búsqueda para el autocomplete (BD) ===
        get {
            try {
                val q = call.request.queryParameters["q"]?.trim().orEmpty()
                val limit = (call.request.queryParameters["limit"] ?: "50")
                    .toInt().coerceIn(1, 200)

                val localidades = if (q.isBlank()) {
                    localidadRepository.obtenerTodos(limit)
                } else {
                    localidadRepository.buscar(q, limit)
                }

                call.respond(HttpStatusCode.OK, localidades)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Error en el servidor")
                )
            }
        }

        // === Alta automática (upsert) desde Georef/autocomplete ===
        post("sync") {
            try {
                val body = call.receive<LocalidadSyncReq>()
                if (body.nombre.isBlank() || body.provincia.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "nombre y provincia son obligatorios")
                    )
                    return@post
                }

                val id = localidadRepository.upsertAndGetId(
                    nombre = body.nombre,
                    provincia = body.provincia,
                    codigoPostal = body.codigoPostal ?: ""
                )

                call.respond(HttpStatusCode.OK, LocalidadSyncRes(localidadId = id))
            } catch (e: Exception) {
                println("Error /localidades/sync: ${e.message}")
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Error en el servidor")
                )
            }
        }
    }
}
