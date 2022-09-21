package ru.sui.bi.backend.provider

import ru.sui.bi.backend.core.DatabaseClient

interface DatabaseClientProvider {

    fun get(databaseId: Long): DatabaseClient

    fun invalidate(databaseId: Long)

}