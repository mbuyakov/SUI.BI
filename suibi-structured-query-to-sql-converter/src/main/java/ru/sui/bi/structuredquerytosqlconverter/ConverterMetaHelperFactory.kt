package ru.sui.bi.structuredquerytosqlconverter

import org.springframework.data.repository.findByIdOrNull
import ru.sui.bi.structuredquerytosqlconverter.exception.ConversionException
import ru.sui.suientity.entity.suimeta.TableInfo
import ru.sui.suientity.repository.suimeta.ColumnInfoRepository
import ru.sui.suientity.repository.suimeta.TableInfoRepository

class ConverterMetaHelperFactory(
    private val tableInfoRepository: TableInfoRepository,
    private val columnInfoRepository: ColumnInfoRepository
) {

    fun create(aliasMap: Map<String, Long>): ConverterMetaHelper {
        return ConverterMetaHelper(
            tableInfoRepository = tableInfoRepository,
            columnInfoRepository = columnInfoRepository,
            aliasMap = aliasMap
        )
    }

    fun getTableInfo(id: Long): TableInfo {
        return getTableInfoOrNull(id) ?: throw ConversionException("Не удалось найти таблицу с ИД = $id")
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun getTableInfoOrNull(id: Long): TableInfo? {
        return tableInfoRepository.findByIdOrNull(id)
    }

}