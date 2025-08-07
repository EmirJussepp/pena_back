package com.example.infraestructure.persistence

import com.example.domain.contracts.IUserRepository

import com.example.domain.entities.User
import com.example.domain.entities.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class UserRepository(private val database: Database) : IUserRepository {

    init {
        transaction(database) {
            SchemaUtils.create(Users) // Crea la tabla si no existe
        }
    }

    override fun save(user: User) {
        transaction(database) {
            val existingUser = Users.select { Users.email eq user.email }.singleOrNull()

            if (existingUser == null) {
                // Insertar un nuevo usuario
                Users.insert {
                    it[name] = user.name
                    it[email] = user.email
                    it[password] = user.password
                }
            } else {
                // Actualizar usuario existente
                Users.update({ Users.email eq user.email }) {
                    it[name] = user.name
                    it[password] = user.password
                }
            }
        }
    }
    override fun findById(userId: Int): User? {
        return transaction(database) {
            Users.select { Users.userId eq userId }
                .map { row ->
                    User(
                        userId = row[Users.userId],
                        name = row[Users.name],
                        email = row[Users.email],
                        password = row[Users.password]
                    )
                }
                .singleOrNull() // Retorna un solo usuario o null si no existe
        }
    }
    override fun findByName(name: String): List<User> {
        return transaction(database) {
            Users.select { Users.name eq name }
                .map { row ->
                    User(
                        userId = row[Users.userId],
                        name = row[Users.name],
                        email = row[Users.email],
                        password = row[Users.password]
                    )
                }
        }
    }
    fun findByEmail(email: String): User? {
        return transaction(database) {
            Users.select { Users.email eq email }.mapNotNull {
                User(
                    userId = it[Users.userId],
                    name = it[Users.name],
                    email = it[Users.email],
                    password = it[Users.password] // hash almacenado
                )
            }.singleOrNull()
        }
    }
    override fun obtenerTodos(): List<User> {
        return transaction(database) {
            Users.selectAll().map {
                User(
                    userId = it[Users.userId],
                    name = it[Users.name],
                    email = it[Users.email],
                    password = it[Users.password]

                )
            }
        }
    }


}
