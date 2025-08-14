package com.example.infraestructure.http.routes

import com.example.application.security.PasswordService
import com.example.domain.dto.LoginRequest
import com.example.infraestructure.persistence.UserRepository
import com.example.infraestructure.persistence.AccessRepository
import com.example.infraestructure.persistence.connectToMySql

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.auth.*            // <- importa auth
import io.ktor.server.auth.jwt.*       // <- importa JWT
import org.jetbrains.exposed.sql.Database

fun Application.authRoutes() {
    val database: Database = connectToMySql() ?: error("Error connecting to MySQL database")
    val userRepository = UserRepository(database)
    val accessRepository = AccessRepository(database)

    routing {
        post("/login") {
            try {
                val login = call.receive<LoginRequest>()

                val usuario = userRepository.findByEmail(login.email)
                    ?: return@post call.respond(HttpStatusCode.BadRequest)

                val stored = usuario.passwordHash
                val ok = if (PasswordService.looksHashed(stored)) {
                    PasswordService.verify(login.password, stored)
                } else {
                    val match = stored == login.password
                    if (match) userRepository.updateHash(usuario.userId!!, PasswordService.hash(login.password))
                    match
                }
                if (!ok) return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Credenciales incorrectas"))

                // >>> Carga roles/permisos y emite token con claims
                val access = AccessRepository(database).getAccessByEmail(login.email)
                    ?: return@post call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Sin acceso"))

                // comenta o eliminá:
                val puedeEntrar = "*" in access.permissions || "app:acceder" in access.permissions
                if (!puedeEntrar) return@post call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Acceso denegado"))



                val token = JwtConfig.issue(
                    userId = access.userId,
                    email  = access.email,
                    roles  = access.roles,
                    perms  = access.permissions
                )

                call.respond(HttpStatusCode.OK, mapOf("message" to "Login exitoso", "token" to token))
            } catch (e: Exception) {
                e.printStackTrace() // <- mirá la consola del server
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Error interno")))
            }
        }


        // 🔎 Endpoint de debug de claims (proteger con JWT)
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
}
