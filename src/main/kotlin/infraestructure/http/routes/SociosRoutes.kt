// src/main/kotlin/com/example/infraestructure/http/routes/SocioRoutes.kt
package com.example.infraestructure.http.routes

import com.example.application.Service.CuotaService
import com.example.application.command.Socios.CreateSocioCommand
import com.example.application.commandhandler.Socios.CreateSocioHandler
import com.example.application.commandhandler.Socios.actualizarSocioHandler
import com.example.application.querys.ObtenerSocioIdQuery
import com.example.application.querysHandler.ObtenerSocioIdHandler
import com.example.application.security.requireAny
import com.example.application.security.requirePerm
import com.example.domain.Mappers.mapearUpdateDTOaEntidad
import com.example.domain.dto.SocioDTO
import com.example.domain.dto.SocioUpdateDTO
import com.example.domain.entities.SociosPage
import com.example.infraestructure.persistence.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database

fun Route.socioRoutes() {
    val database: Database = application.connectToMySql()
        ?: error("Error connecting to MySQL database")

    // Armado de repos y servicios (con la dependencia circular controlada)
    lateinit var beneficioRepository: BeneficioRepository
    val socioRepository = SocioRepository(database) { beneficioRepository }
    val actSocioHandler = actualizarSocioHandler(socioRepository)
    beneficioRepository = BeneficioRepository(database, socioRepository)

    val cobradorRepository      = CobradorRepository(database)
    val tipoSocioPenaRepository = SociosPeñaRepository(database)
    val tipoBocaRepository      = TipoSocioBocaRepository(database)
    val usuarioRepository       = UserRepository(database)
    val localidadRepository     = LocalidadRepository(database)
    val cuotaRepository         = CuotaRepository(database, beneficioRepository)

    val cuotaService = CuotaService(
        tipoSocioPenaRepository,
        cuotaRepository,
        socioRepository,
        beneficioRepository
    )

    val createSocioHandler = CreateSocioHandler(
        socioRepository,
        localidadRepository,
        tipoSocioPenaRepository,
        tipoBocaRepository,
        cobradorRepository,
        usuarioRepository,
        cuotaService
    )

    val obtenerSocioIdHandler = ObtenerSocioIdHandler(socioRepository)

    authenticate("auth-jwt") {

        // Crear socio -> SOLO ADMIN (socios:gestionar)
        post("/socios") {
            if (!requirePerm(call, "socios:gestionar")) return@post
            try {
                val body = call.receive<CreateSocioCommand>()
                val validationErrors = body.validate()
                if (validationErrors.isNotEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("errors" to validationErrors))
                    return@post
                }
                val socio = createSocioHandler.handle(body)
                if (socio == null) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "No se pudo crear el socio"))
                } else {
                    call.respond(HttpStatusCode.Created, mapOf("message" to "Socio creado exitosamente"))
                }
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("mensaje" to e.message))
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Ha ocurrido un error en el servidor"))
            }
        }

        // Ver socio por ID -> ADMIN o COBRADOR
        get("/socios/{socio_id}") {
            if (!requireAny(call, "socios:ver", "socios:gestionar")) return@get
            val socioId = call.parameters["socio_id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "ID inválido")

            val socio = obtenerSocioIdHandler.handle(ObtenerSocioIdQuery(socioId))
            if (socio != null) {
                call.respond(HttpStatusCode.OK, socio)
            } else {
                call.respond(HttpStatusCode.NotFound, "Socio no encontrado")
            }
        }

        // Listado paginado -> ADMIN o COBRADOR
        get("/socios") {
            if (!requireAny(call, "socios:ver", "socios:gestionar")) return@get
            try {
                val page   = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val size   = call.request.queryParameters["size"]?.toIntOrNull() ?: 10
                val search = call.request.queryParameters["search"]
                val offset = (page - 1) * size

                val resultado = socioRepository.obtenerPaginadoYFiltrado(size, offset, search, estado = true)
                if (resultado.socios.isEmpty()) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    val sociosDto = resultado.socios.map { socio ->
                        SocioDTO(
                            socioId        = socio.socioId,
                            nombre         = socio.nombre,
                            apellido       = socio.apellido,
                            alias          = socio.alias,
                            email          = socio.email,
                            telefono       = socio.telefono,
                            dni            = socio.dni,
                            estado         = socio.estado,
                            numSocioBoca   = socio.numSocioBoca,
                            fechaInicio    = socio.fechaInicio,
                            direccion      = socio.direccion,
                            fechaDeBaja    = socio.fechaDeBaja,
                            cobradorNombre = socio.cobradorNombre,
                            tipoPeñaNombre = socio.tipoPeñaNombre,
                            tipoBocaNombre = socio.tipoBocaNombre,
                            localidadNombre= socio.localidadNombre
                        )
                    }

                    call.respond(
                        HttpStatusCode.OK,
                        SociosPage(page = page, size = size, total = resultado.total, socios = sociosDto)
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al obtener los socios"))
            }
        }

        // Eliminar socio -> SOLO ADMIN
        delete("/socios/{socio_id}") {
            if (!requirePerm(call, "socios:gestionar")) return@delete
            val socioId = call.parameters["socio_id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID inválido"))

            try {
                val eliminado = socioRepository.eliminarPorId(socioId)
                if (eliminado) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Socio eliminado exitosamente"))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Socio no encontrado"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al eliminar el socio"))
            }
        }

        // Actualizar socio -> SOLO ADMIN
        patch("/socios/{socio_id}") {
            if (!requirePerm(call, "socios:gestionar")) return@patch
            try {
                val id = call.parameters["socio_id"]?.toIntOrNull()
                    ?: return@patch call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID inválido"))

                val dto = call.receive<SocioUpdateDTO>()
                if (dto.socioId != id) {
                    return@patch call.respond(HttpStatusCode.BadRequest, mapOf("error" to "El ID no coincide"))
                }

                val socioExistente = socioRepository.findById(dto.socioId)
                    ?: return@patch call.respond(HttpStatusCode.NotFound, mapOf("error" to "Socio no encontrado"))

                val socioParaActualizar = mapearUpdateDTOaEntidad(dto, socioExistente)
                val socioActualizado = actSocioHandler.actualizarSocio(socioParaActualizar)
                call.respond(HttpStatusCode.OK, socioActualizado)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al actualizar socio"))
            }
        }

        // Dar de baja socio -> SOLO ADMIN
        patch("/socios/{id}/baja") {
            if (!requirePerm(call, "socios:gestionar")) return@patch
            val socioId = call.parameters["id"]?.toIntOrNull()
                ?: return@patch call.respond(HttpStatusCode.BadRequest, "ID de socio inválido")

            val existe = socioRepository.findById(socioId)
                ?: return@patch call.respond(HttpStatusCode.NotFound, "Socio no encontrado")

            val resultado = socioRepository.darDeBajaPorId(socioId)
            if (!resultado) {
                call.respond(HttpStatusCode.InternalServerError, "Error al dar de baja el socio")
                return@patch
            }

            // Solo si hay fechaDeBaja, generamos cuotas desde esa fecha
            existe.fechaDeBaja?.let { fecha ->
                cuotaService.generarCuotasDesdeFecha(socioId, fecha)
            }

            call.respond(HttpStatusCode.OK, mapOf("mensaje" to "Socio dado de baja correctamente"))
        }

        // Listado de bajas -> ADMIN o COBRADOR
        get("/socios-baja-paginado") {
            if (!requireAny(call, "socios:ver", "socios:gestionar")) return@get
            val limit  = call.request.queryParameters["limit"]?.toIntOrNull() ?: 10
            val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0
            val filtro = call.request.queryParameters["filtro"]
            try {
                val resultado = socioRepository.obtenerPaginadoYFiltrado(limit, offset, filtro, estado = false)
                if (resultado.socios.isEmpty()) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.OK, resultado)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Error al obtener los socios dados de baja")
                )
            }
        }

        // Reactivar socio -> SOLO ADMIN
        patch("/socios-baja/reactivar/{id}") {
            if (!requirePerm(call, "socios:gestionar")) return@patch
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@patch call.respond(HttpStatusCode.BadRequest, "ID inválido")

            val socio = socioRepository.findById(id)
                ?: return@patch call.respond(HttpStatusCode.NotFound, "Socio no encontrado")

            if (socio.fechaDeBaja == null) {
                return@patch call.respond(HttpStatusCode.BadRequest, "El socio no estaba dado de baja")
            }

            val reactivado = socioRepository.reactivarSocio(id)
            if (!reactivado) {
                return@patch call.respond(HttpStatusCode.InternalServerError, "Error al reactivar socio")
            }

            // Ya validamos que no es null → usar !!
            cuotaService.generarCuotasDesdeFecha(id, socio.fechaDeBaja!!)
            call.respond(HttpStatusCode.OK, "Socio reactivado y cuotas generadas desde la fecha de baja")
        }

        // Totales -> ADMIN o COBRADOR
        get("/socios/total-activos") {
            if (!requireAny(call, "socios:ver", "socios:gestionar")) return@get
            try {
                val total = socioRepository.contarPorEstado(true)
                call.respond(HttpStatusCode.OK, mapOf("total" to total))
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Error al obtener total de socios activos")
                )
            }
        }

        get("/socios/total-bajas") {
            if (!requireAny(call, "socios:ver", "socios:gestionar")) return@get
            try {
                val total = socioRepository.contarPorEstado(false)
                call.respond(HttpStatusCode.OK, mapOf("total" to total))
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Error al obtener total de socios dados de baja")
                )
            }
        }
    }
}
