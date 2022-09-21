package ru.sui.bi.backend.jpa.entity

import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import javax.persistence.*

@Entity
@Table(schema = "sui_bi", name = "tables")
@EntityListeners(AuditingEntityListener::class)
class TableEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    var database: DatabaseEntity,
    var tableSchema: String?,
    var tableName: String,
    var tableType: String?
) {

    @CreatedDate
    var created: Instant? = null
        private set

    @CreatedBy
    @ManyToOne(fetch = FetchType.LAZY)
    var creator: UserEntity? = null
        private set

    @LastModifiedDate
    var lastModified: Instant? = null
        private set

    @LastModifiedBy
    @ManyToOne(fetch = FetchType.LAZY)
    var lastModifier: UserEntity? = null
        private set

}