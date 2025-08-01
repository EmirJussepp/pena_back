package com.example.infraestructure.http.routes

import com.example.application.command.Movimientos.CrearMovimientoCommand
import com.example.application.commandhandler.Movimientos.MovimientoCommandHandler
import com.example.domain.contracts.MovimientoContract
import com.example.domain.entities.BalanceResponse
import com.example.domain.entities.BalanceSemanalResponse
import com.example.domain.entities.Movimiento
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.server.routing.*
import java.time.LocalDate
import java.time.YearMonth

fun Application.movimientoRoutes(
    movimientoRepository: MovimientoContract,
    movimientoCommandHandler: MovimientoCommandHandler
) {
    routing {
        // Crear un nuevo movimiento
        post("/movimientos") {
            try {
                val command = call.receive<CrearMovimientoCommand>()
                val movimiento = movimientoCommandHandler.handle(command)
                call.respond(HttpStatusCode.Created, movimiento)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al crear movimiento"))
            }
        }

        // Obtener un movimiento por ID
        get("/movimientos/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID inválido"))

                val movimiento = movimientoRepository.findById(id)
                if (movimiento != null) {
                    call.respond(movimiento)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Movimiento no encontrado"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al buscar movimiento"))
            }
        }

        // Obtener todos los movimientos
        get("/movimientos") {
            try {
                val movimientos = movimientoRepository.findAll()
                call.respond(movimientos)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al obtener movimientos"))
            }
        }

        // Actualizar un movimiento existente
        patch("/movimientos/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@patch call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID inválido"))

                val movimientoData = call.receive<Movimiento>()
                if (movimientoData.movimientoId != id) {
                    return@patch call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID no coincide"))
                }

                val actualizado = movimientoRepository.update(movimientoData)
                call.respond(HttpStatusCode.OK, actualizado)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al actualizar movimiento"))
            }
        }
        // Eliminar un movimiento
        delete("/movimientos/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID inválido"))

            val eliminado = movimientoRepository.eliminarPorId(id)
            if (eliminado) {
                call.respond(HttpStatusCode.OK, mapOf("message" to "Movimiento eliminado"))
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Movimiento no encontrado"))
            }
        }


        get("/balance") {
            try {
                val balance = movimientoRepository.calcularBalance()
                call.respond(HttpStatusCode.OK, mapOf("balanceTotal" to balance))
            } catch (e: Exception) {
                println("❌ Error al calcular balance: ${e.message}")
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al calcular balance"))
            }
        }

        get("/balance/semanal") {
            try {
                val desdeStr = call.request.queryParameters["desde"]
                val hastaStr = call.request.queryParameters["hasta"]

                if (desdeStr.isNullOrBlank() || hastaStr.isNullOrBlank()) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Parámetros 'desde' y 'hasta' requeridos"))
                    return@get
                }

                val desde = LocalDate.parse(desdeStr)
                val hasta = LocalDate.parse(hastaStr)

                val movimientos = movimientoRepository.findMovimientosPorRango(desde, hasta)
                val (balanceTotal, totalEfectivo, totalTransferencia) = movimientoRepository.calcularBalanceSemanal(desde, hasta)

                val response = BalanceSemanalResponse(
                    movimientos = movimientos,
                    balanceTotal = balanceTotal,
                    totalEfectivo = totalEfectivo,
                    totalTransferencia = totalTransferencia
                )

                call.respond(HttpStatusCode.OK, response)
            } catch (e: Exception) {
                println("❌ Error en balance semanal: ${e.message}")
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al calcular balance semanal"))
            }
        }

        get("/balance/mensual") {
            try {
                val mesParam = call.request.queryParameters["mes"]
                if (mesParam.isNullOrBlank()) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Parámetro 'mes' requerido"))
                    return@get
                }
                val yearMonth = YearMonth.parse(mesParam)
                val movimientos = movimientoRepository.findMovimientosPorMes(yearMonth)
                val (balanceTotal, totalEfectivo, totalTransferencia) = movimientoRepository.calcularBalanceMensual(yearMonth)

                val balanceDouble = balanceTotal.toDouble() // o lo que quieras para balance

                val response = BalanceResponse(
                    balance = balanceDouble,
                    movimientos = movimientos,
                    balanceTotal = balanceTotal,
                    totalEfectivo = totalEfectivo,
                    totalTransferencia = totalTransferencia
                )
                call.respond(HttpStatusCode.OK, response)
            } catch (e: Exception) {
                println("❌ Error en balance mensual: ${e.message}")
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al calcular balance mensual"))
            }
        }






    }
}