package com.example.application.commandhandler.Socios

import com.example.application.command.Socios.CreateSocioCommand

import com.example.domain.entities.Socio
import com.example.domain.contracts.ISocioPeñaContract
import com.example.domain.contracts.ISocioRepository
import com.example.domain.contracts.ILocalidadRepository
import com.example.domain.contracts.ISocioBocaContract

import com.example.domain.contracts.ICobradorRepository
import com.example.domain.contracts.IUserRepository

import com.example.application.Service.CuotaService

import kotlinx.datetime.toKotlinLocalDateTime

import java.time.LocalDateTime





class CreateSocioHandler(
    private val socioRepository: ISocioRepository,
    private val localidadRepository: ILocalidadRepository,
    private val tipoSocioPenaRepository: ISocioPeñaContract,
    private val tipoSocioBocaRepository: ISocioBocaContract,
    private val cobradorRepository: ICobradorRepository,
    private val userRepository: IUserRepository,
    private val cuotaService: CuotaService
) {
    fun handle(command: CreateSocioCommand): Socio? {
        // 🔍 Validaciones
        val errors = command.validate()
        if (errors.isNotEmpty()) {
            throw IllegalArgumentException(errors.joinToString(", "))
        }

        if (socioRepository.existsByDni(command.dni)) throw IllegalArgumentException("El DNI ya está registrado")
        if (command.numSocioBoca != null && socioRepository.existsByNumSocioBoca(command.numSocioBoca)) throw IllegalArgumentException("El número de socio Boca ya está registrado")
        if (socioRepository.existsByTelefono(command.telefono)) throw IllegalArgumentException("El teléfono ya está registrado")
        if (socioRepository.existsByEmail(command.email)) throw IllegalArgumentException("El email ya está registrado")

        if (cobradorRepository.findById(command.cobradorId) == null)
            throw IllegalArgumentException("El cobrador con ID ${command.cobradorId} no existe")

        if (tipoSocioPenaRepository.findById(command.tipoSocioPeñaId) == null)
            throw IllegalArgumentException("El tipo de socio Peña con ID ${command.tipoSocioPeñaId} no existe")



        if (localidadRepository.findById(command.localidadId) == null)
            throw IllegalArgumentException("La localidad con ID ${command.localidadId} no existe")

        // 🧠 Crear socio
        val socio = Socio.create(
            nombre = command.nombre,
            alias = command.alias,
            apellido = command.apellido,
            email = command.email,
            dni = command.dni,
            numSocioBoca = command.numSocioBoca,
            telefono = command.telefono,
            fechaInicio = LocalDateTime.now().toKotlinLocalDateTime(),
            cobradorId = command.cobradorId,
            tipoSocioPeñaId = command.tipoSocioPeñaId,
            tipoBocaId = command.tipoBocaId,
            userId = command.userId,
            localidadId = command.localidadId,
            direccion = command.direccion
        )

        return try {
            val socioGuardado = socioRepository.save(socio)!!

            // 🧾 Generar cuota inicial desde el service
            cuotaService.generarCuotaParaNuevoSocio(socioGuardado)

            println("✅ Socio creado con cuota inicial generada.")
            socioGuardado
        } catch (e: Exception) {
            throw IllegalArgumentException("Error al guardar el socio o generar la cuota: ${e.message}", e)
        }
    }
}