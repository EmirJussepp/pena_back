package com.example.application.command.localidad


import kotlinx.serialization.Serializable

@Serializable
class CreateLocalidadCommand(
    val nombre: String,
    val provincia: String,
    val codigoPostal: String
) {

    fun validate(): CreateLocalidadCommand {
        if (nombre.isEmpty()) {
            throw IllegalArgumentException("El nombre de localidaad no debe ser vacio.")
        }
        if (provincia.isEmpty()) {
            throw IllegalArgumentException("El nombre de provincia no debe ser vacio.")
        }
        if (codigoPostal.isEmpty()) {
            throw IllegalArgumentException("El codigo postal no debe ser vacio.")
        }
//

        return this
    }


}
