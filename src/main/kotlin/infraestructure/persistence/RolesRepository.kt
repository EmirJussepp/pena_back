package com.example.infraestructure.persistence

import com.example.domain.contracts.RolesContract
import com.example.domain.dto.Role
import com.example.domain.entities.Roles
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class RolesRepository(private val db: Database): RolesContract {
    override fun create(name: String, description: String?): Int = transaction(db) {
        Roles.insert {
            it[Roles.name] = name
            it[Roles.description] = description
        } get Roles.roleId
    }


    override fun getIdByName(name: String): Int? = transaction(db) {
        Roles.slice(Roles.roleId).select { Roles.name eq name }.firstOrNull()?.get(Roles.roleId)
    }
    override fun list(): List<String> = transaction(db) {
        Roles.slice(Roles.name).selectAll().map { it[Roles.name] }
    }
     fun obtenerRolesPorNombres(nombres: List<String>): List<Role> = transaction(db) {
        Roles
            .select { Roles.name inList nombres }
            .map {
                Role(
                    roleId = it[Roles.roleId],
                    name = it[Roles.name],
                    description = it[Roles.description] // si tu tabla tiene descripción
                )
            }
    }
}

