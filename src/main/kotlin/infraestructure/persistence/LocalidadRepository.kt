package com.example.infraestructure.persistence

import com.example.domain.contracts.ILocalidadRepository
import com.example.domain.entities.Localidad
import com.example.domain.entities.Localidades
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.exceptions.ExposedSQLException
import java.sql.ResultSet

class LocalidadRepository(private val database: Database) : ILocalidadRepository {

    init {
        transaction(database) {
            // Crea la tabla si no existe
            SchemaUtils.create(Localidades)

            // Asegurar índice UNIQUE (nombre, provincia)
            val exists = exec(
                """
                SELECT 1
                FROM INFORMATION_SCHEMA.STATISTICS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'localidades'
                  AND INDEX_NAME = 'ux_localidades_nombre_prov'
                """.trimIndent()
            ) { rs: ResultSet -> rs.next() } ?: false

            if (!exists) {
                exec("CREATE UNIQUE INDEX ux_localidades_nombre_prov ON localidades (nombre, provincia)")
            }
        }
    }

    override fun save(localidad: Localidad) {
        transaction(database) {
            val existing = Localidades
                .select { (Localidades.nombre eq localidad.nombre) and (Localidades.provincia eq localidad.provincia) }
                .singleOrNull()

            if (existing == null) {
                Localidades.insert {
                    it[nombre] = localidad.nombre
                    it[provincia] = localidad.provincia
                    it[codigoPostal] = localidad.codigoPostal
                }
            } else {
                Localidades.update({
                    (Localidades.nombre eq localidad.nombre) and (Localidades.provincia eq localidad.provincia)
                }) {
                    it[codigoPostal] = localidad.codigoPostal
                }
            }
        }
    }

    override fun findById(localidadId: Int): Localidad? = transaction(database) {
        Localidades
            .select { Localidades.localidadId eq localidadId }
            .map { row ->
                Localidad(
                    localidadId = row[Localidades.localidadId],
                    nombre = row[Localidades.nombre],
                    provincia = row[Localidades.provincia],
                    codigoPostal = row[Localidades.codigoPostal]
                )
            }
            .singleOrNull()
    }

    override fun obtenerTodos(): List<Localidad> = transaction(database) {
        Localidades
            .selectAll()
            .orderBy(Localidades.nombre, SortOrder.ASC)
            .map {
                Localidad(
                    localidadId = it[Localidades.localidadId],
                    nombre = it[Localidades.nombre],
                    provincia = it[Localidades.provincia],
                    codigoPostal = it[Localidades.codigoPostal]
                )
            }
    }

    override fun obtenerTodos(limit: Int): List<Localidad> = transaction(database) {
        Localidades
            .selectAll()
            .orderBy(Localidades.nombre, SortOrder.ASC)
            .limit(limit)
            .map {
                Localidad(
                    localidadId = it[Localidades.localidadId],
                    nombre = it[Localidades.nombre],
                    provincia = it[Localidades.provincia],
                    codigoPostal = it[Localidades.codigoPostal]
                )
            }
    }

    override fun buscar(q: String, limit: Int): List<Localidad> = transaction(database) {
        val like = "%${q.trim()}%"
        Localidades
            .select { (Localidades.nombre like like) or (Localidades.provincia like like) }
            .orderBy(Localidades.nombre, SortOrder.ASC)
            .limit(limit)
            .map {
                Localidad(
                    localidadId = it[Localidades.localidadId],
                    nombre = it[Localidades.nombre],
                    provincia = it[Localidades.provincia],
                    codigoPostal = it[Localidades.codigoPostal]
                )
            }
    }

    // ===== Upsert para /localidades/sync SIN JDBC crudo =====
    override fun upsertAndGetId(nombre: String, provincia: String, codigoPostal: String): Int =
        transaction(database) {
            val n = nombre.trim()
            val p = provincia.trim()
            val cp = codigoPostal.trim()

            // 1) Intentar insertar
            val insertedId = try {
                Localidades.insert {
                    it[Localidades.nombre] = n
                    it[Localidades.provincia] = p
                    it[Localidades.codigoPostal] = cp
                } get Localidades.localidadId
            } catch (e: ExposedSQLException) {
                // 23000 = unique violation en MySQL/MariaDB
                if (e.sqlState == "23000") null else throw e
            }

            if (insertedId != null) return@transaction insertedId

            // 2) Si ya existía, leer su id
            Localidades
                .slice(Localidades.localidadId)
                .select { (Localidades.nombre eq n) and (Localidades.provincia eq p) }
                .limit(1)
                .single()[Localidades.localidadId]
        }
}
