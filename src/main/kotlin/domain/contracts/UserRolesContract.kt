package com.example.domain.contracts

interface IUserRolesRepository {
    fun assign(userId: Int, roleId: Int): Boolean
    fun getRolesForUser(userId: Int): List<String>

}