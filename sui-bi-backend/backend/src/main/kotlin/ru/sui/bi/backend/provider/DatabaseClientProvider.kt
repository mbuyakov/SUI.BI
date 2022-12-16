package ru.sui.bi.backend.provider

import ru.sui.bi.core.DatabaseClient
import ru.sui.bi.core.Query

interface DatabaseClientProvider {

    fun get(databaseId: Long): DatabaseClient<Query>

    fun invalidate(databaseId: Long)

}