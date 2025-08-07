package com.example.infraestructure.http.routes



import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.example.application.commandhandler.CreateUserCommandHandler
import com.example.application.command.CreateUserCommand
import com.example.domain.entities.LoginRequest
import com.example.infraestructure.persistence.UserRepository
import com.example.infraestructure.persistence.connectToMySql
import org.jetbrains.exposed.sql.Database

fun Application.userRoutes() {
    val database: Database = connectToMySql() ?: error("Error connecting to MySQL database")
    // Conectar a MySQL
    val userRepository = UserRepository(database)
    val createUserHandler = CreateUserCommandHandler(userRepository)

    routing {
        post("/users") {
            try {
                val body = call.receive<CreateUserCommand>()
                body.validate() // Validamos los datos

                createUserHandler.handle(body)

                call.respond(HttpStatusCode.Created, mapOf("message" to "User Created Successfully"))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Server error"))
            }
        }
        post("/login") {
            val login = call.receive<LoginRequest>()

            // Buscar el usuario por email
            val usuario = userRepository.findByEmail(login.email)

            if (usuario == null || usuario.password != login.password) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Credenciales incorrectas"))
            } else {
                call.respond(HttpStatusCode.OK, mapOf("message" to "Login exitoso", "token" to "abc123"))
            }
        }
        get("/usuarios") {
            try {
                val usuarios = userRepository.obtenerTodos()
                call.respond(HttpStatusCode.OK, usuarios)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "No se pudieron obtener los usuarios"))
            }
        }


    }
}
