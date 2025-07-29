package com.example.infraestructure.persistence



import com.example.domain.Dto.SocioDTO
import com.example.domain.contracts.ISocioRepository
import com.example.domain.entities.*
import kotlinx.datetime.toKotlinLocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime


class SocioRepository(private val database: Database) : ISocioRepository {

    init {
        transaction(database) {
            SchemaUtils.create(Socios)
        }
    }

    override fun save(socio: Socio): Socio {
        return transaction(database) {

            val existingSocio = Socios.select { Socios.dni eq socio.dni }.singleOrNull()

            if (existingSocio == null) {
                // Insertar nuevo socio
                val insertedId = Socios.insert {
                    it[nombre] = socio.nombre
                    it[alias] = socio.alias
                    it[apellido] = socio.apellido
                    it[dni] = socio.dni
                    it[email] = socio.email
                    it[telefono] = socio.telefono
                    it[numSocioBoca] = socio.numSocioBoca
                    it[fechaInicio] =  LocalDateTime.now()
                    it[cobradorId] = socio.cobradorId
                    it[tipoSocioPeñaId] = socio.tipoSocioPeñaId
                    it[tipoBocaId] = socio.tipoBocaId
                    it[userId] = socio.userId
                    it[localidadId] = socio.localidadId
                    it[estado] = socio.estado
                    it[direccion]= socio.direccion
                } get Socios.socioId

                // Obtener el socio recién insertado
                Socios.select { Socios.socioId eq insertedId }.map { row ->
                    Socio(
                        socioId = row[Socios.socioId],
                        nombre = row[Socios.nombre],
                        alias = row[Socios.alias],
                        apellido = row[Socios.apellido],
                        dni = row[Socios.dni],
                        email = row[Socios.email],
                        telefono = row[Socios.telefono],
                        numSocioBoca = row[Socios.numSocioBoca],
                        fechaInicio = row[Socios.fechaInicio].toKotlinLocalDateTime(),
                        cobradorId = row[Socios.cobradorId],
                        tipoSocioPeñaId = row[Socios.tipoSocioPeñaId],
                        tipoBocaId = row[Socios.tipoBocaId],
                        userId = row[Socios.userId],
                        localidadId = row[Socios.localidadId],
                        estado = row[Socios.estado],
                        direccion = row[Socios.direccion]
                    )
                }.first()
            } else {
                // Actualizar socio existente
                Socios.update({ Socios.dni eq socio.dni }) {
                    it[nombre] = socio.nombre
                    it[alias] = socio.alias
                    it[apellido] = socio.apellido
                    it[email] = socio.email
                    it[telefono] = socio.telefono
                    it[numSocioBoca] = socio.numSocioBoca
                    it[fechaInicio] = LocalDateTime.now()
                    it[cobradorId] = socio.cobradorId
                    it[tipoSocioPeñaId] = socio.tipoSocioPeñaId
                    it[tipoBocaId] = socio.tipoBocaId
                    it[userId] = socio.userId
                    it[localidadId] = socio.localidadId
                    it[estado] = socio.estado
                    it[direccion]= socio.direccion
                }
                socio.copy(socioId = existingSocio[Socios.socioId])
            }
        }
    }


