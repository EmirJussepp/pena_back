package com.example.application.commandhandler

import com.example.domain.contracts.IUserRepository
import com.example.application.command.CreateUserCommand
import com.example.domain.entities.User
import com.example.application.security.PasswordService
import com.example.domain.contracts.IUserRolesRepository
import com.example.infraestructure.persistence.RolesRepository

class CreateUserCommandHandler(
    private val userRepository: IUserRepository,
    private val userRolesRepo: IUserRolesRepository,
    private val rolesRepo: RolesRepository
) {
    fun handle(cmd: CreateUserCommand) {
        cmd.validate()

        // Verificar si ya existe
        if (userRepository.findByEmail(cmd.email) != null)
            throw IllegalArgumentException("El email ya está registrado")

        // Hashear la password
        val hashed = PasswordService.hash(cmd.password)

        // Crear usuario
        val user = User.create(cmd.name, cmd.email, hashed)
        val createdUser = userRepository.save(user)
        val userId = createdUser.userId!!
        cmd.roles.forEach { roleName ->
            val roleId = rolesRepo.getIdByName(roleName)
                ?: throw IllegalArgumentException("El rol '$roleName' no existe")
            userRolesRepo.assign(userId, roleId)
        }

        println("✅ Usuario creado con roles: ${cmd.roles}")
    }
}
