package com.example.domain.contracts

data class UserAccess(val userId: Int, val email: String, val roles: List<String>, val permissions: List<String>)
interface AccessRepository {
    fun getAccessByEmail(email: String): UserAccess?
}