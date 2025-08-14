// com/example/infraestructure/persistence/UserRepository.kt
package com.example.infraestructure.persistence

import com.example.domain.contracts.IUserRepository
import com.example.domain.entities.User
import com.example.domain.entities.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class UserRepository(private val database: Database) : IUserRepository {

    init {
        transaction(database) { SchemaUtils.create(Users) }
    }

    override fun save(user: User) {
        transaction(database) {
            val existing = Users.select { Users.email eq user.email }.singleOrNull()
            if (existing == null) {
                Users.insert {
                    it[name] = user.name
                    it[email] = user.email
                    it[passwordHash] = user.passwordHash
                }
            } else {
                Users.update({ Users.email eq user.email }) {
                    it[name] = user.name
                    it[passwordHash] = user.passwordHash
                }
            }
        }
    }

    override fun findById(userId: Int): User? = transaction(database) {
        Users.select { Users.userId eq userId }.map {
            User(
                userId = it[Users.userId],
                name = it[Users.name],
                email = it[Users.email],
                passwordHash = it[Users.passwordHash]
            )
        }.singleOrNull()
    }

    override fun findByName(name: String): List<User> = transaction(database) {
        Users.select { Users.name eq name }.map {
            User(
                userId = it[Users.userId],
                name = it[Users.name],
                email = it[Users.email],
                passwordHash = it[Users.passwordHash]
            )
        }
    }

    override fun findByEmail(email: String): User? = transaction(database) {
        Users.select { Users.email eq email }.mapNotNull {
            User(
                userId = it[Users.userId],
                name = it[Users.name],
                email = it[Users.email],
                passwordHash = it[Users.passwordHash]
            )
        }.singleOrNull()
    }

    override fun obtenerTodos(): List<User> = transaction(database) {
        Users.selectAll().map {
            User(
                userId = it[Users.userId],
                name = it[Users.name],
                email = it[Users.email],
                passwordHash = it[Users.passwordHash]
            )
        }
    }

    override fun updateHash(userId: Int, newHash: String) {
        transaction(database) {
            Users.update({ Users.userId eq userId }) {
                it[passwordHash] = newHash
            }
        }
    }

    override fun getHashAndIdByEmail(email: String): Pair<String, Int>? = transaction(database) {
        Users.slice(Users.userId, Users.passwordHash)
            .select { Users.email eq email }
            .firstOrNull()
            ?.let { it[Users.passwordHash] to it[Users.userId] }
    }
}
