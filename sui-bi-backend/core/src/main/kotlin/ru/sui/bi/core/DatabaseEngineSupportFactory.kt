package ru.sui.bi.core

import com.fasterxml.jackson.databind.node.ObjectNode

interface DatabaseEngineSupportFactory<C : DatabaseClient<*>> {

    fun getEngineCode(): String

    fun createClient(connectionDetails: ObjectNode): C

}