package ru.sui.bi.backend.jpa.entity

import org.hibernate.annotations.Immutable
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(schema = "sui_bi", name = "engines")
@Immutable
class DatabaseEngineEntity(
    @Id
    val id: Long,
    val code: String,
    val name: String
)