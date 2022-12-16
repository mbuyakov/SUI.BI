package ru.sui.bi.backend.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sui.bi.backend.jpa.repository.DatabaseEngineRepository
import ru.sui.bi.backend.jpa.repository.DatabaseRepository
import ru.sui.bi.backend.provider.DatabaseClientProvider
import ru.sui.bi.backend.provider.DatabaseEngineSupportFactoryProvider
import ru.sui.bi.backend.provider.impl.DefaultDatabaseClientProvider
import ru.sui.bi.backend.provider.impl.DefaultDatabaseEngineSupportFactoryProvider
import ru.sui.bi.core.DatabaseClient
import ru.sui.bi.core.DatabaseEngineSupportFactory
import ru.sui.bi.core.Query

@Configuration
class ProviderConfiguration {

    @Bean
    @Suppress("UNCHECKED_CAST")
    fun engineSupportFactoryProvider(
        databaseEngineRepository: DatabaseEngineRepository,
        databaseEngineSupportFactories: List<DatabaseEngineSupportFactory<*>>
    ): DatabaseEngineSupportFactoryProvider {
        return DefaultDatabaseEngineSupportFactoryProvider(
            databaseEngineRepository = databaseEngineRepository,
            databaseEngineSupportFactories = databaseEngineSupportFactories as List<DatabaseEngineSupportFactory<DatabaseClient<Query>>>
        )
    }

    @Bean
    fun databaseClientProvider(
        databaseRepository: DatabaseRepository,
        databaseEngineSupportFactoryProvider: DatabaseEngineSupportFactoryProvider
    ): DatabaseClientProvider {
        return DefaultDatabaseClientProvider(
            databaseRepository = databaseRepository,
            databaseEngineSupportFactoryProvider = databaseEngineSupportFactoryProvider
        )
    }

}