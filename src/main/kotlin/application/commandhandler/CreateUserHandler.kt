package com.example.application.commandhandler

import com.example.domain.contracts.IUserRepository
import com.example.application.command.CreateUserCommand
import com.example.domain.entities.User
import com.example.application.security.PasswordService

class CreateUserCommandHandler(
    private val userRepository: IUserRepository
) {
    fun handle(command: CreateUserCommand) {
        // 1) Validaciones de formato/campos
        command.validate()

        // 2) No permitir emails duplicados
        if (userRepository.findByEmail(command.email) != null) {
            throw IllegalArgumentException("El email ya está registrado")
        }

        // 3) Hashear la contraseña en texto plano
        val hashed = PasswordService.hash(command.passwordHash)

        // 4) Crear entidad y persistir
        val user = User.create(
            name = command.name,
            email = command.email,
            passwordHash = hashed
        )
        userRepository.save(user)

        // (Opcional) logging
        println("✅ Usuario creado: ${command.email}")
    }
}
