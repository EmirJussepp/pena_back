package com.example.application.commandhandler

import com.example.application.command.UpdateUserCommand
import com.example.infraestructure.persistence.RolesRepository
import com.example.infraestructure.persistence.UserRepository
import com.example.infraestructure.persistence.UserRolesRepository
import org.mindrot.jbcrypt.BCrypt

class UpdateUserCommandHandler(
    private val userRepository: UserRepository,
    private val rolesRepository: RolesRepository,
    private val userRolesRepository: UserRolesRepository
) {
     fun handle(userId: Int, command: UpdateUserCommand) {
        // 1. Verificar usuario
        val user = userRepository.findById(userId)
            ?: throw IllegalArgumentException("Usuario no encontrado")

        // 2. Actualizar nombre/email si vinieron
        val nuevoNombre = command.name ?: user.name
        val nuevoEmail = command.email ?: user.email
        userRepository.update(userId, nuevoNombre, nuevoEmail)

        // 3. Actualizar password si viene
        if (command.password != null) {
            val hash = BCrypt.hashpw(command.password, BCrypt.gensalt())
            userRepository.updateHash(userId, hash)
        }

        // 4. Actualizar roles si vienen
        if (command.roles != null) {
            userRolesRepository.eliminarRolesDeUsuario(userId)

            val roles = rolesRepository.obtenerRolesPorNombres(command.roles)
            if (roles.size != command.roles.size) {
                throw IllegalArgumentException("Uno o más roles no existen")
            }

            roles.forEach { role ->
                userRolesRepository.asignarRolAUsuario(userId, role.roleId)
            }
        }
    }
}

