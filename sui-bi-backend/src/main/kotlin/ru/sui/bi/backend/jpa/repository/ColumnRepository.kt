package ru.sui.bi.backend.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.sui.bi.backend.jpa.entity.ColumnEntity

interface ColumnRepository : JpaRepository<ColumnEntity, Long>