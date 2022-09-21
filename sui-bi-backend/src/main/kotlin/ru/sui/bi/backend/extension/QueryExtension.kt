package ru.sui.bi.backend.extension

import io.zeko.db.sql.Query
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.jvm.isAccessible

val Query.currentTable: String
    get() = getMember("currentTable")

// Мясо, но работает
fun Query.addField(name: String) {
    this.fields(*(this.toParts().fields[this.currentTable] ?: emptyArray()).plus(name))
}

@Suppress("UNCHECKED_CAST")
private fun <T> Query.getMember(name: String): T {
    val currentTableMember = Query::class.declaredMembers.first { it.name == name }
    currentTableMember.isAccessible = true
    return currentTableMember.call(this) as T
}