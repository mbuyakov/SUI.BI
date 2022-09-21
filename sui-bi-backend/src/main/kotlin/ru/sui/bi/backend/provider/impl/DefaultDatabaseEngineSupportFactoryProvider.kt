package ru.sui.bi.backend.provider.impl

import org.springframework.data.repository.findByIdOrNull
import ru.sui.bi.backend.core.DatabaseEngineSupportFactory
import ru.sui.bi.backend.core.exception.SuiBiException
import ru.sui.bi.backend.jpa.entity.DatabaseEngineEntity
import ru.sui.bi.backend.jpa.repository.DatabaseEngineRepository
import ru.sui.bi.backend.provider.DatabaseEngineSupportFactoryProvider

class DefaultDatabaseEngineSupportFactoryProvider(
    private val databaseEngineRepository: DatabaseEngineRepository,
    private val databaseEngineSupportFactories: List<DatabaseEngineSupportFactory>
) : DatabaseEngineSupportFactoryProvider {

    override fun get(id: Long): DatabaseEngineSupportFactory {
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

    override fun get(code: String): DatabaseEngineSupportFactory {
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

    private fun get(engine: DatabaseEngineEntity): DatabaseEngineSupportFactory {
        return databaseEngineSupportFactories.firstOrNull { it.getEngineCode() == engine.code }
            ?: throw SuiBiException("Engine \"${engine.name}\" не поддерживается")
    }

}