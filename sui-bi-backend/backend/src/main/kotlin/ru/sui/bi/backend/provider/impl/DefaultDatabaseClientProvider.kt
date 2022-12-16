package ru.sui.bi.backend.provider.impl

import mu.KotlinLogging
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Scheduled
import ru.sui.bi.backend.jpa.repository.DatabaseRepository
import ru.sui.bi.backend.provider.DatabaseClientProvider
import ru.sui.bi.backend.provider.DatabaseEngineSupportFactoryProvider
import ru.sui.bi.core.DatabaseClient
import ru.sui.bi.core.Query
import ru.sui.bi.core.exception.SuiBiException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

private val log = KotlinLogging.logger { }

class DefaultDatabaseClientProvider(
    private val databaseRepository: DatabaseRepository,
    private val databaseEngineSupportFactoryProvider: DatabaseEngineSupportFactoryProvider
) : DatabaseClientProvider {

    private val lockMap = ConcurrentHashMap<Long, ReentrantLock>()
    private val actualClientWrapperMap = ConcurrentHashMap<Long, DatabaseClientWrapper>()
    private val invalidatedClientWrappers = Collections.synchronizedList(mutableListOf<DatabaseClientWrapper>())

    override fun get(databaseId: Long): DatabaseClient<Query> {
        return getLock(databaseId).withLock {
            var clientWrapper = actualClientWrapperMap[databaseId]

            if (clientWrapper == null) {
                val client = createClient(databaseId)
                clientWrapper = DatabaseClientWrapper(databaseId, client)
                actualClientWrapperMap[databaseId] = clientWrapper
            }

            clientWrapper.incrementUsages()

            return@withLock object : DatabaseClient<Query> by clientWrapper.client {
                override fun close() {
                    clientWrapper.decrementUsages()
                }
            }
        }
    }

    override fun invalidate(databaseId: Long) {
        val clientWrapper = actualClientWrapperMap.remove(databaseId)

        if (clientWrapper != null) {
            invalidatedClientWrappers.add(clientWrapper)
        }
    }

    @Scheduled(fixedDelay = 5000)
    fun closeInvalidatedClients() {
        val clientWrappersToClose = mutableListOf<DatabaseClientWrapper>()

        // toArray (использующийся в toTypedArray) синхронизирован, так что не бахнет
        invalidatedClientWrappers.toTypedArray()
            .filterNot { it.hasUsages() }
            .groupBy { it.databaseId }
            .forEach { (databaseId, groupClientWrappers) ->
                // Пропускаем через getLock, чтобы не закрывать то, что возможно получается в методе get
                getLock(databaseId).withLock {
                    clientWrappersToClose.addAll(groupClientWrappers.filterNot { it.hasUsages() })
                }
            }

        // Закрываем
        clientWrappersToClose.forEach { clientWrapper ->
            try {
                clientWrapper.client.close()
                invalidatedClientWrappers.remove(clientWrapper)
            } catch (exception: Exception) {
                log.debug(exception) { "Не удалось закрыть клиента БД" }
            }
        }
    }

    private fun createClient(databaseId: Long): DatabaseClient<Query> {
        try {
            val database = databaseRepository.findByIdOrNull(databaseId)
                ?: throw SuiBiException("Не удалось найти Базу Данных с ИД $databaseId")

            val databaseEngineSupportFactory = databaseEngineSupportFactoryProvider.get(database.engine.code)

            return databaseEngineSupportFactory.createClient(database.connectionDetails)
        } catch (exception: SuiBiException) {
            throw exception
        } catch (exception: Exception) {
            throw SuiBiException(exception.message, exception)
        }
    }

    private fun getLock(databaseId: Long): ReentrantLock {
        val existingLock = lockMap[databaseId]

        if (existingLock != null) {
            return existingLock
        }

        synchronized(lockMap) {
            val existingLock2 = lockMap[databaseId]

            if (existingLock2 != null) {
                return existingLock2
            }

            val newLock = ReentrantLock()

            lockMap[databaseId] = newLock

            return newLock
        }
    }

    private class DatabaseClientWrapper(val databaseId: Long, val client: DatabaseClient<Query>) {

        private val usagesInternal: AtomicInteger = AtomicInteger(0)

        fun hasUsages(): Boolean {
            return usagesInternal.get() != 0
        }

        fun incrementUsages() {
            usagesInternal.incrementAndGet()
        }

        fun decrementUsages() {
            usagesInternal.getAndDecrement()
        }

    }

}