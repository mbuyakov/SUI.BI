package ru.sui.bi.structuredquerytosqlconverter

import org.springframework.data.repository.findByIdOrNull
import ru.sui.bi.structuredquerytosqlconverter.exception.ConversionException
import ru.sui.suientity.entity.suimeta.ColumnInfo
import ru.sui.suientity.entity.suimeta.TableInfo
import ru.sui.suientity.repository.suimeta.ColumnInfoRepository
import ru.sui.suientity.repository.suimeta.TableInfoRepository

class ConverterMetaHelper(
    private val tableInfoRepository: TableInfoRepository,
    private val columnInfoRepository: ColumnInfoRepository,
    private val aliasMap: Map<String, Long>
) {

    fun getTable(alias: String): TableInfo {
        val id = aliasMap[alias] ?: throw ConversionException("Не удалось найти таблицу с алиасом = $alias")
        return getTableInfo(id)
    }

    private fun getTableInfoOrNull(id: Long): TableInfo? {
        return tableInfoRepository.findByIdOrNull(id)
    }

    private fun getTableInfo(id: Long): TableInfo {
        return getTableInfoOrNull(id) ?: throw ConversionException("Не удалось найти таблицу с ИД = $id")
    }

    fun getColumn(tableAlias: String, id: Long): ColumnInfo {
        val tableId = aliasMap[tableAlias] ?: throw ConversionException("Не удалось найти таблицу с алиасом = $tableAlias")
        return getColumnInfo(tableId, id)
    }

    private fun getColumnInfo(tableInfoId: Long, id: Long): ColumnInfo {
        return columnInfoRepository.findByIdOrNull(id)
            ?.takeIf { it.tableInfo.id == tableInfoId }
            ?: throw ConversionException("Не удалось найти поле с ИД = $id в таблице с ИД = $tableInfoId")
    }

}