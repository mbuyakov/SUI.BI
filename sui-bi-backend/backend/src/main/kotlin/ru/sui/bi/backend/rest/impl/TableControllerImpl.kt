package ru.sui.bi.backend.rest.impl

import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RestController
import ru.sui.bi.backend.dto.TableDto
import ru.sui.bi.backend.jpa.entity.TableEntity
import ru.sui.bi.backend.jpa.repository.TableRepository
import ru.sui.bi.backend.rest.TableController

@RestController
class TableControllerImpl(private val tableRepository: TableRepository) : TableController {

    @Transactional(readOnly = true)
    override fun getById(id: Long): ResponseEntity<TableDto> {
        return ResponseEntity.of(tableRepository.findById(id).map { convertTableEntityToTableDto(it) })
    }

    @Transactional(readOnly = true)
    override fun getByDatabaseId(databaseId: Long): List<TableDto> {
        return tableRepository.findAllByDatabase_Id(databaseId).map { convertTableEntityToTableDto(it) }
    }

    private fun convertTableEntityToTableDto(tableEntity: TableEntity): TableDto {
        return TableDto(
            id = tableEntity.id!!,
            created = tableEntity.created!!,
            creatorId = tableEntity.creator?.id,
            lastModified = tableEntity.lastModified!!,
            lastModifierId = tableEntity.lastModifier?.id,
            databaseId = tableEntity.database.id!!,
            tableSchema = tableEntity.tableSchema,
            tableName = tableEntity.tableName,
            tableType = tableEntity.tableType
        )
    }

}