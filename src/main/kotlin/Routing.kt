package com.example

import com.example.application.Service.CuotaService
import com.example.application.commandhandler.CreateCuotaCommandHandler
import com.example.application.commandhandler.Movimientos.MovimientoCommandHandler
import com.example.application.commandhandler.pagos.CrearPagoCommandHandler

// Rutas HTTP (todas las que viven en infraestructure/http/routes)
import com.example.infraestructure.http.routes.*

// Infra / repos
import com.example.infraestructure.persistence.*
import com.example.repository.ReportesRepository

// Handlers de Socios
import com.example.application.commandhandler.Socios.CreateSocioHandler
import com.example.application.commandhandler.Socios.actualizarSocioHandler
import com.example.application.querysHandler.ObtenerSocioIdHandler

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

    // ---- DB: inicializar UNA sola vez y compartirla ----
    val dbUrl = buildJdbcUrlFromEnv()
    val dbUser = System.getenv("DB_USER") ?: System.getenv("MYSQLUSER") ?: error("Falta DB_USER o MYSQLUSER")
    val dbPass = System.getenv("DB_PASSWORD") ?: System.getenv("MYSQLPASSWORD") ?: error("Falta DB_PASSWORD o MYSQLPASSWORD")

    val database = DatabaseProvider.init(
        url = dbUrl,
        user = dbUser,
        pass = dbPass,
        driver = "com.mysql.cj.jdbc.Driver",
        poolSize = 10
    )

    // Migraciones/seed RBAC
    configureDatabases(database)

    // ---- Repos ----
    lateinit var beneficioRepository: BeneficioRepository
    val socioRepository        = SocioRepository(database) { beneficioRepository }
    val reportesRepository     = ReportesRepository(database)
    beneficioRepository        = BeneficioRepository(database, socioRepository)

    val cobradorRepo           = CobradorRepository(database)
    val pagoRepository         = PagoRepository(database)
    val movimientoRepository   = MovimientoRepository(database)
    val sociosPenaRepository   = SociosPeñaRepository(database)
    val localidadRepo          = LocalidadRepository(database)
    val salonesRepo            = SalonesRepository(database)
    val metodoPagoRepo         = MetodoPagoRepository(database)
    val tipoSocioBocaRepo      = TipoSocioBocaRepository(database)
    val alquileresRepo         = AlquilerSalonesRepository(database)
    val viajesPagosRepo        = ViajesPagosRepository(database)
    val viajeBomboneraRepo     = ViajeBomboneraRepository(database)

    val userRepo               = UserRepository(database)
    val rolesRepo              = RolesRepository(database)
    val userRolesRepo          = UserRolesRepository(database)
    val accessRepo             = AccessRepository(database)

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

    // ---- Handlers ----
    val movimientoCommandHandler = MovimientoCommandHandler(movimientoRepository)
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

    val createSocioHandler    = CreateSocioHandler(
        socioRepository          = socioRepository,
        localidadRepository      = localidadRepo,
        tipoSocioPenaRepository  = sociosPenaRepository,
        tipoSocioBocaRepository  = tipoSocioBocaRepo, // <— nombre correcto
        cobradorRepository       = cobradorRepo,
        userRepository           = userRepo,
        cuotaService             = cuotaService
    )
    val actSocioHandler       = actualizarSocioHandler(socioRepository)
    val obtenerSocioIdHandler = ObtenerSocioIdHandler(socioRepository)

    routing {
        get("/") { call.respondText("Hello World!") }

        // -------- Rutas (inyectadas) --------
        reportes(reportesRepository, database)

        userRoutes(
            userRepository = userRepo,
            userRolesRepository = userRolesRepo,
            rolesRepository = rolesRepo
        )

        metodoPagoRoutes(metodoPagoRepo)

        socioPeñaRoutes(sociosPeñaRepository = sociosPenaRepository)

        socioRoutes(
            socioRepository       = socioRepository,
            createSocioHandler    = createSocioHandler,
            actSocioHandler       = actSocioHandler,
            obtenerSocioIdHandler = obtenerSocioIdHandler,
            cuotaService          = cuotaService
        )

        socioBocaRoutes(tipoSocioBocaRepository = tipoSocioBocaRepo)

        viajeBomboneraRoutes(viajeRepo = viajeBomboneraRepo)

        cobradorRoutes(cobradorRepository = cobradorRepo)

        viajesPagosRoutes(viajesPagosRepository = viajesPagosRepo)

        localidadRoutes(localidadRepository = localidadRepo)

        movimientoRoutes(
            movimientoRepository     = movimientoRepository,
            movimientoCommandHandler = movimientoCommandHandler // <— nombre correcto del parámetro
        )

        alquileresSalonesRoutes(alquileresRepo)

        salonesRoutes(salonRepository = salonesRepo)

        beneficiosRoutes(beneficioRepository)

        authRoutes(
            userRepository   = userRepo,
            accessRepository = accessRepo,
            issueJwt = { userId, email, roles, perms ->
                JwtConfig.issue(userId = userId, email = email, roles = roles, perms = perms)
            }
        )

        // -------- Cuotas y pagos --------
        cuotaRoutes(
            cuotaRepository    = cuotaRepository,
            createCuotaHandler = createCuotaHandler,
            cuotaService       = cuotaService,
            socioRepository    = socioRepository
        )

        pagoRoutes(
            pagoRepository          = pagoRepository,
            crearPagoCommandHandler = crearPagoCommandHandler
        )
    }
}

/** JDBC desde envs Railway */
private fun buildJdbcUrlFromEnv(): String {
    System.getenv("DB_URL")?.let { return it }
    System.getenv("JDBC_DATABASE_URL")?.let { return it }

    val host = System.getenv("MYSQLHOST") ?: error("Falta MYSQLHOST (o definí DB_URL/JDBC_DATABASE_URL)")
    val port = System.getenv("MYSQLPORT") ?: "3306"
    val db   = System.getenv("MYSQLDATABASE") ?: error("Falta MYSQLDATABASE")

    val params = listOf(
        "useSSL=false",
        "serverTimezone=UTC",
        "allowPublicKeyRetrieval=true",
        "rewriteBatchedStatements=true",
        "characterEncoding=UTF-8",
        "useUnicode=true"
    ).joinToString("&")

    return "jdbc:mysql://$host:$port/$db?$params"
}
