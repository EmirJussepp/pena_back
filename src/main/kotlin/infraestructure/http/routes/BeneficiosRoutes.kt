// src/main/kotlin/com/example/infraestructure/http/routes/BeneficiosRoutes.kt
package com.example.infraestructure.http.routes

import com.example.infraestructure.persistence.BeneficioRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Define rutas de beneficios recibiendo el repositorio ya creado.
 * No abre conexiones ni usa connectToMySql.
 */
fun Route.beneficiosRoutes(
    beneficioRepository: BeneficioRepository
) {
    route("/beneficios") {

        // POST /beneficios/recalcular
        post("/recalcular") {
            try {
                // Si el repo usa Exposed con newSuspendedTransaction, este withContext puede no ser necesario.
                withContext(Dispatchers.IO) {
                    beneficioRepository.recalcularBeneficiosATodos()
                }
                call.respond(HttpStatusCode.OK, mapOf("mensaje" to "Beneficios recalculados correctamente"))
            } catch (e: Exception) {
                call.application.log.error("Error recalculando beneficios", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error recalculando beneficios"))
            }
        }

        // GET /beneficios?pagina=&limite=&filtro=
        get {
            try {
                val pagina = call.request.queryParameters["pagina"]?.toIntOrNull() ?: 1
                val limite = call.request.queryParameters["limite"]?.toIntOrNull() ?: 10
                val filtro = call.request.queryParameters["filtro"]

                val resultado = beneficioRepository.obtenerBeneficiosPaginadoYFiltrado(pagina, limite, filtro)
                call.respond(HttpStatusCode.OK, resultado)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Datos inválidos")))
            } catch (e: Exception) {
                call.application.log.error("Error listando beneficios", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error en el servidor"))
            }
        }
    }
}
