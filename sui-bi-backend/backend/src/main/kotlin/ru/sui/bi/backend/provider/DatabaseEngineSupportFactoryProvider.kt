package ru.sui.bi.backend.provider

import ru.sui.bi.core.DatabaseClient
import ru.sui.bi.core.DatabaseEngineSupportFactory
import ru.sui.bi.core.Query

interface DatabaseEngineSupportFactoryProvider {

    fun get(id: Long): DatabaseEngineSupportFactory<DatabaseClient<Query>>

    fun get(code: String): DatabaseEngineSupportFactory<DatabaseClient<Query>>

}