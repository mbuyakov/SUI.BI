package ru.sui.bi.backend.rest

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.sui.bi.backend.dto.QueryResultDto
import ru.sui.bi.backend.provider.DatabaseClientProvider
import ru.sui.bi.backend.unclassified.MetaSchemaUpdater
import ru.sui.bi.backend.unclassified.StructuredQueryParser
import ru.sui.bi.backend.unclassified.StructuredQueryValidator

@RestController
@RequestMapping("/api/metaSchema")
class MetaSchemaController(private val metaSchemaUpdater: MetaSchemaUpdater) {

    @PostMapping("/update")
    fun updateMetaSchema(@RequestParam("databaseId") databaseId: Long) {
        metaSchemaUpdater.update(databaseId)
    }

}