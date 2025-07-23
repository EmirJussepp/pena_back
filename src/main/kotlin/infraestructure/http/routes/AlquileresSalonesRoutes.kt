package com.example.infraestructure.http.routes



import com.example.application.command.AlquilerSalon.ActualizarAlquilerCommand
import com.example.application.command.AlquilerSalon.CreateAlquilerSalonCommand
import com.example.application.commandhandler.AlquilerSalon.ActualizarAlquilerSalonHandler

import com.example.application.commandhandler.AlquilerSalon.CreateAlquilerSalonHandler
import com.example.infraestructure.persistence.AlquilerSalonesRepository
import com.example.infraestructure.persistence.connectToMySql
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database

fun Application.alquileresSalonesRoutes() {
    val database: Database = connectToMySql() ?: error("Error al conectar a la base de datos MySQL")
    val alquilerRepository = AlquilerSalonesRepository(database)
    val alquilerCommandHandler = CreateAlquilerSalonHandler(alquilerRepository)

    routing {
        post("/alquileres") {
            try {
                val body = call.receive<CreateAlquilerSalonCommand>()

                // Log para verificar el cuerpo recibido
                println("📥 Recibido: $body")

                alquilerCommandHandler.handle(body)

                // Log para confirmar la inserción
                println("✅ Alquiler registrado correctamente")

                call.respond(HttpStatusCode.Created, mapOf("message" to "Alquiler registrado exitosamente"))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error en el servidor"))
                println("❌ Error: ${e.message}")
            }
        }

        get("/alquileres") {
            try {
                val alquileres = alquilerRepository.findAll()

                // Log para depuración
                println("📤 Enviando lista de alquileres: $alquileres")

                call.respond(HttpStatusCode.OK, alquileres)
            } catch (e: Exception) {
                e.printStackTrace()  // Esto imprime el stacktrace completo en consola
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error en el servidor"))
            }

        }
        delete("/alquileres/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "ID inválido")

            alquilerRepository.eliminarPorId(id)
            call.respond(HttpStatusCode.OK, "Alquiler eliminado con éxito")
        }
        patch("/alquileres/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@patch call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID inválido"))

                val datos = call.receive<ActualizarAlquilerCommand>()
                println("Actualizar alquiler - ID URL: $id")
                println("Datos recibidos: $datos")

                if (datos.alquilerId != id) {
                    return@patch call.respond(HttpStatusCode.BadRequest, mapOf("error" to "El ID no coincide"))
                }

                val handler = ActualizarAlquilerSalonHandler(alquilerRepository)
                handler.handle(datos)

                call.respond(HttpStatusCode.OK, mapOf("message" to "Alquiler actualizado correctamente"))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno al actualizar alquiler"))
            }
        }



    }
}
