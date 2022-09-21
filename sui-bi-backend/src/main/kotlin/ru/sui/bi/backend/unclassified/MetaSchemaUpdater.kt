package ru.sui.bi.backend.unclassified

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import ru.sui.bi.backend.core.domain.TableName
import ru.sui.bi.backend.core.enumeration.ColumnType
import ru.sui.bi.backend.core.exception.SuiBiException
import ru.sui.bi.backend.jpa.entity.ColumnEntity
import ru.sui.bi.backend.jpa.entity.ColumnTypeEntity
import ru.sui.bi.backend.jpa.entity.TableEntity
import ru.sui.bi.backend.jpa.repository.ColumnRepository
import ru.sui.bi.backend.jpa.repository.ColumnTypeRepository
import ru.sui.bi.backend.jpa.repository.DatabaseRepository
import ru.sui.bi.backend.jpa.repository.TableRepository
import ru.sui.bi.backend.provider.DatabaseEngineSupportFactoryProvider

// Временный, чтобы была хотя бы какая-нибудь метасхема
@Component
class MetaSchemaUpdater(
    private val databaseRepository: DatabaseRepository,
    private val tableRepository: TableRepository,
    private val columnRepository: ColumnRepository,
    private val columnTypeRepository: ColumnTypeRepository,
    private val engineSupportFactoryProvider: DatabaseEngineSupportFactoryProvider
) {

    @Transactional(rollbackFor = [Throwable::class])
    fun update(databaseId: Long) {
        val database = databaseRepository.findByIdOrNull(databaseId)
            ?: throw SuiBiException("Не удалось найти Database с ИД $databaseId")

        val engineSupportFactory = engineSupportFactoryProvider.get(database.engine.id)

        val databaseClient = engineSupportFactory.createClient(database.connectionDetails)

        // Пока что самая простая реализация
        tableRepository.deleteAllByDatabase(database)

        databaseClient.getTableMetaSchemas().forEach { tableMetaSchema ->
            val table = tableRepository.save(
                TableEntity(
                    database = database,
                    tableSchema = tableMetaSchema.schema,
                    tableName = tableMetaSchema.name,
                    tableType = tableMetaSchema.type
                )
            )

            val tableName = TableName(tableMetaSchema.schema, tableMetaSchema.name)

            val columnMetaSchemas = databaseClient.getColumnMetaSchemas(tableName)

            columnRepository.saveAll(
                columnMetaSchemas.map { columnMetaSchema ->
                    val columnTypeId = when (columnMetaSchema.type) {
                        ColumnType.BOOLEAN -> ColumnTypeEntity.ID_BOOLEAN
                        ColumnType.INTEGER -> ColumnTypeEntity.ID_INTEGER
                        ColumnType.DECIMAL -> ColumnTypeEntity.ID_DECIMAL
                        ColumnType.DATE -> ColumnTypeEntity.ID_DATE
                        ColumnType.TIMESTAMP -> ColumnTypeEntity.ID_TIMESTAMP
                        ColumnType.TIME -> ColumnTypeEntity.ID_TIME
                        ColumnType.STRING -> ColumnTypeEntity.ID_STRING
                        ColumnType.BINARY -> ColumnTypeEntity.ID_BINARY
                    }

                    return@map ColumnEntity(
                        table = table,
                        columnName = columnMetaSchema.name,
                        columnType = columnTypeRepository.getReferenceById(columnTypeId),
                        rawColumnType = columnMetaSchema.rawType,
                        isNullable = columnMetaSchema.isNullable
                    )
                }
            )
        }
    }

}