    override fun findById(socioId: Int): Socio? {
        return transaction(database) {
            Socios.select { Socios.socioId eq socioId }
                .map { row ->
                    Socio(
                        socioId = row[Socios.socioId],
                        nombre = row[Socios.nombre],
                        apellido = row[Socios.apellido],
                        alias = row[Socios.alias],
                        dni = row[Socios.dni],
                        numSocioBoca = row[Socios.numSocioBoca],
                        email = row[Socios.email],
                        telefono = row[Socios.telefono],
                        fechaInicio = row[Socios.fechaInicio].toKotlinLocalDateTime(),
                        cobradorId = row[Socios.cobradorId],
                        tipoSocioPeñaId = row[Socios.tipoSocioPeñaId],
                        tipoBocaId = row[Socios.tipoBocaId],
                        userId = row[Socios.userId],
                        localidadId = row[Socios.localidadId],
                        estado = row[Socios.estado],
                        fechaDeBaja = row[Socios.fechaDeBaja]?.toKotlinLocalDateTime(),
                        direccion = row[Socios.direccion]
                    )
                }
                .singleOrNull()
        }
    }
    override fun findByName(nombre: String): List<Socio> {
        return transaction(database) {
            Socios.select { Socios.nombre eq nombre }
                .map { row ->
                    Socio(
                        socioId = row[Socios.socioId],
                        nombre = row[Socios.nombre],
                        alias = row[Socios.alias],
                        apellido = row[Socios.apellido],
                        email = row[Socios.email],
                        dni = row[Socios.dni],
                        numSocioBoca = row[Socios.numSocioBoca],
                        telefono = row[Socios.telefono],
                        fechaInicio = row[Socios.fechaInicio].toKotlinLocalDateTime(),  // Asegúrate de usar el tipo correcto (LocalDateTime)
                        cobradorId = row[Socios.cobradorId],
                        tipoSocioPeñaId = row[Socios.tipoSocioPeñaId],
                        tipoBocaId = row[Socios.tipoBocaId],
                        userId = row[Socios.userId],
                        localidadId = row[Socios.localidadId],
                        estado = row[Socios.estado],
                        direccion = row[Socios.direccion]
                    )
                }
        }
    }
    override fun obtenerPaginadoYFiltrado(
        limit: Int,
        offset: Int,
        filtro: String?,
        estado: Boolean?
    ): SociosPage {
        return transaction(database) {
            val condiciones = mutableListOf<Op<Boolean>>()

            // Filtro de texto
            if (!filtro.isNullOrBlank()) {
                val filtroLike = "%${filtro.lowercase()}%"
                condiciones += (Socios.nombre.lowerCase() like filtroLike) or
                        (Socios.apellido.lowerCase() like filtroLike) or
                        (Socios.dni.castTo<String>(TextColumnType()).lowerCase() like filtroLike)
            }

            // Filtro por estado si se especifica
            if (estado != null) {
                condiciones += (Socios.estado eq estado)
            }

            val queryBase = Socios
                .leftJoin(Cobradores, { Socios.cobradorId }, { Cobradores.cobradoresId })
                .leftJoin(TiposSocioPeña, { Socios.tipoSocioPeñaId }, { TiposSocioPeña.tipoSocioPeñaId })
                .leftJoin(TiposSocioBoca, { Socios.tipoBocaId }, { TiposSocioBoca.tipoSocioBocaId })
                .leftJoin(Localidades, { Socios.localidadId }, { Localidades.localidadId })

            val queryFiltrada = if (condiciones.isNotEmpty()) {
                queryBase.select { condiciones.reduce { acc, op -> acc and op } }
            } else {
                queryBase.selectAll()
            }

            val total = queryFiltrada.count().toInt()

            val sociosPaginados = queryFiltrada
                .orderBy(Socios.socioId to SortOrder.ASC)
                .limit(limit, offset.toLong())
                .map(::mapRowToSocioDTO)

            SociosPage(
                total = total,
                socios = sociosPaginados,
                page = (offset / limit) + 1,
                size = limit
            )
        }
    }


    override fun obtenerSociosActivos(): List<Socio> {
        return transaction(database) {
            Socios.select {
                Socios.estado eq true and (Socios.fechaDeBaja.isNull())
            }.map { row ->
                Socio(
                    socioId = row[Socios.socioId],
                    nombre = row[Socios.nombre],
                    apellido = row[Socios.apellido],
                    alias = row[Socios.alias],
                    dni = row[Socios.dni],
                    numSocioBoca = row[Socios.numSocioBoca],
                    email = row[Socios.email],
                    telefono = row[Socios.telefono],
                    fechaInicio = row[Socios.fechaInicio].toKotlinLocalDateTime(),
                    cobradorId = row[Socios.cobradorId],
                    tipoSocioPeñaId = row[Socios.tipoSocioPeñaId],
                    tipoBocaId = row[Socios.tipoBocaId],
                    userId = row[Socios.userId],
                    localidadId = row[Socios.localidadId],
                    estado = row[Socios.estado],
                    fechaDeBaja = row[Socios.fechaDeBaja]?.toKotlinLocalDateTime(),
                    direccion = row[Socios.direccion]
                )
            }
        }
    }
fun darDeBajaPorId(socioId: Int): Boolean {
    return transaction(database) {
        val ahora = LocalDateTime.now()
        val actualizados = Socios.update({ Socios.socioId eq socioId }) {
            it[estado] = false
            it[fechaDeBaja] = ahora
        }
        println("Socios actualizados: $actualizados con fecha $ahora")
        actualizados > 0
    }
}

    override fun contarPorEstado(estado: Boolean): Int {
        return transaction(database) {
            Socios.select { Socios.estado eq estado }.count().toInt()
        }
    }


    fun reactivarSocio(socioId: Int): Boolean {
        return transaction(database) {
            Socios.update({ Socios.socioId eq socioId }) {
                it[estado] = true
                it[fechaDeBaja] = null
            } > 0
        }
    }

    override suspend fun update(socio: Socio): Socio {
        return transaction(database) {
            Socios.update({ Socios.socioId eq socio.socioId!! }) {
                it[nombre] = socio.nombre
                it[alias] = socio.alias
                it[apellido] = socio.apellido
                it[email] = socio.email
                it[dni] = socio.dni
                it[numSocioBoca] = socio.numSocioBoca
                it[telefono] = socio.telefono
                it[fechaInicio] = LocalDateTime.now()
                it[cobradorId] = socio.cobradorId
                it[tipoSocioPeñaId] = socio.tipoSocioPeñaId
                it[tipoBocaId] = socio.tipoBocaId
                it[userId] = socio.userId
                it[localidadId] = socio.localidadId
                it[estado] = socio.estado
                it[direccion] = socio.direccion
            }

            socio // devuelve el socio actualizado (ya recibido)
        }
    }



