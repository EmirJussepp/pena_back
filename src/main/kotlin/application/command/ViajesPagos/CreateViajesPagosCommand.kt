package com.example.application.command.ViajesPagos
import kotlinx.serialization.Serializable


@Serializable
data class CreateViajesPagosCommand(
    val viajeId: Int,
    val monto: Double,
    val nombre: String?,
    val apellido: String?,
    val dni: String?,
    val metodoPagoId: Int,
    val cobradoresId: Int
) {
    fun validate() {
        if (!nombre.isNullOrBlank() || !apellido.isNullOrBlank() || !dni.isNullOrBlank()) {
            require(!nombre.isNullOrBlank()) { "El nombre del no socio no puede estar vacío si se proporciona alguno de sus datos." }
            require(!apellido.isNullOrBlank()) { "El apellido del no socio no puede estar vacío si se proporciona alguno de sus datos." }
            require(!dni.isNullOrBlank()) { "El DNI del no socio no puede estar vacío si se proporciona alguno de sus datos." }

            require(dni.length in 7..10) { "El DNI debe tener entre 7 y 10 caracteres." }
            require(dni.all { it.isDigit() }) { "El DNI solo debe contener números." }
        }
    }
}
