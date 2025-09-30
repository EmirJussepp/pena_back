// src/main/kotlin/com/example/infraestructure/http/routes/BeneficiosRoutes.kt
package com.example.infraestructure.http.routes

import com.example.infraestructure.persistence.BeneficioRepository
import com.example.infraestructure.persistence.SocioRepository
import com.example.infraestructure.persistence.connectToMySql
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database

fun Route.beneficiosRoutes() {
    val database: Database = application.connectToMySql()
        ?: error("Error connecting to MySQL database")

    // Dependencia circular controlada
    lateinit var beneficioRepository: BeneficioRepository
    val socioRepository = SocioRepository(database) { beneficioRepository }
    beneficioRepository = BeneficioRepository(database, socioRepository)

    route("/beneficios") {

        // POST /beneficios/recalcular
        post("/recalcular") {
            try {
                withContext(Dispatchers.IO) {
                    beneficioRepository.recalcularBeneficiosATodos()
                }
                call.respond(HttpStatusCode.OK, mapOf("mensaje" to "Beneficios recalculados correctamente"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error recalculando beneficios"))
            }
        }

        // GET /beneficios?pagina=&limite=&filtro=
        get {
            try {
                val pagina = call.request.queryParameters["pagina"]?.toIntOrNull() ?: 1
                val limite = call.request.queryParameters["limite"]?.toIntOrNull() ?: 10
                val filtro = call.request.queryParameters["filtro"]

                println("📥 Recibido params - pagina: $pagina, limite: $limite, filtro: $filtro")

                val resultado = beneficioRepository.obtenerBeneficiosPaginadoYFiltrado(pagina, limite, filtro)

                println("📤 Enviando beneficios paginados: ${resultado.beneficios.size} items")

                call.respond(HttpStatusCode.OK, resultado)
            } catch (e: IllegalArgumentException) {
                println("❌ Error de validación: ${e.message}")
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                println("❌ Error interno: ${e.message}")
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error en el servidor"))
            }
        }
    }
}
