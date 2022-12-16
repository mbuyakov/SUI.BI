package ru.sui.bi.backend.jpa.entity

import com.fasterxml.jackson.databind.node.ObjectNode
import com.vladmihalcea.hibernate.type.json.JsonNodeBinaryType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import javax.persistence.*

@Entity
@Table(schema = "sui_bi", name = "databases")
@EntityListeners(AuditingEntityListener::class)
@TypeDef(name = "jsonb-node", typeClass = JsonNodeBinaryType::class)
class DatabaseEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    var engine: DatabaseEngineEntity,
    var name: String,
    @Type(type = "jsonb-node")
    @Column(columnDefinition = "json")
    var connectionDetails: ObjectNode,
    var description: String?,
    @Column(name = "timezone")
    var timeZone: String,
    var isFullySynchronized: Boolean
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