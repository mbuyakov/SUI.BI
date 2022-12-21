package ru.sui.bi.backend.rest.impl

import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RestController
import ru.sui.bi.backend.dto.ColumnDto
import ru.sui.bi.backend.jpa.entity.ColumnEntity
import ru.sui.bi.backend.jpa.entity.ColumnTypeEntity
import ru.sui.bi.backend.jpa.repository.ColumnRepository
import ru.sui.bi.backend.rest.ColumnController

@RestController
class ColumnControllerImpl(private val columnRepository: ColumnRepository) : ColumnController {

    @Transactional(readOnly = true)
    override fun getById(id: Long): ResponseEntity<ColumnDto> {
        return ResponseEntity.of(columnRepository.findById(id).map { convertColumnEntityToColumnDto(it) })
    }

    @Transactional(readOnly = true)
    override fun getByTableId(tableId: Long): List<ColumnDto> {
        return columnRepository.findAllByTable_Id(tableId).map { convertColumnEntityToColumnDto(it) }
    }

    private fun convertColumnEntityToColumnDto(columnEntity: ColumnEntity): ColumnDto {
        return ColumnDto(
            id = columnEntity.id!!,
            created = columnEntity.created!!,
            creatorId = columnEntity.creator?.id,
            lastModified = columnEntity.lastModified!!,
            lastModifierId = columnEntity.lastModifier?.id,
            tableId = columnEntity.table.id!!,
            columnName = columnEntity.columnName,
            columnType = ColumnTypeEntity.getColumnType(columnEntity.columnType),
            rawColumnType = columnEntity.rawColumnType,
            nullable = columnEntity.isNullable
        )
    }

}