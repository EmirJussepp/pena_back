// src/main/kotlin/com/example/infraestructure/http/routes/UserRoutes.kt
package com.example.infraestructure.http.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.auth.*

import com.example.application.security.requirePerm

import com.example.application.commandhandler.CreateUserCommandHandler
import com.example.application.command.CreateUserCommand
import com.example.application.command.UpdateUserCommand
import com.example.application.commandhandler.UpdateUserCommandHandler

import com.example.domain.dto.UserResponse
import com.example.infraestructure.persistence.RolesRepository
import com.example.infraestructure.persistence.UserRepository
import com.example.infraestructure.persistence.UserRolesRepository
import com.example.infraestructure.persistence.connectToMySql
import org.jetbrains.exposed.sql.Database

fun Route.userRoutes() {
    val database: Database = application.connectToMySql()
        ?: error("Error connecting to MySQL database")

    val userRepository      = UserRepository(database)
    val userRolesRepository = UserRolesRepository(database)
    val rolesRepository     = RolesRepository(database)

    val createUserHandler = CreateUserCommandHandler(userRepository, userRolesRepository, rolesRepository)
    val updateUserHandler = UpdateUserCommandHandler(userRepository, rolesRepository, userRolesRepository)

    authenticate("auth-jwt") {
        route("/usuarios") {

            // Crear usuario con roles
            post {
                if (!requirePerm(call, "usuarios:gestionar")) return@post
                try {
                    val body = call.receive<CreateUserCommand>()
                    createUserHandler.handle(body)
                    call.respond(HttpStatusCode.Created, mapOf("message" to "Usuario creado con roles"))
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                }
            }

            // Listar usuarios con roles
            get {
                if (!requirePerm(call, "usuarios:gestionar")) return@get
                try {
                    val usuarios = userRepository.obtenerTodosConRoles()
                    val response = usuarios.map { user ->
                        UserResponse(
                            userId = user.userId,
                            name   = user.name,
                            email  = user.email,
                            roles  = user.roles.map { it.name } // asegurate que sea el campo correcto
                        )
                    }
                    call.respond(HttpStatusCode.OK, response)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "No se pudieron obtener los usuarios"))
                }
            }

            // Actualizar usuario
            patch("{id}") {
                if (!requirePerm(call, "usuarios:gestionar")) return@patch
                try {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: return@patch call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID inválido"))

                    val body = call.receive<UpdateUserCommand>()
                    updateUserHandler.handle(id, body)

                    call.respond(HttpStatusCode.OK, mapOf("message" to "Usuario actualizado correctamente"))
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al actualizar usuario"))
                }
            }

            // Eliminar usuario
            delete("{id}") {
                if (!requirePerm(call, "usuarios:gestionar")) return@delete
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID inválido"))

                val eliminado = userRepository.eliminarPorId(id)
                if (eliminado) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Usuario eliminado correctamente"))
                } else {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "No se pudo eliminar el usuario"))
                }
            }
        }
    }
}
