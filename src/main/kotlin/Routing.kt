package com.example

import com.example.application.Service.CuotaService
import com.example.application.commandhandler.CreateCuotaCommandHandler
import com.example.application.commandhandler.Movimientos.MovimientoCommandHandler
import com.example.application.commandhandler.pagos.CrearPagoCommandHandler

import com.example.infraestructure.http.routes.*   // todas tus Route.xxxRoutes()
import com.example.routes.cuotaRoutes              // <- cuotaRoutes está en otro paquete
import com.example.infraestructure.persistence.*
import com.example.repository.ReportesRepository

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }

    val database = connectToMySql() ?: error("Error connecting to MySQL")

    lateinit var beneficioRepository: BeneficioRepository
    val socioRepository = SocioRepository(database) { beneficioRepository }
    val reportesRepository = ReportesRepository(database)
    beneficioRepository = BeneficioRepository(database, socioRepository)

    val pagoRepository = PagoRepository(database)
    val movimientoRepository = MovimientoRepository(database)
    val sociosPenaRepository = SociosPeñaRepository(database)

    val cuotaRepository = CuotaRepository(
        database = database,
        beneficioRepository = beneficioRepository
    )

    val cuotaService = CuotaService(
        tipoSocioPeñaRepository = sociosPenaRepository,
        socioRepository = socioRepository,
        cuotaRepository = cuotaRepository,
        beneficioRepository = beneficioRepository
    )
    cuotaService.iniciarSchedulerCuotasMensuales()

    val createMovimientoHandler = MovimientoCommandHandler(movimientoRepository)
    val createCuotaHandler = CreateCuotaCommandHandler(
        cuotaRepository = cuotaRepository,
        socioRepository = socioRepository,
        cuotaService = cuotaService
    )
    val crearPagoCommandHandler = CrearPagoCommandHandler(
        pagoRepository = pagoRepository,
        cuotaRepository = cuotaRepository,
        cuotaService = cuotaService,
        socioRepository = socioRepository
    )

    routing {
        get("/") { call.respondText("Hello World!") }
        get("/json/gson") { call.respond(mapOf("hello" to "world")) }
        get("/json/kotlinx-serialization") { call.respond(mapOf("hello" to "world")) }

        // Todas estas deben ser fun Route.xxxRoutes(...)
        reportes(reportesRepository, database)
        userRoutes()
        metodoPagoRoutes()
        socioPeñaRoutes()          // <- con ñ (o cambia a sociosPenaRoutes() si preferís sin ñ)
        socioRoutes()
        viajeBomboneraRoutes()
        cobradorRoutes()
        viajesPagosRoutes()
        localidadRoutes()
        socioBocaRoutes()
        movimientoRoutes(movimientoRepository, createMovimientoHandler)
        alquileresSalonesRoutes()
        salonesRoutes()
        beneficiosRoutes()
        authRoutes()

        // Cuotas y pagos
        cuotaRoutes(
            cuotaRepository = cuotaRepository,
            createCuotaHandler = createCuotaHandler,
            cuotaService = cuotaService,
            socioRepository = socioRepository
        )
        pagoRoutes(
            pagoRepository = pagoRepository,
            crearPagoCommandHandler = crearPagoCommandHandler
        )
    }
}
