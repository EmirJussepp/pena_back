package com.example

import com.example.application.Service.CuotaService
import com.example.application.commandhandler.CreateCuotaCommandHandler
import com.example.application.commandhandler.Movimientos.MovimientoCommandHandler

import com.example.application.commandhandler.pagos.CrearPagoCommandHandler
import com.example.infraestructure.http.routes.*
import com.example.infraestructure.persistence.*
import com.example.repository.ReportesRepository


import com.example.routes.cuotaRoutes
import com.example.routes.reportes
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

// Primero creamos socioRepository, pasando una lambda que retorna la instancia de beneficioRepository que aún no está inicializada
    val socioRepository = SocioRepository(database) { beneficioRepository}
val reportesRepository= ReportesRepository(database)
// Ahora inicializamos beneficioRepository con socioRepository ya creado
    beneficioRepository = BeneficioRepository(database, socioRepository)
    reportes(reportesRepository, database)
// Luego continuás con el resto
    val pagoRepository = PagoRepository(database)
//    val userRepository = UserRepository(database)
//    val viajePagoRepository = ViajesPagosRepository(database)
//    val viajeRepository = ViajeBomboneraRepository(database)
    val movimientoRepository = MovimientoRepository(database)
    val sociosPenaRepository = SociosPeñaRepository(database)
//    val tipoSocioBocaRepository = TipoSocioBocaRepository(database)

    val cuotaRepository = CuotaRepository(
        database = database,
        beneficioRepository = beneficioRepository
    )


    val cuotaService = CuotaService(
        tipoSocioPeñaRepository = sociosPenaRepository,
        socioRepository= socioRepository,
        cuotaRepository = cuotaRepository,
        beneficioRepository = beneficioRepository
    )
    cuotaService.iniciarSchedulerCuotasMensuales()

    // Handlers
    val createMovimientoHandler = MovimientoCommandHandler(
        movimientoRepository
    )




    // Handlers
    val createCuotaHandler = CreateCuotaCommandHandler(
        cuotaRepository = cuotaRepository,
        socioRepository = socioRepository,
        cuotaService = cuotaService // 👈 este también lo requiere
    )


    val crearPagoCommandHandler = CrearPagoCommandHandler(
        pagoRepository = pagoRepository,
        cuotaRepository = cuotaRepository,
        cuotaService = cuotaService,
        socioRepository = socioRepository
    )



    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        get("/json/gson") {
            call.respond(mapOf("hello" to "world"))
        }

        get("/json/kotlinx-serialization") {
            call.respond(mapOf("hello" to "world"))
        }

        // Rutas del sistema
        userRoutes()
        metodoPagoRoutes()
        socioPeñaRoutes()
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

        // Cuotas y Pagos
        cuotaRoutes(
            cuotaRepository = cuotaRepository,
            createCuotaHandler = createCuotaHandler,
            cuotaService= cuotaService,
            socioRepository = socioRepository
        )

        pagoRoutes(
            pagoRepository = pagoRepository,
//            cuotaRepository = cuotaRepository,
            crearPagoCommandHandler = crearPagoCommandHandler
        )
    }
}

