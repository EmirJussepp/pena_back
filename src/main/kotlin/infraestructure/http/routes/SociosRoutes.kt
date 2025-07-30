package com.example.infraestructure.http.routes



import com.example.application.Service.CuotaService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.example.application.commandhandler.Socios.CreateSocioHandler
import com.example.application.command.Socios.CreateSocioCommand
import com.example.application.querys.ObtenerSocioIdQuery
import com.example.application.querys.ObtenerSocioPorNombreQuery
import com.example.application.querysHandler.GetSocioQueryHandler

import com.example.application.querysHandler.ObtenerSocioIdHandler
import com.example.application.querysHandler.ObtenerSocioNombreHandler
import com.example.domain.Dto.SocioDTO
import com.example.domain.Dto.SocioUpdateDTO

import com.example.domain.Mappers.mapearUpdateDTOaEntidad
import com.example.domain.entities.SociosPage
import com.example.infraestructure.persistence.*
import com.example.infrastructure.repositories.CuotaRepository
import org.jetbrains.exposed.sql.Database

fun Application.socioRoutes() {
    val database: Database = connectToMySql() ?: error("Error connecting to MySQL database")

    lateinit var beneficioRepository: BeneficioRepository

    val socioRepository = SocioRepository(database) { beneficioRepository }

    beneficioRepository = BeneficioRepository(database, socioRepository)

    val cobradorRepository = CobradorRepository(database)
    val tipoSocioPeñaRepository = SociosPeñaRepository(database)
    val tipoBocaRepository = TipoSocioBocaRepository(database)
    val usuarioRepository = UserRepository(database)
    val localidadRepository = LocalidadRepository(database)

    val cuotaRepository = CuotaRepository(database, beneficioRepository)

    val cuotaService = CuotaService(
        tipoSocioPeñaRepository,
        tipoBocaRepository,
        cuotaRepository,
        socioRepository,
        beneficioRepository

    )

    val createSocioHandler = CreateSocioHandler(
        socioRepository,
        localidadRepository,
        tipoSocioPeñaRepository,
        tipoBocaRepository,
        cobradorRepository,
        usuarioRepository,
        cuotaService
    )


    val obtenerSocioIdHandler = ObtenerSocioIdHandler(socioRepository) // 🔹 Aquí creamos la instancia

    val obtenerSocioNombreHandler = ObtenerSocioNombreHandler(socioRepository)

    routing {
        post("/socios") {
            try {
                // Recibe el cuerpo de la solicitud
                val body = call.receive<CreateSocioCommand>()

                // Valida los datos del socio
                val validationErrors = body.validate()

                // Si hay errores de validación, responde con BadRequest
                if (validationErrors.isNotEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("errors" to validationErrors))
                    return@post
                }

                // Maneja la creación del socio a través del handler
                val socio = createSocioHandler.handle(body)

                // Si no se pudo crear el socio, responde con InternalServerError
                if (socio == null) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "No se pudo crear el socio"))
                } else {
                    // Si el socio se crea exitosamente, responde con Created
                    call.respond(HttpStatusCode.Created, mapOf("message" to "Socio creado exitosamente"))
                }
            } catch (e: IllegalArgumentException) {
                // Asegúrate de que e.message no sea null y responde con un mensaje de error adecuado
                val errorMessage = e.message ?: "Argumento inválido"
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to errorMessage))
            } catch (e: Exception) {
                // Captura cualquier otro error inesperado y responde con un error genérico
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Ha ocurrido un error en el servidor")
                )
                e.printStackTrace()  // Imprime el stack trace del error para depuración
            }
        }


        get("/socios/{socio_id}") {
            val socioId = call.parameters["socio_id"]?.toIntOrNull()
            if (socioId == null) {
                call.respond(HttpStatusCode.BadRequest, "ID inválido")
                return@get
            }

            val socio = obtenerSocioIdHandler.handle(ObtenerSocioIdQuery(socioId)) // 🔹 Usamos la instancia
            if (socio != null) {
                call.respond(HttpStatusCode.OK, socio)
            } else {
                call.respond(HttpStatusCode.NotFound, "Socio no encontrado")
            }
        }

