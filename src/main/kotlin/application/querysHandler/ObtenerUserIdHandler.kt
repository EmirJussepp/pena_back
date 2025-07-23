package com.example.application.querysHandler

import com.example.application.querys.ObtenerUserIdQuery
import com.example.application.querys.ObtenerUserPorNombreQuery
import com.example.domain.entities.User
import com.example.domain.contracts.IUserRepository

class ObtenerUserIdHandler(private val userRepository: IUserRepository) {
    fun handle(query: ObtenerUserIdQuery): User? {
        return userRepository.findById(query.userId)
    }
}

class ObtenerUserNameHandler(private val userRepository: IUserRepository) {
    fun handle(query: ObtenerUserPorNombreQuery): List<User> {
        return userRepository.findByName(query.name)
    }
}

