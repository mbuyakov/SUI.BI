package ru.sui.bi.backend.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.sui.bi.backend.jpa.entity.DatabaseEngineEntity

interface DatabaseEngineRepository : JpaRepository<DatabaseEngineEntity, Long> {

    fun findByCode(code: String): DatabaseEngineEntity?

}