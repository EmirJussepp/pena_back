package com.example.infraestructure.http.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.auth.*

import com.example.application.security.requirePerm   // <— importá esto

import com.example.application.commandhandler.CreateUserCommandHandler
import com.example.application.command.CreateUserCommand
import com.example.domain.dto.UserResponse
import com.example.infraestructure.persistence.UserRepository
import com.example.infraestructure.persistence.connectToMySql
import org.jetbrains.exposed.sql.Database

fun Application.userRoutes() {
    val database: Database = connectToMySql() ?: error("Error connecting to MySQL database")
    val userRepository = UserRepository(database)
    val createUserHandler = CreateUserCommandHandler(userRepository)

    routing {
        authenticate("auth-jwt") {

            post("/users") {
                requirePerm(call, "usuarios:gestionar")
                // si respondió 403 arriba, retorná para cortar
                if (call.response.isCommitted) return@post

                try {
                    val body = call.receive<CreateUserCommand>()
                    body.validate()
                    createUserHandler.handle(body)
                    call.respond(HttpStatusCode.Created, mapOf("message" to "User Created Successfully"))
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Server error"))
                }
            }

            get("/usuarios") {
                requirePerm(call, "usuarios:gestionar")
                if (call.response.isCommitted) return@get

                try {
                    val usuarios = userRepository.obtenerTodos()
                    val safe = usuarios.map { UserResponse(it.userId!!, it.name, it.email) }
                    call.respond(HttpStatusCode.OK, safe)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "No se pudieron obtener los usuarios"))
                }
            }
        }
    }
}
