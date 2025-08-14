package com.example.domain.contracts

interface PermissionsRepository {
    fun create(code: String, description: String?): Int
    fun getIdByCode(code: String): Int?
    fun list(): List<String>
}