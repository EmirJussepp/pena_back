// src/main/kotlin/com/example/infraestructure/http/routes/ViajeBomboneraRoutes.kt
package com.example.infraestructure.http.routes

import com.example.application.command.ViajesBombonera.ActualizarViajeBombonera
import com.example.application.command.ViajesBombonera.ViajeBomboneraCommand
import com.example.application.commandhandler.ViajeBomboneraCommandHandler
import com.example.application.commandhandler.ViajesBombonera.ActualizarViajeBomboneraHandler
import com.example.domain.dto.ViajeBomboneraDto
import com.example.domain.dto.ViajeBomboneraFiltroResponse
import com.example.infraestructure.persistence.ViajeBomboneraRepository
import com.example.infraestructure.persistence.connectToMySql
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database

fun Route.viajeBomboneraRoutes() {
    val database: Database = application.connectToMySql()
        ?: error("Error connecting to MySQL database")

    val viajeRepo        = ViajeBomboneraRepository(database)
    val crearHandler     = ViajeBomboneraCommandHandler(viajeRepo)
    val actualizarHandler= ActualizarViajeBomboneraHandler(viajeRepo)

    // Crear / actualizar un viaje
    post("/viajesBombonera") {
        try {
            val cmd = call.receive<ViajeBomboneraCommand>()
            println("Datos recibidos: $cmd")
            cmd.validate() // si tu comando tiene validate()
            val viajeGuardado = crearHandler.handle(cmd)
            call.respond(HttpStatusCode.Created, viajeGuardado)
        } catch (e: IllegalArgumentException) {
            call.respond(HttpStatusCode.BadRequest, e.message ?: "Datos inválidos")
        } catch (e: Exception) {
            println("Error /viajesBombonera: ${e.message}")
            call.respond(HttpStatusCode.InternalServerError, "Error en el servidor")
        }
    }

    // Filtro + totales
    get("/viajeBomboneraFiltro") {
        val filtro  = call.request.queryParameters["filtro"]
        val mes     = call.request.queryParameters["mes"]?.toIntOrNull()
        val pagina  = call.request.queryParameters["pagina"]?.toIntOrNull() ?: 1
        val tamanio = call.request.queryParameters["tamanioPagina"]?.toIntOrNull() ?: 3

        try {
            val viajes = viajeRepo.buscarViajesConTotales(filtro, mes, pagina, tamanio)
            val total  = viajeRepo.contarViajes(filtro, mes?.toString())

            val dtoList = viajes.map { v ->
                ViajeBomboneraDto(
                    viajeBomboneraId = v.viajeBomboneraId,
                    fechaViaje       = v.fechaViaje,
                    destino          = v.destino,
                    totalPasajeros   = v.totalPasajeros,
                    totalMonto       = v.totalMonto
                )
            }
            call.respond(ViajeBomboneraFiltroResponse(viajes = dtoList, total = total))
        } catch (e: Exception) {
            println("Error /viajeBomboneraFiltro: ${e.message}")
            call.respond(HttpStatusCode.InternalServerError, "Error al obtener viajes")
        }
    }

    // Actualizar viaje
    patch("/viajesBombonera/{id}") {
        val id = call.parameters["id"]?.toIntOrNull()
            ?: return@patch call.respond(HttpStatusCode.BadRequest, "ID inválido")
        try {
            val command = call.receive<ActualizarViajeBombonera>()
            val viajeActualizado = actualizarHandler.handle(command.copy(viajeBomboneraId = id))
            call.respond(HttpStatusCode.OK, viajeActualizado)
        } catch (e: IllegalArgumentException) {
            call.respond(HttpStatusCode.NotFound, e.message ?: "Viaje no encontrado")
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Error al actualizar viaje")
        }
    }
}
