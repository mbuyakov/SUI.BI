package ru.sui.bi.backend.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sui.bi.backend.core.DatabaseEngineSupportFactory
import ru.sui.bi.backend.jpa.repository.DatabaseRepository
import ru.sui.bi.backend.jpa.repository.DatabaseEngineRepository
import ru.sui.bi.backend.provider.DatabaseClientProvider
import ru.sui.bi.backend.provider.DatabaseEngineSupportFactoryProvider
import ru.sui.bi.backend.provider.impl.DefaultDatabaseClientProvider
import ru.sui.bi.backend.provider.impl.DefaultDatabaseEngineSupportFactoryProvider

@Configuration
class ProviderConfiguration {

    @Bean
    fun engineSupportFactoryProvider(
        databaseEngineRepository: DatabaseEngineRepository,
        databaseEngineSupportFactories: List<DatabaseEngineSupportFactory>,
    ): DatabaseEngineSupportFactoryProvider {
        return DefaultDatabaseEngineSupportFactoryProvider(databaseEngineRepository, databaseEngineSupportFactories)
    }

    @Bean
    fun databaseClientProvider(
        databaseRepository: DatabaseRepository,
        databaseEngineSupportFactoryProvider: DatabaseEngineSupportFactoryProvider
    ): DatabaseClientProvider {
        return DefaultDatabaseClientProvider(databaseRepository, databaseEngineSupportFactoryProvider)
    }

}