//        get("/socios/name/{nombre}") {
//            val name = call.parameters["nombre"]
//            if (name == null) {
//                call.respond(HttpStatusCode.BadRequest, "Nombre inválido")
//                return@get
//            }
//
//            // Crea el query y llama al handler
//            val query = ObtenerSocioPorNombreQuery(name)
//            val socio = obtenerSocioNombreHandler.handle(query)
//
//            if (socio.isNotEmpty()) {
//                call.respond(HttpStatusCode.OK, socio)
//            } else {
//                call.respond(HttpStatusCode.NotFound, "Socio no encontrado")
//            }
//        }
        get("/socios") {
            try {
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10
                val search = call.request.queryParameters["search"]

                // Calcular offset
                val offset = ((page - 1) * size).toInt()

                // Obtener resultado paginado y filtrado desde el repositorio
                val resultado = socioRepository.obtenerPaginadoYFiltrado(size, offset, search, estado = true)


                if (resultado.socios.isEmpty()) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    // Mapear cada socio a SocioDTO
                    val sociosDto = resultado.socios.map { socio ->
                        SocioDTO(
                            socioId = socio.socioId,
                            nombre = socio.nombre,
                            apellido = socio.apellido,
                            alias = socio.alias,
                            email = socio.email,
                            telefono = socio.telefono,
                            dni = socio.dni,
                            estado = socio.estado,
                            numSocioBoca = socio.numSocioBoca,
                            fechaInicio = socio.fechaInicio,
                            direccion = socio.direccion,
                            fechaDeBaja = socio.fechaDeBaja,
                            cobradorNombre = socio.cobradorNombre,
                            tipoPeñaNombre = socio.tipoPeñaNombre,
                            tipoBocaNombre = socio.tipoBocaNombre,
                            localidadNombre = socio.localidadNombre
                        )
                    }

                    // Enviar la respuesta con paginación
                    call.respond(
                        HttpStatusCode.OK,
                        SociosPage(
                            page = page,
                            size = size,
                            total = resultado.total,
                            socios = sociosDto
                        )
                    )

                }
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al obtener los socios"))
            }
        }



        delete("/socios/{socio_id}") {
            val socioId = call.parameters["socio_id"]?.toIntOrNull()

            if (socioId == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID inválido"))
                return@delete
            }

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

        patch("/socios/{socio_id}") {
            try {
                val id = call.parameters["socio_id"]?.toIntOrNull()
                    ?: return@patch call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID inválido"))

                val dto = call.receive<SocioUpdateDTO>()
                println("DTO recibido: $dto")


                if (dto.socioId != id) {
                    return@patch call.respond(HttpStatusCode.BadRequest, mapOf("error" to "El ID no coincide"))
                }

                val socioExistente = socioRepository.findById(dto.socioId)
                    ?: return@patch call.respond(HttpStatusCode.NotFound, mapOf("error" to "Socio no encontrado"))

                val socioActualizado = mapearUpdateDTOaEntidad(dto, socioExistente)
                println("Entidad para actualizar: $socioActualizado")

                val socioGuardado = socioRepository.update(socioActualizado)
                println("Entidad guardada: $socioGuardado")

                call.respond(HttpStatusCode.OK, socioGuardado)
            } catch (e: Exception) {
                e.printStackTrace()  // imprime detalle del error en consola
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al actualizar socio"))
            }
        }
        patch("/socios/{id}/baja") {
            val idParam = call.parameters["id"]
            val socioId = idParam?.toIntOrNull()
            if (socioId == null) {
                call.respond(HttpStatusCode.BadRequest, "ID de socio inválido")
                return@patch
            }

            val existe = socioRepository.findById(socioId)
            if (existe == null) {
                call.respond(HttpStatusCode.NotFound, "Socio no encontrado")
                return@patch
            }

            val resultado = socioRepository.darDeBajaPorId(socioId)
            if (resultado) {
                call.respond(HttpStatusCode.OK, mapOf("mensaje" to "Socio dado de baja correctamente"))
            } else {
                call.respond(HttpStatusCode.InternalServerError, "Error al dar de baja el socio")
            }
        }

        get("/socios-baja-paginado") {
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 10
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


        patch("/socios-baja/reactivar/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "ID inválido")
                return@patch
            }

            val socio = socioRepository.findById(id)
            if (socio == null || socio.fechaDeBaja == null) {
                call.respond(HttpStatusCode.NotFound, "Socio no encontrado o no estaba dado de baja")
                return@patch
            }

            val reactivado = socioRepository.reactivarSocio(id)
            if (!reactivado) {
                call.respond(HttpStatusCode.InternalServerError, "Error al reactivar socio")
                return@patch
            }

            cuotaService.generarCuotasDesdeFecha(id, socio.fechaDeBaja)

            call.respond(HttpStatusCode.OK, "Socio reactivado y cuotas generadas desde la fecha de baja")
        }
        get("/socios/total-activos") {
            try {
                val total = socioRepository.contarPorEstado(true)
                call.respond(HttpStatusCode.OK, mapOf("total" to total))
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al obtener total de socios activos"))
            }
        }

        get("/socios/total-bajas") {
            try {
                val total = socioRepository.contarPorEstado(false)
                call.respond(HttpStatusCode.OK, mapOf("total" to total))
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al obtener total de socios dados de baja"))
            }
        }






    }
}
