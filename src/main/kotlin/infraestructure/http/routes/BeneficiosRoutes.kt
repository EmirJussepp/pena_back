package com.example.infraestructure.http.routes



import com.example.infraestructure.persistence.BeneficioRepository
import com.example.infraestructure.persistence.SocioRepository
import com.example.infraestructure.persistence.connectToMySql
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database

fun Application.beneficiosRoutes() {
    val database: Database = connectToMySql() ?: error("Error connecting to MySQL database")
    lateinit var beneficioRepository: BeneficioRepository

    // Creamos socioRepository pasando una lambda que devuelve beneficioRepository
    val socioRepository = SocioRepository(database) { beneficioRepository }

    // Ahora inicializamos beneficioRepository con la instancia socioRepository creada arriba
    beneficioRepository = BeneficioRepository(database, socioRepository)

    routing {
        post("/beneficios/recalcular") {
            try {
                withContext(Dispatchers.IO) {
                    beneficioRepository.recalcularBeneficiosATodos()
                }
                call.respond(HttpStatusCode.OK, mapOf("mensaje" to "Beneficios recalculados correctamente"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error recalculando beneficios"))
            }
        }


        get("/beneficios") {
            try {
                val pagina = call.request.queryParameters["pagina"]?.toIntOrNull() ?: 1
                val limite = call.request.queryParameters["limite"]?.toIntOrNull() ?: 10
                val filtro = call.request.queryParameters["filtro"]

                println("📥 Recibido params - pagina: $pagina, limite: $limite, filtro: $filtro")

                val resultado = beneficioRepository.obtenerBeneficiosPaginadoYFiltrado(pagina, limite, filtro)

                println("📤 Enviando beneficios paginados: ${resultado.beneficios.size} items")

                call.respond(HttpStatusCode.OK, resultado)

            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                println("❌ Error de validación: ${e.message}")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error en el servidor"))
                println("❌ Error interno: ${e.message}")
            }
        }
    }
}
