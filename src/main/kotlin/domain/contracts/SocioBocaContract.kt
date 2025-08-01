package com.example.domain.contracts

import com.example.domain.entities.TipoSocioBoca
import com.example.domain.entities.TiposSocioBoca


interface ISocioBocaContract {
    fun save(tipoSocioBoca: TipoSocioBoca)
    fun findById(tipoSocioBocaId: Int) : TipoSocioBoca?

    fun obtenerTodos(): List<TipoSocioBoca>
    fun eliminar(tipoSocioBocaId: Int)
    fun actualizar(tipoSocioBoca: TipoSocioBoca)
}
