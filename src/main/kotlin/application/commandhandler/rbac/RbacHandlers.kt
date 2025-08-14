package com.example.application.commandhandler.rbac



import com.example.application.command.rbac.AssignRolesToUserCommand
import com.example.application.command.rbac.CreatePermissionCommand
import com.example.application.command.rbac.CreateRoleCommand
import com.example.application.command.rbac.GrantPermissionsToRoleCommand
import com.example.domain.contracts.*

class CreateRoleHandler(private val rolesRepo: RolesContract) {
    fun handle(cmd: CreateRoleCommand): Int {
        require(cmd.name.isNotBlank()) { "Nombre de rol requerido" }
        return rolesRepo.create(cmd.name.trim(), cmd.description?.trim())
    }
}

class CreatePermissionHandler(private val permsRepo: PermissionsRepository) {
    fun handle(cmd: CreatePermissionCommand): Int {
        require(cmd.code.isNotBlank()) { "Código de permiso requerido" }
        return permsRepo.create(cmd.code.trim(), cmd.description?.trim())
    }
}

class GrantPermissionsToRoleHandler(private val rolePermsRepo: RolePermissionsRepository) {
    fun handle(cmd: GrantPermissionsToRoleCommand) {
        require(cmd.roleName.isNotBlank()) { "Rol requerido" }
        require(cmd.permissions.isNotEmpty()) { "Permisos requeridos" }
        cmd.permissions.forEach { rolePermsRepo.grant(cmd.roleName, it) }
    }
}

class AssignRolesToUserHandler(private val userRolesRepo: UserRolesRepository) {
    fun handle(cmd: AssignRolesToUserCommand) {
        require(cmd.roles.isNotEmpty()) { "Roles requeridos" }
        cmd.roles.forEach { userRolesRepo.assign(cmd.userId, it) }
    }
}