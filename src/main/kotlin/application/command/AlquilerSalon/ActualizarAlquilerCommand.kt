package com.example.application.command.AlquilerSalon



import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.math.BigDecimal
@Serializable
data class ActualizarAlquilerCommand(
    val alquilerId: Int,
    val salonId: Int,
    val nombre: String,
    val telefono: String,
    val fecha: LocalDateTime,
    val observaciones: String,
    @Contextual val monto: BigDecimal,
    val condicion: Boolean,
    val metodoPagoId: Int,
    val dni: String
) {
    fun validate() {
        require(nombre.isNotBlank()) { "El nombre no puede estar vacío" }
        require(telefono.length >= 8) { "Teléfono inválido" }


        require(dni.length in 7..10) { "DNI inválido" }
        // Podés agregar más validaciones si tenés reglas específicas
    }
}
