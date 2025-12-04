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
        // 1) Verificar usuario
        val user = userRepository.findById(userId)
            ?: throw IllegalArgumentException("Usuario no encontrado")

        // 2) Actualizar nombre/email si vinieron (ignorar strings vacías)
        val nuevoNombre = command.name?.trim().takeUnless { it.isNullOrEmpty() } ?: user.name
        val nuevoEmail  = command.email?.trim().takeUnless { it.isNullOrEmpty() } ?: user.email
        if (nuevoNombre != user.name || nuevoEmail != user.email) {
            userRepository.update(userId, nuevoNombre, nuevoEmail)
        }

        // 3) Actualizar password SOLO si vino no vacía (mantener la anterior si null/blank)
        command.password?.let { raw ->
            val pwd = raw.trim()
            if (pwd.isNotEmpty()) {
                require(pwd.length >= 7) { "La contraseña debe tener al menos 7 caracteres" }
                val hash = BCrypt.hashpw(pwd, BCrypt.gensalt(12)) // cost 12 recomendado
                userRepository.updateHash(userId, hash)
            }
        }

        // 4) Actualizar roles si vinieron (si la lista es vacía => dejar sin roles)
        command.roles?.let { nombresRoles ->
            // Validar que existan
            val roles = rolesRepository.obtenerRolesPorNombres(nombresRoles)
            require(roles.size == nombresRoles.size) { "Uno o más roles no existen" }

            // Reemplazo atómico: primero borro, luego asigno
            userRolesRepository.eliminarRolesDeUsuario(userId)
            roles.forEach { role ->
                userRolesRepository.asignarRolAUsuario(userId, role.roleId)
            }
        }
    }
}
