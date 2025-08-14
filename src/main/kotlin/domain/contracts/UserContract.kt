package com.example.domain.contracts
import com.example.domain.entities.User

interface IUserRepository {
    fun save(user: User)
    fun findById(userId: Int): User? // Agregar este método
    fun findByEmail(email: String): User?
    fun findByName(name: String): List<User>
    fun obtenerTodos(): List<User>

    fun updateHash(userId: Int, newHash: String)        // <-- importante
    fun getHashAndIdByEmail(email: String): Pair<String, Int>?
}
