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

fun Application.localidadRoutes() {
    val database: Database = connectToMySql() ?: error("Error connecting to MySQL database")
    val localidadRepository = LocalidadRepository(database)
    val createLocalidadHandler = CreateLocalidadHandler(localidadRepository)

    routing {
        post("/localidades") {
            try {
                val body = call.receive<CreateLocalidadCommand>()

                // Agrega un log para ver el cuerpo recibido
                println("Recibido: $body")

                createLocalidadHandler.handle(body)

                // Agrega un log para confirmar la inserción
                println("Localidad insertada correctamente")

                call.respond(HttpStatusCode.Created, mapOf("message" to "Localidad creada exitosamente"))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error en el servidor"))
                // Log para el error
                println("Error: ${e.message}")
            }
        }
        get("/localidades"){
            try {
                val localidades = localidadRepository.obtenerTodos()

                // Log para depuración
                println("📤 Enviando lista de cobradores: $localidades")

                call.respond(HttpStatusCode.OK, localidades)

            }catch (e:IllegalArgumentException){
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }
    }
}

