package com.example.application.security

import at.favre.lib.crypto.bcrypt.BCrypt

object PasswordService {
    fun hash(plain: String): String =
        BCrypt.withDefaults().hashToString(12, plain.toCharArray())

    fun verify(plain: String, hash: String): Boolean =
        BCrypt.verifyer().verify(plain.toCharArray(), hash.toCharArray()).verified

    fun looksHashed(value: String): Boolean =
        value.startsWith("\$2a$") || value.startsWith("\$2b$") || value.startsWith("\$2y$")
}