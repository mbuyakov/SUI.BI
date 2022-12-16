package ru.sui.bi.engine.postgresql

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.zaxxer.hikari.HikariDataSource
import org.postgresql.Driver
import org.springframework.boot.jdbc.DataSourceBuilder
import ru.sui.bi.core.DatabaseEngineSupportFactory
import ru.sui.bi.core.exception.SuiBiException
import java.util.*

class PostgresqlEngineSupportFactory : DatabaseEngineSupportFactory<PostgresqlDatabaseClient> {

    private val objectMapper = jacksonObjectMapper()

    override fun getEngineCode(): String {
        return "postgresql"
    }

    override fun createClient(connectionDetails: ObjectNode): PostgresqlDatabaseClient {
        try {
            val connectionConfig = objectMapper.treeToValue<PostgresqlDatabaseConnectionConfig>(connectionDetails)

            val datasource = DataSourceBuilder.create()
                .type(HikariDataSource::class.java)
                .url(connectionConfig.url)
                .username(connectionConfig.user)
                .password(connectionConfig.password)
                .driverClassName(Driver::class.qualifiedName)
                .build()

            datasource.poolName = "postgresql-${UUID.randomUUID()}"

            return PostgresqlDatabaseClient(datasource)
        } catch (exception: Exception) {
            throw SuiBiException(exception.message, exception)
        }
    }

}