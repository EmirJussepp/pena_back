// src/main/kotlin/com/example/infraestructure/http/routes/AuthRoutes.kt
package com.example.infraestructure.http.routes

import com.example.application.security.PasswordService
import com.example.domain.dto.LoginRequest
import com.example.infraestructure.persistence.UserRepository
import com.example.infraestructure.persistence.AccessRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import org.slf4j.LoggerFactory

/**
 * Rutas de autenticación recibiendo dependencias ya inicializadas.
 * - No abre conexiones ni usa connectToMySql.
 * - issueJwt: función para emitir el token (ej: JwtConfig::issue).
 */
fun Route.authRoutes(
    userRepository: UserRepository,
    accessRepository: AccessRepository,
    issueJwt: (userId: Int, email: String, roles: List<String>, perms: List<String>) -> String
) {
    val log = LoggerFactory.getLogger("AuthRoutes")

    route("/login") {
        post {
            try {
                val login = call.receive<LoginRequest>()

                val usuario = userRepository.findByEmail(login.email)
                    ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Usuario no encontrado"))

                val stored = usuario.passwordHash
                val ok = if (PasswordService.looksHashed(stored)) {
                    PasswordService.verify(login.password, stored)
                } else {
                    val match = stored == login.password
                    if (match) {
                        // Migración transparente a hash
                        userRepository.updateHash(requireNotNull(usuario.userId), PasswordService.hash(login.password))
                    }
                    match
                }

                if (!ok) {
                    return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Credenciales incorrectas"))
                }

                val access = accessRepository.getAccessByEmail(login.email)
                    ?: return@post call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Sin acceso"))

                val puedeEntrar = "*" in access.permissions || "app:acceder" in access.permissions
                if (!puedeEntrar) {
                    return@post call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Acceso denegado"))
                }

                val token = issueJwt(
                    access.userId,
                    access.email,
                    access.roles,
                    access.permissions
                )

                call.respond(HttpStatusCode.OK, mapOf("message" to "Login exitoso", "token" to token))
            } catch (e: Exception) {
                log.error("Error en /login", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Error interno")))
            }
        }
    }

    // Endpoints protegidos con JWT
    authenticate("auth-jwt") {
        get("/auth/me") {
            val p = call.principal<JWTPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val roles = p.payload.getClaim("roles").asList(String::class.java) ?: emptyList()
            val perms = p.payload.getClaim("perms").asList(String::class.java) ?: emptyList()

            call.respond(mapOf("roles" to roles, "perms" to perms))
        }
    }
}
