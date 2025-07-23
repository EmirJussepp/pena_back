package com.example.application.command.Socios

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable



@Serializable
data class CreateSocioCommand(
    val nombre: String,
    val alias: String?,
    val apellido: String,
    val email: String,
    val dni: String,
    val numSocioBoca: Int?,
    val telefono: String,
    val cobradorId: Int,
    val tipoSocioPeñaId: Int,
    val tipoBocaId: Int,
    val userId: Int,
    val localidadId: Int,
    val estado: Boolean,
    val direccion: String?
) {
    fun validate(): List<String> {
        val errors = mutableListOf<String>()

        println("\uD83D\uDCC3 Recibiendo datos: $this")  // Depuración

        // Validaciones de campos obligatorios
        if (nombre.isBlank()) errors.add("El nombre es obligatorio")
        if (apellido.isBlank()) errors.add("El apellido es obligatorio")
        if (email.isBlank()) errors.add("El email es obligatorio")
        if (dni.isBlank()) errors.add("El DNI es obligatorio")

        // Validaciones de formato
        if (!isValidEmail(email)) errors.add("El email no tiene un formato válido")
//        if (!isValidDni(dni)) errors.add("El DNI no tiene un formato válido")

        // Validaciones de relaciones
        if (cobradorId <= 0) errors.add("El ID del cobrador es inválido")
        if (tipoSocioPeñaId <= 0) errors.add("El ID del tipo de socio peña es inválido")
        if (tipoBocaId <= 0) errors.add("El ID del tipo de boca es inválido")
        if (userId <= 0) errors.add("El ID del usuario es inválido")
        if (localidadId <= 0) errors.add("El ID de la localidad es inválido")

        return errors
    }

    private fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.contains(".")
    }
//
//    object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
//        private val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy'T'HH:mm:ss")
//
//        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)
//
//        override fun serialize(encoder: Encoder, value: LocalDateTime) {
//            encoder.encodeString(value.format(formatter))
//        }
//
//        override fun deserialize(decoder: Decoder): LocalDateTime {
//            return LocalDateTime.parse(decoder.decodeString(), formatter)
//        }
//    }
}
