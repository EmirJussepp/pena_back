package com.example.application.security

import com.auth0.jwt.interfaces.Claim
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

private fun Claim.asStringList(): List<String> =
    if (this.isNull || this.isMissing) emptyList() else (this.asList(String::class.java) ?: emptyList())

private fun permsFrom(call: ApplicationCall): List<String> {
    val p = call.principal<JWTPrincipal>() ?: return emptyList()
    return p.payload.getClaim("perms").asStringList()
}

suspend fun requirePerm(call: ApplicationCall, required: String): Boolean {
    val ps = permsFrom(call)
    val ok = ps.contains(required) || ps.contains("*")
    if (!ok) call.respond(HttpStatusCode.Forbidden, "No tenés permiso: $required")
    return ok
}

suspend fun requireAny(call: ApplicationCall, vararg required: String): Boolean {
    val ps = permsFrom(call)
    val ok = ps.contains("*") || required.any { it in ps }
    if (!ok) call.respond(HttpStatusCode.Forbidden, "Permiso requerido: ${required.joinToString(" | ")}")
    return ok
}

// Azúcar
suspend fun requireView(call: ApplicationCall, resource: String) =
    requireAny(call, "$resource:ver", "$resource:gestionar")

suspend fun requireManage(call: ApplicationCall, resource: String) =
    requirePerm(call, "$resource:gestionar")
