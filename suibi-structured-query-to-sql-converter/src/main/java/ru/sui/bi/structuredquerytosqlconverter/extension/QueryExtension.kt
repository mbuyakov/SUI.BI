package ru.sui.bi.structuredquerytosqlconverter.extension

import io.zeko.db.sql.Condition
import io.zeko.db.sql.Query
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.jvm.isAccessible

@Suppress("UNCHECKED_CAST")
private fun <T> Query.getMember(name: String): T {
    val currentTableMember = Query::class.declaredMembers.first { it.name == name }
    currentTableMember.isAccessible = true
    return currentTableMember.call(this) as T
}

val Query.currentTable: String
    get() = getMember("currentTable")

fun Query.fullJoin(table: String): Query {
    val tableToJoin = getMember<LinkedHashMap<String, ArrayList<Condition>>>("tableToJoin")
    tableToJoin["full-join-${table}"] = arrayListOf()
    return this
}

// Мясо, но работает
fun Query.addField(name: String) {
    this.fields(*(this.toParts().fields[this.currentTable] ?: emptyArray()).plus(name))
}