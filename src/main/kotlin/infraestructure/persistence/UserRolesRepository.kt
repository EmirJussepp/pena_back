package com.example.infraestructure.persistence


import com.example.domain.contracts.IUserRolesRepository
import com.example.domain.entities.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class UserRolesRepository(private val db: Database): IUserRolesRepository {
    override fun assign(userId: Int, roleId: Int): Boolean = transaction(db) {
        val exists = UserRoles.select {
            (UserRoles.userId eq userId) and (UserRoles.roleId eq roleId)
        }.any()
        if (!exists) {
            UserRoles.insert {
                it[UserRoles.userId] = userId
                it[UserRoles.roleId] = roleId
            }
        }
        true
    }


    override fun getRolesForUser(userId: Int): List<String> = transaction(db) {
        (UserRoles innerJoin Roles).slice(Roles.name).select { UserRoles.userId eq userId }.map { it[Roles.name] }
    }
     fun eliminarRolesDeUsuario(userId: Int) = transaction {
        UserRoles.deleteWhere { UserRoles.userId eq userId }
    }

     fun asignarRolAUsuario(userId: Int, roleId: Int) = transaction {
        UserRoles.insert {
            it[UserRoles.userId] = userId
            it[UserRoles.roleId] = roleId
        }
    }
}