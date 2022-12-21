package ru.sui.bi.backend.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import ru.sui.bi.backend.jpa.entity.DatabaseEntity
import ru.sui.bi.backend.jpa.entity.TableEntity

@Suppress("FunctionName")
interface TableRepository : JpaRepository<TableEntity, Long> {

    fun findAllByDatabase(database: DatabaseEntity): List<TableEntity>

    fun findAllByDatabase_Id(databaseId: Long): List<TableEntity>

    @Modifying
    @Query("DELETE FROM TableEntity t WHERE t.database = :database")
    fun deleteAllByDatabase(@Param("database") database: DatabaseEntity): Int

}