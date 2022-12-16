package ru.sui.bi.backend.provider.impl

import org.springframework.data.repository.findByIdOrNull
import ru.sui.bi.backend.jpa.entity.DatabaseEngineEntity
import ru.sui.bi.backend.jpa.repository.DatabaseEngineRepository
import ru.sui.bi.backend.provider.DatabaseEngineSupportFactoryProvider
import ru.sui.bi.core.DatabaseClient
import ru.sui.bi.core.DatabaseEngineSupportFactory
import ru.sui.bi.core.Query
import ru.sui.bi.core.exception.SuiBiException

class DefaultDatabaseEngineSupportFactoryProvider(
    private val databaseEngineRepository: DatabaseEngineRepository,
    private val databaseEngineSupportFactories: List<DatabaseEngineSupportFactory<DatabaseClient<Query>>>
) : DatabaseEngineSupportFactoryProvider {

    override fun get(id: Long): DatabaseEngineSupportFactory<DatabaseClient<Query>> {
        try {
            val databaseEngine = databaseEngineRepository.findByIdOrNull(id)
                ?: throw SuiBiException("Не удалось найти Engine с ИД $id")

            return get(databaseEngine)
        } catch (exception: SuiBiException) {
            throw exception
        } catch (exception: Exception) {
            throw SuiBiException(exception.message, exception)
        }
    }

    override fun get(code: String): DatabaseEngineSupportFactory<DatabaseClient<Query>> {
        try {
            val databaseEngine = databaseEngineRepository.findByCode(code)
                ?: throw SuiBiException("Не удалось найти Engine с кодом $code")

            return get(databaseEngine)
        } catch (exception: SuiBiException) {
            throw exception
        } catch (exception: Exception) {
            throw SuiBiException(exception.message, exception)
        }
    }

    private fun get(engine: DatabaseEngineEntity): DatabaseEngineSupportFactory<DatabaseClient<Query>> {
        return databaseEngineSupportFactories.firstOrNull { it.getEngineCode() == engine.code }
            ?: throw SuiBiException("Engine \"${engine.name}\" не поддерживается")
    }

}