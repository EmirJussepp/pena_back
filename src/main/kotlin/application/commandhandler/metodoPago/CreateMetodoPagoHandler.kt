package com.example.application.commandhandler.metodoPago

import com.example.application.command.metodoPago.CreateMetodoPagoCommand
import com.example.domain.contracts.IMetodoPagoRepository
import com.example.domain.entities.metodoPago

class CreateMetodoPagoHandler (
    private val metodoPagoRepository :IMetodoPagoRepository
){
    fun handle(command: CreateMetodoPagoCommand){
        try {
            // Validamos el comando
            command.validate()

            // Creamos la localidad utilizando un método de fábrica
            val metodoPago = metodoPago.create(
                command.nombre,

            )

            // Guardamos la localidad en la base de datos
            metodoPagoRepository.save(metodoPago)

            println("✅ metodo pago creado exitosamente")
        } catch (e: IllegalArgumentException) {
            // Error en la validación
            println("❌ Error en los datos proporcionados: ${e.message}")
        } catch (e: Exception) {
            // Cualquier otro error inesperado
            println("⚠️ Error inesperado al crear el metodo pago: ${e.message}")
        }
    }
}