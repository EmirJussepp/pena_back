package com.example.domain.contracts

interface RolesContract {
    fun create(name: String, description: String?): Int
    fun getIdByName(name: String): Int?
    fun list(): List<String>
}

