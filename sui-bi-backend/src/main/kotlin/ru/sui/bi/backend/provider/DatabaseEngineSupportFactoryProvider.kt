package ru.sui.bi.backend.provider

import ru.sui.bi.backend.core.DatabaseEngineSupportFactory

interface DatabaseEngineSupportFactoryProvider {

    fun get(id: Long): DatabaseEngineSupportFactory

    fun get(code: String): DatabaseEngineSupportFactory

}