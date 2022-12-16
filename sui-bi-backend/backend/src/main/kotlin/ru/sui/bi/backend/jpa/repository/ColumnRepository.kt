package ru.sui.bi.backend.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.sui.bi.backend.jpa.entity.ColumnEntity
import ru.sui.bi.backend.jpa.entity.TableEntity

interface ColumnRepository : JpaRepository<ColumnEntity, Long> {

    fun findAllByTable(table: TableEntity): List<ColumnEntity>

}