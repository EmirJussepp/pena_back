package com.example.infraestructure.persistence

import com.example.domain.contracts.PermissionsRepository
import com.example.domain.entities.Permissions

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class PermissionsRepository(private val db: Database): PermissionsRepository {
    override fun create(code: String, description: String?): Int = transaction(db) {
      Permissions.insert {
            it[Permissions.code] = code
            it[Permissions.description] = description
        } get Permissions.permissionId

    }

    override fun getIdByCode(code: String): Int? = transaction(db) {
        Permissions.slice(Permissions.permissionId).select { Permissions.code eq code }.firstOrNull()?.get(Permissions.permissionId)
    }
    override fun list(): List<String> = transaction(db) {
        Permissions.slice(Permissions.code).selectAll().map { it[Permissions.code] }
    }
}