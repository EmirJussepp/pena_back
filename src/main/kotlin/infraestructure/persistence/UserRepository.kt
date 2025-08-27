// com/example/infraestructure/persistence/UserRepository.kt
package com.example.infraestructure.persistence

import com.example.domain.contracts.IUserRepository
import com.example.domain.dto.UsuarioConRoles
import com.example.domain.dto.Role
import com.example.domain.entities.*

import org.jetbrains.exposed.sql.*

import org.jetbrains.exposed.sql.transactions.transaction

class UserRepository(private val database: Database) : IUserRepository {

    init {
        transaction(database) { SchemaUtils.create(Users) }
    }

    override fun save(user: User): User {
        return transaction(database) {
            val existingUser = Users.select { Users.email eq user.email }.singleOrNull()

            if (existingUser == null) {
                // Insertar nuevo usuario
                val insertedId = Users.insert {
                    it[name] = user.name
                    it[email] = user.email
                    it[passwordHash] = user.passwordHash
                } get Users.userId

                // Obtener el usuario recién insertado
                Users.select { Users.userId eq insertedId }.map { row ->
                    User(
                        userId = row[Users.userId],
                        name = row[Users.name],
                        email = row[Users.email],
                        passwordHash = row[Users.passwordHash]
                    )
                }.first()
            } else {
                // Actualizar usuario existente
                Users.update({ Users.email eq user.email }) {
                    it[name] = user.name
                    it[passwordHash] = user.passwordHash
                }
                user.copy(userId = existingUser[Users.userId])
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
        Users
            .select { Users.activo eq true } // 👈 solo usuarios activos
            .map {
                User(
                    userId = it[Users.userId],
                    name = it[Users.name],
                    email = it[Users.email],
                    passwordHash = it[Users.passwordHash]
                    // si querés, también podés traer el campo activo
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
    override fun obtenerTodosConRoles(): List<UsuarioConRoles> = transaction {
        val usuarios = Users
            .select { Users.activo eq true }   // 👈 filtro acá
            .map { it[Users.userId] to it }
            .toMap()

        val rolesMap = UserRoles.innerJoin(Roles)
            .selectAll()
            .groupBy({ it[UserRoles.userId] }) { it[Roles.roleId] to it[Roles.name] }

        usuarios.map { (userId, row) ->
            UsuarioConRoles(
                userId = userId,
                name = row[Users.name],
                email = row[Users.email],
                password = "",
                roles = rolesMap[userId]?.map { (id, name) -> Role(id, name, null) } ?: emptyList()
            )
        }
    }

    override fun update(userId: Int, newName: String, newEmail: String) {
        transaction(database) {
            Users.update({ Users.userId eq userId }) {
                it[name] = newName
                it[email] = newEmail
            }
        }
    }
    override suspend fun eliminarPorId(userId: Int): Boolean {
        return transaction(database) {
            val updatedCount = Users.update({ Users.userId eq userId }) {
                it[activo] = false
            }
            updatedCount > 0
        }
    }





}
