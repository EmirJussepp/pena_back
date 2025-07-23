package com.example.domain.contracts
import com.example.domain.entities.User
interface IUserRepository {
    fun save(user: User)

    fun findById(userId: Int): User? // Agregar este método

    fun findByName(name: String): List<User>
    fun obtenerTodos(): List<User>

//    fun createUser(name: String, email: String, password: String): User
//    fun getUserById(userId: Int): User?
//    fun getUserByEmail(email: String): User?
//    fun updateUser(userId: Int, name: String, email: String, password: String): Boolean
}
