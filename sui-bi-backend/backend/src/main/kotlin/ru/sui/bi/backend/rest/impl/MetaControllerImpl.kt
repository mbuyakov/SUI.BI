package ru.sui.bi.backend.rest.impl

import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RestController
import ru.sui.bi.backend.jpa.entity.ColumnEntity
import ru.sui.bi.backend.jpa.entity.ColumnTypeEntity
import ru.sui.bi.backend.jpa.entity.TableEntity
import ru.sui.bi.backend.jpa.repository.ColumnRepository
import ru.sui.bi.backend.jpa.repository.ColumnTypeRepository
import ru.sui.bi.backend.jpa.repository.DatabaseRepository
import ru.sui.bi.backend.jpa.repository.TableRepository
import ru.sui.bi.backend.provider.DatabaseClientProvider
import ru.sui.bi.backend.rest.MetaController
import ru.sui.bi.core.domain.TableName
import ru.sui.bi.core.exception.SuiBiException
import ru.sui.bi.core.metaschema.ColumnMetaSchema
import ru.sui.bi.core.metaschema.TableMetaSchema

@RestController
class MetaControllerImpl(
    private val databaseClientProvider: DatabaseClientProvider,
    private val columnRepository: ColumnRepository,
    private val columnTypeRepository: ColumnTypeRepository,
    private val databaseRepository: DatabaseRepository,
    private val tableRepository: TableRepository
) : MetaController {

    // Пока игнорируем ООМ
    @Transactional(rollbackFor = [Throwable::class])
    override fun updateMeta(databaseId: Long) {
        val database = databaseRepository.findByIdOrNull(databaseId)
            ?: throw SuiBiException("Не удалось найти Database с ИД $databaseId")

        val tableMetaSchemas: List<TableMetaSchema>
        val columnMetaSchemas: Map<TableName, List<ColumnMetaSchema>>

        databaseClientProvider.get(databaseId).use { client ->
            tableMetaSchemas = client.getTableMetaSchemas()
            columnMetaSchemas = client.getColumnMetaSchemas(tableMetaSchemas.map { TableName(it.schema, it.name) })
        }

        val tables = tableRepository.saveAll(tableMetaSchemas.map {
            TableEntity(
                database = database,
                tableSchema = it.schema,
                tableName = it.name,
                tableType = it.type
            )
        })

        val tableByTableName = tables.associateBy { TableName(it.tableSchema, it.tableName) }

        columnRepository.saveAll(columnMetaSchemas.flatMap { (tableName, tableColumnMetaSchemas) ->
            val table = tableByTableName[tableName]!!

            return@flatMap tableColumnMetaSchemas.map {
                ColumnEntity(
                    table = table,
                    columnName = it.name,
                    columnType = columnTypeRepository.getReferenceById(ColumnTypeEntity.getId(it.type)),
                    rawColumnType = it.nativeType,
                    isNullable = it.isNullable
                )
            }
        })
    }

}