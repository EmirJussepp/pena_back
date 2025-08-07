package com.example.infraestructure.http.routes
import com.example.application.commandhandler.ViajesPagos.ViajePagoCommandHandler
import com.example.application.command.ViajesPagos.CreateViajesPagosCommand
import com.example.infraestructure.persistence.connectToMySql
import com.example.infraestructure.persistence.ViajesPagosRepository
import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database

fun Application.viajesPagosRoutes() {
    val database: Database = connectToMySql() ?: error("Error connecting to MySQL database")
    val viajesPagosRepository = ViajesPagosRepository(database)
    val viajesPagosHandler = ViajePagoCommandHandler(viajesPagosRepository)


    routing {
        // Ruta para crear o actualizar un pago de viaje
        post("/viajesPagos") {
            try {
                val command = call.receive<CreateViajesPagosCommand>()
                println("Datos recibidos: $command")

                // Validar los datos recibidos
                command.validate()

                // Guardar el pago en la base de datos (crear o actualizar)
                 viajesPagosHandler.handle(command)

                // Responder con un mensaje de confirmación
                val mensaje = "El pago del viaje se ha registrado correctamente"
                call.respond(HttpStatusCode.Created, mensaje)
            } catch (e: Exception) {
                println("Error: ${e.message}")
                call.respond(HttpStatusCode.BadRequest, "Error: ${e.message}")
            }
        }
        get("viajePagos"){

            try {
                val viajesPagos = viajesPagosRepository.findAll()
                call.respond(viajesPagos)
            } catch (e: Exception) {
                println("❌ Error al obtener viaje: ${e.message}")
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al viaje pago"))
            }

        }
        get("/viajePagos/{viajeId}") {
            try {
                val viajeId = call.parameters["viajeId"]?.toIntOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "viajeId inválido")

                val pagos = viajesPagosRepository.findByViajeId(viajeId)

                call.respond(pagos)
            } catch (e: Exception) {
                println("❌ Error al obtener pagos por viajeId: ${e.message}")
                call.respond(HttpStatusCode.InternalServerError, "Error al obtener pagos")
            }
        }
        // NUEVA RUTA: Listar todos los pagos con detalles completos (método pago + cobrador)
        get("/viajePagosFull") {
            try {
                val pagosCompletos = viajesPagosRepository.findAllConCobradorYMetodo()
                call.respond(pagosCompletos)
            } catch (e: Exception) {
                println("❌ Error al obtener pagos completos: ${e.message}")
                call.respond(HttpStatusCode.InternalServerError, "Error al obtener pagos completos")
            }
        }
        delete("/viajePagos/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID Invalido"))

            val eliminado = viajesPagosRepository.eliminarPorId(id)
            if (eliminado) {
                call.respond(HttpStatusCode.OK, mapOf("message" to "Pasajero eliminado"))
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Pasajero no encontrado"))
            }
        }

    }
}
