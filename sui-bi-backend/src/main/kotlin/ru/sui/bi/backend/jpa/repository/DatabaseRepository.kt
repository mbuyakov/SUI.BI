package ru.sui.bi.backend.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.sui.bi.backend.jpa.entity.DatabaseEntity

interface DatabaseRepository : JpaRepository<DatabaseEntity, Long>