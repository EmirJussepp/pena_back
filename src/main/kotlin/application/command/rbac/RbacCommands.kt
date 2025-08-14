package com.example.application.command.rbac

data class CreateRoleCommand(val name: String, val description: String?)
data class CreatePermissionCommand(val code: String, val description: String?)
data class GrantPermissionsToRoleCommand(val roleName: String, val permissions: List<String>)
data class AssignRolesToUserCommand(val userId: Int, val roles: List<String>)