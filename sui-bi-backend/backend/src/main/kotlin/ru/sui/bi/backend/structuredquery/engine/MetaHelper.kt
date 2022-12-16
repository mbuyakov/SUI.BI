package ru.sui.bi.backend.structuredquery.engine

import com.querydsl.core.types.Path
import com.querydsl.sql.RelationalPath
import ru.sui.bi.backend.jpa.entity.ColumnEntity
import ru.sui.bi.backend.jpa.entity.TableEntity
import ru.sui.bi.core.exception.SuiBiException

class MetaHelper {

    private class AliasMapValue(
        val table: TableEntity,
        val columns: List<ColumnEntity>,
        val relationalPath: MetaRelationalPath
    )

    private val aliasMap = mutableMapOf<String, AliasMapValue>()

    fun addTable(alias: String, table: TableEntity, columns: List<ColumnEntity>) {
        aliasMap[alias] = AliasMapValue(
            table = table,
            columns = columns,
            relationalPath = MetaRelationalPath(alias, table, columns)
        )
    }

    fun getTablePaths(): Collection<RelationalPath<*>> {
        return aliasMap.values.map { it.relationalPath }
    }

    fun getTablePath(alias: String): RelationalPath<*> {
        return getAliasMapValue(alias).relationalPath
    }

    fun getColumnPath(alias: String, columnId: Long): Path<*> {
        val aliasMapValue = getAliasMapValue(alias)

        val column = aliasMapValue.columns.firstOrNull { it.id == columnId }
            ?: throw SuiBiException("Не удалось найти колонку с ИД = $columnId в таблице с алиасом = $alias")

        return aliasMapValue.relationalPath.columns.firstOrNull { it.metadata.name == column.columnName }
            ?: throw SuiBiException("Не удалось найти колонку ${column.columnName} в таблице с алиасом = $alias")
    }

    private fun getAliasMapValue(alias: String): AliasMapValue {
        return aliasMap[alias] ?: throw SuiBiException("Не удалось найти таблицу с алиасом = $alias")
    }

}