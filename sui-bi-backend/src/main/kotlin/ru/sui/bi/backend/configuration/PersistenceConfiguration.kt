package ru.sui.bi.backend.configuration

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import ru.sui.bi.backend.jpa.entity.ColumnEntity
import ru.sui.bi.backend.jpa.repository.ColumnRepository

@Configuration
@EntityScan(basePackageClasses = [ColumnEntity::class])
@EnableJpaRepositories(basePackageClasses = [ColumnRepository::class])
@EnableJpaAuditing
class PersistenceConfiguration