package com.example.application.commandhandler
import com.example.domain.contracts.IUserRepository
import com.example.application.command.CreateUserCommand
import com.example.domain.entities.User
class CreateUserCommandHandler(
    private val userRepository: IUserRepository
) {
    fun handle(command: CreateUserCommand) {
        try {
            // Validamos el comando
            command.validate()

            // Creamos el usuario y lo guardamos
            val user = User.create(
                command.name,
                command.email,
                command.password
            )
            userRepository.save(user)

            println("✅ Usuario creado exitosamente")

        } catch (e: IllegalArgumentException) {
            println("❌ Error: ${e.message}")
        } catch (e: Exception) {
            println("⚠️ Error inesperado al crear el usuario: ${e.message}")
        }
    }
}
