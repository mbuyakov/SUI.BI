package ru.sui.bi.backend.core

import com.fasterxml.jackson.databind.node.ObjectNode

interface DatabaseEngineSupportFactory {

    fun getEngineCode(): String

    fun createClient(connectionDetails: ObjectNode): DatabaseClient

}