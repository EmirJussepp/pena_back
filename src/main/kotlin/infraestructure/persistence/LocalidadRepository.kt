package com.example.infraestructure.persistence

import com.example.domain.contracts.ILocalidadRepository
import com.example.domain.entities.Cobrador
import com.example.domain.entities.Cobradores
import com.example.domain.entities.Localidad
import com.example.domain.entities.Localidades
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class LocalidadRepository(private val database: Database) : ILocalidadRepository {

    init {
        transaction(database) {
            SchemaUtils.create(Localidades) // Crea la tabla si no existe
        }
    }

    override fun save(localidad: Localidad) {
        transaction(database) {
            val existingLocalidad = Localidades.select { Localidades.nombre eq localidad.nombre }.singleOrNull()

            if (existingLocalidad == null) {
                // Insertar una nueva localidad
                Localidades.insert {
                    it[nombre] = localidad.nombre
                    it[provincia] = localidad.provincia
                    it[codigoPostal] = localidad.codigoPostal
                }
            } else {
                // Actualizar localidad existente
                Localidades.update({ Localidades.nombre eq localidad.nombre }) {
                    it[provincia] = localidad.provincia
                    it[codigoPostal] = localidad.codigoPostal
                }
            }
        }
    }
    override fun findById(localidadId: Int): Localidad? {
        return transaction(database) {
            Localidades.select { Localidades.localidadId eq localidadId }
                .map { row ->
                    Localidad(
                        localidadId = row[Localidades.localidadId],
                        nombre = row[Localidades.nombre],
                        provincia = row[Localidades.provincia],
                        codigoPostal = row[Localidades.codigoPostal]
                    )
                }
                .singleOrNull() // Retorna una sola localidad o null si no existe
        }
    }
    override fun obtenerTodos(): List<Localidad> {
        return transaction(database) {
            Localidades.selectAll().map {
                Localidad(
                    localidadId = it[Localidades.localidadId],
                    nombre = it[Localidades.nombre],
                    provincia = it[Localidades.provincia],
                    codigoPostal = it[Localidades.codigoPostal]

                )
            }
        }
    }

}
