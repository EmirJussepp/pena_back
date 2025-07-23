package com.example.application.command.ViajesBombonera



import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Serializable
data class ViajeBomboneraCommand(
    @Serializable(with = LocalDateTimeSerializer::class) val fechaViaje: LocalDateTime,
    val destino: String,
) {
    // Validación del comando
    fun validate() {
        // Validación del destino
        if (destino.isEmpty()) {
            throw IllegalArgumentException("El destino no debe estar vacío.")
        }




    }

    object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
        private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        // Genera: "2024-06-24T14:00:00"

        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: LocalDateTime) {
            encoder.encodeString(value.format(formatter))
        }

        override fun deserialize(decoder: Decoder): LocalDateTime {
            return LocalDateTime.parse(decoder.decodeString(), formatter)
        }
    }

}
