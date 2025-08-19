package com.example.infraestructure.http.routes
import com.example.application.command.ViajesBombonera.ActualizarViajeBombonera
import com.example.application.commandhandler.ViajeBomboneraCommandHandler
import com.example.application.command.ViajesBombonera.ViajeBomboneraCommand
import com.example.application.commandhandler.ViajesBombonera.ActualizarViajeBomboneraHandler
import com.example.domain.dto.ViajeBomboneraDto
import com.example.domain.dto.ViajeBomboneraFiltroResponse
import com.example.infraestructure.persistence.ViajeBomboneraRepository
import com.example.infraestructure.persistence.connectToMySql
import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database

fun Application.viajeBomboneraRoutes() {
    val database: Database = connectToMySql() ?: error("Error connecting to MySQL database")
    // Conectamos al repositorio de ViajeBombonera
    val viajeBomboneraRepository = ViajeBomboneraRepository(database)
    val viajeBomboneraHandler = ViajeBomboneraCommandHandler(viajeBomboneraRepository)
    val actualizarViajeBomboneraHandler= ActualizarViajeBomboneraHandler(viajeBomboneraRepository)

    routing {
        // Ruta para crear o actualizar un viaje
        post("/viajesBombonera") {
            try {
                val viajeBomboneraCommand = call.receive<ViajeBomboneraCommand>()
                // Log para verificar los datos recibidos
                println("Datos recibidos: $viajeBomboneraCommand")

                // Validar el comando antes de guardar
                viajeBomboneraCommand.validate()

                // Guardar el viaje en la base de datos (crear o actualizar)
                val viajeGuardado = viajeBomboneraHandler.handle(viajeBomboneraCommand)



                // Responder con el mensaje de confirmación
                call.respond(HttpStatusCode.Created,viajeGuardado)
            } catch (e: Exception) {
                println("Error: ${e.message}")
                call.respond(HttpStatusCode.BadRequest, "Error: ${e.message}")
            }
        }
        get("viajeBombonera"){

                try {
                    val viajesBombonera = viajeBomboneraRepository.findAll()
                    call.respond(viajesBombonera)
                } catch (e: Exception) {
                    println("❌ Error al obtener viaje: ${e.message}")
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al obtener viaje"))
                }

        }
        get("/viajeBomboneraFiltro") {
            val filtro = call.request.queryParameters["filtro"]
            val mes = call.request.queryParameters["mes"]
            val pagina = call.request.queryParameters["pagina"]?.toIntOrNull() ?: 1
            val tamanio = call.request.queryParameters["tamanioPagina"]?.toIntOrNull() ?: 3

            val viajes = viajeBomboneraRepository.buscarViajes(filtro, mes, pagina, tamanio)
            val total = viajeBomboneraRepository.contarViajes(filtro, mes)

            val dtoList = viajes.map { v ->
                ViajeBomboneraDto(
                    viajeBomboneraId = v.viajeBomboneraId,
                    fechaViaje = v.fechaViaje.toString(), // formatear si necesitas
                    destino = v.destino
                )
            }

            call.respond(
                ViajeBomboneraFiltroResponse(
                    viajes = dtoList,
                    total = total
                )
            )
        }
        patch("/viajesBombonera/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@patch call.respond(HttpStatusCode.BadRequest, "ID inválido")

            val command = call.receive<ActualizarViajeBombonera>()

            try {
                val viajeActualizado = actualizarViajeBomboneraHandler.handle(command.copy(viajeBomboneraId = id))
                call.respond(HttpStatusCode.OK, viajeActualizado)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.NotFound, e.message ?: "Viaje no encontrado")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error al actualizar viaje")
            }
        }



    }
}


