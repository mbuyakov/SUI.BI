package ru.sui.bi.backend.postgresql

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.zaxxer.hikari.HikariDataSource
import org.postgresql.Driver
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.stereotype.Component
import ru.sui.bi.backend.core.DatabaseClient
import ru.sui.bi.backend.core.DatabaseEngineSupportFactory
import ru.sui.bi.backend.core.exception.SuiBiException
import ru.sui.bi.backend.jpa.repository.ColumnRepository
import ru.sui.bi.backend.jpa.repository.TableRepository
import java.util.*

// TODO: Вынести в автоконфигурацию при разбитии на модули
@Component
class PostgresqlEngineSupportFactory(
    // Костыли, т.к. StructuredQuery, передаваемый в клиенты содержит ИДшники (в будущем запрос будет раскрываться до передачи)
    private val tableRepository: TableRepository,
    private val columnRepository: ColumnRepository
) : DatabaseEngineSupportFactory {

    private val objectMapper = jacksonObjectMapper()

    override fun getEngineCode(): String {
        return "postgresql"
    }

    override fun createClient(connectionDetails: ObjectNode): DatabaseClient {
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

            return PostgresqlDatabaseClient(
                dataSource = datasource,
                tableRepository = tableRepository,
                columnRepository = columnRepository
            )
        } catch (exception: Exception) {
            throw SuiBiException(exception.message, exception)
        }
    }

}