    fun eliminarPorId(socioId: Int): Boolean {
        return transaction(database) {
            // Primero eliminamos las cuotas asociadas si existen
            Cuotas.deleteWhere { Cuotas.socioId eq socioId }

            // Luego eliminamos el socio
            val deletedCount = Socios.deleteWhere { Socios.socioId eq socioId }
            deletedCount > 0
        }
    }

    override fun findByDni(dni: String): Socio? {
        return transaction(database) {
            Socios.select { Socios.dni eq dni }
                .map { row ->
                    Socio(
                        socioId = row[Socios.socioId],
                        nombre = row[Socios.nombre],
                        apellido = row[Socios.apellido],
                        alias = row[Socios.alias],
                        dni = row[Socios.dni],
                        numSocioBoca = row[Socios.numSocioBoca],
                        email = row[Socios.email],
                        telefono = row[Socios.telefono],
                        fechaInicio = row[Socios.fechaInicio].toKotlinLocalDateTime(),
                        cobradorId = row[Socios.cobradorId],
                        tipoSocioPeñaId = row[Socios.tipoSocioPeñaId],
                        tipoBocaId = row[Socios.tipoBocaId],
                        userId = row[Socios.userId],
                        localidadId = row[Socios.localidadId],
                        estado = row[Socios.estado],
                        fechaDeBaja = row[Socios.fechaDeBaja]?.toKotlinLocalDateTime(),
                        direccion = row[Socios.direccion]
                    )
                }
                .singleOrNull()
        }
    }
    private fun mapRowToSocio(row: ResultRow): Socio {
        return Socio(
            socioId = row[Socios.socioId],
            nombre = row[Socios.nombre],
            apellido = row[Socios.apellido],
            alias = row[Socios.alias],
            dni = row[Socios.dni],
            numSocioBoca = row[Socios.numSocioBoca],
            email = row[Socios.email],
            telefono = row[Socios.telefono],
            fechaInicio = row[Socios.fechaInicio].toKotlinLocalDateTime(),
            cobradorId = row[Socios.cobradorId],
            tipoSocioPeñaId = row[Socios.tipoSocioPeñaId],
            tipoBocaId = row[Socios.tipoBocaId],
            userId = row[Socios.userId],
            localidadId = row[Socios.localidadId],
            estado = row[Socios.estado],
            fechaDeBaja = row[Socios.fechaDeBaja]?.toKotlinLocalDateTime(),
            direccion = row[Socios.direccion]
        )
    }

    private fun mapRowToSocioDTO(row: ResultRow): SocioDTO {
        return SocioDTO(
            socioId = row[Socios.socioId],
            nombre = row[Socios.nombre],
            apellido = row[Socios.apellido],
            alias = row[Socios.alias],
            email = row[Socios.email],
            telefono = row[Socios.telefono],
            dni = row[Socios.dni],
            estado = row[Socios.estado],
            numSocioBoca = row[Socios.numSocioBoca],
            fechaInicio = row[Socios.fechaInicio].toKotlinLocalDateTime(),
            direccion = row[Socios.direccion],
            fechaDeBaja = row[Socios.fechaDeBaja]?.toKotlinLocalDateTime(),
            cobradorNombre = row[Cobradores.nombre] ?: "—",
            tipoPeñaNombre = row[TiposSocioPeña.nombre] ?: "—",
            tipoBocaNombre = row[TiposSocioBoca.nombre] ?: "—",
            localidadNombre = row[Localidades.nombre] ?: "—"
        )
    }

    override fun existsByDni(dni: String): Boolean =
        transaction { Socios.select { Socios.dni eq dni }.count() > 0 }

    override fun existsByNumSocioBoca(numSocioBoca: Int): Boolean =
        transaction { Socios.select { Socios.numSocioBoca eq numSocioBoca }.count() > 0 }

    override fun existsByTelefono(telefono: String): Boolean =
        transaction { Socios.select { Socios.telefono eq telefono }.count() > 0 }

    override fun existsByEmail(email: String): Boolean =
        transaction { Socios.select { Socios.email eq email }.count() > 0 }

    fun contarCuotasPagadas(socioId: Int): Int = transaction(database) {
        Cuotas.select { (Cuotas.socioId eq socioId) and (Cuotas.estado eq true) }.count().toInt()
    }
    fun tieneDeuda(socioId: Int): Boolean = transaction(database) {
        val ahora = LocalDateTime.now()
        Cuotas.select {
            (Cuotas.socioId eq socioId) and
                    (Cuotas.estado eq false) and
                    (Cuotas.fechaVencimiento less ahora)
        }.count() > 0
    }


}






