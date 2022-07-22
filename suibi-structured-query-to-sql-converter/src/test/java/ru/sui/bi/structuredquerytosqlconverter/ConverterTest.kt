package ru.sui.bi.structuredquerytosqlconverter

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.doReturn
import org.mockito.junit.jupiter.MockitoExtension
import ru.sui.bi.structuredquerytosqlconverter.dialect.PostgresConverterDialectHelper
import ru.sui.bi.structuredquerytosqlconverter.model.*
import ru.sui.suientity.entity.suimeta.ColumnInfo
import ru.sui.suientity.entity.suimeta.TableInfo
import ru.sui.suientity.repository.suimeta.ColumnInfoRepository
import ru.sui.suientity.repository.suimeta.TableInfoRepository
import java.util.*

import org.junit.jupiter.api.Assertions.*

// И так сойдёт
@ExtendWith(MockitoExtension::class)
internal class ConverterTest(
    @Mock private val tableInfoRepository: TableInfoRepository,
    @Mock private val columnInfoRepository: ColumnInfoRepository
) {

    // Пример от Галины
    @Suppress("SameParameterValue")
    @Test
    fun conversionTest() {
        // Создаем объекты, представляющие таблицы
        val smaSmevRequestTableInfo = createTableInfo(617, "public", "sma_smev_request")
        val smaSmevResponseTableInfo = createTableInfo(619, "public", "sma_smev_response")
        val fctRequestsTableInfo = createTableInfo(656, "public", "fct_requests")

        // Создаем объекты, представляющие колонки
        val smaSmevRequestTaskIdColumnInfo = createColumnInfo(9983, smaSmevRequestTableInfo, "task_id")
        val smaSmevRequestIdColumnInfo = createColumnInfo(9980, smaSmevRequestTableInfo, "id")
        val smaSmevRequestRecipientSystemColumnInfo = createColumnInfo(9988, smaSmevRequestTableInfo, "recipient_system")
        val smaSmevRequestPlangetdateColumnInfo = createColumnInfo(9986, smaSmevRequestTableInfo, "plangetdate")
        val smaSmevRequestConsumerIdColumnInfo = createColumnInfo(9987, smaSmevRequestTableInfo, "consumer_id")
        val smaSmevRequestSupplierIdColumnInfo = createColumnInfo(9977, smaSmevRequestTableInfo, "supplier_id")
        val smaSmevResponseTaskIdColumnInfo = createColumnInfo(10009, smaSmevResponseTableInfo, "task_id")
        val smaSmevResponseResponseMessageIdColumnInfo = createColumnInfo(10006, smaSmevResponseTableInfo, "response_message_id")
        val smaSmevResponseFactgetdateColumnInfo = createColumnInfo(10104, smaSmevResponseTableInfo, "factgetdate")
        val fctRequestsTaskIdColumnInfo = createColumnInfo(10113, fctRequestsTableInfo, "task_id")
        val fctRequestsServiceKodColumnInfo = createColumnInfo(10008, fctRequestsTableInfo, "service_kod")

        // Обучаем моки (tableInfoRepository)
        doReturn(Optional.of(smaSmevRequestTableInfo)).`when`(tableInfoRepository).findById(smaSmevRequestTableInfo.id!!)
        doReturn(Optional.of(smaSmevResponseTableInfo)).`when`(tableInfoRepository).findById(smaSmevResponseTableInfo.id!!)
        doReturn(Optional.of(fctRequestsTableInfo)).`when`(tableInfoRepository).findById(fctRequestsTableInfo.id!!)

        // Обучаем моки (columnInfoRepository)
        doReturn(Optional.of(smaSmevRequestTaskIdColumnInfo)).`when`(columnInfoRepository).findById(smaSmevRequestTaskIdColumnInfo.id!!)
        doReturn(Optional.of(smaSmevRequestIdColumnInfo)).`when`(columnInfoRepository).findById(smaSmevRequestIdColumnInfo.id!!)
        doReturn(Optional.of(smaSmevRequestRecipientSystemColumnInfo)).`when`(columnInfoRepository).findById(smaSmevRequestRecipientSystemColumnInfo.id!!)
        doReturn(Optional.of(smaSmevRequestPlangetdateColumnInfo)).`when`(columnInfoRepository).findById(smaSmevRequestPlangetdateColumnInfo.id!!)
        doReturn(Optional.of(smaSmevRequestConsumerIdColumnInfo)).`when`(columnInfoRepository).findById(smaSmevRequestConsumerIdColumnInfo.id!!)
        doReturn(Optional.of(smaSmevRequestSupplierIdColumnInfo)).`when`(columnInfoRepository).findById(smaSmevRequestSupplierIdColumnInfo.id!!)
        doReturn(Optional.of(smaSmevResponseTaskIdColumnInfo)).`when`(columnInfoRepository).findById(smaSmevResponseTaskIdColumnInfo.id!!)
        doReturn(Optional.of(smaSmevResponseResponseMessageIdColumnInfo)).`when`(columnInfoRepository).findById(smaSmevResponseResponseMessageIdColumnInfo.id!!)
        doReturn(Optional.of(smaSmevResponseFactgetdateColumnInfo)).`when`(columnInfoRepository).findById(smaSmevResponseFactgetdateColumnInfo.id!!)
        doReturn(Optional.of(fctRequestsTaskIdColumnInfo)).`when`(columnInfoRepository).findById(fctRequestsTaskIdColumnInfo.id!!)
        doReturn(Optional.of(fctRequestsServiceKodColumnInfo)).`when`(columnInfoRepository).findById(fctRequestsServiceKodColumnInfo.id!!)

        // Конвертируем
        val converter = createConverter()

        val structuredQuery = StructuredQuery(
            database = 2L,
            query = Query(
                sourceTable = 617,
                joins = listOf(
                    Join(
                        strategy = Join.Strategy.LEFT_JOIN,
                        sourceTable = 619,
                        leftOn = Join.On(field = 9983),
                        rightOn = Join.On(field = 10009, alias = "sma_smev_response"),
                        alias = "sma_smev_response"
                    ),
                    Join(
                        strategy = Join.Strategy.INNER_JOIN,
                        sourceTable = 656,
                        leftOn = Join.On(field = 9983),
                        rightOn = Join.On(field = 10113, alias = "fct_requests"),
                        alias = "fct_requests"
                    )
                ),
                filter = PredicateTree(
                    not = false,
                    predicate = PredicateTree.Predicate.OR,
                    nodes = listOf(
                        PredicateTree.Node.Value(
                            Filter(
                                field = 9980,
                                operation = Filter.Operation.GREATER_THAN_OR_EQUAL,
                                value = listOf("30")
                            )
                        ),
                        PredicateTree.Node.Value(
                            Filter(
                                field = 9988,
                                operation = Filter.Operation.IN,
                                value = listOf("9000052", "9000053", "9000054")
                            )
                        ),
                        PredicateTree.Node.SubTree(
                            PredicateTree(
                                not = false,
                                predicate = PredicateTree.Predicate.AND,
                                nodes = listOf(
                                    PredicateTree.Node.Value(
                                        Filter(
                                            field = 10006,
                                            operation = Filter.Operation.NOT_EMPTY,
                                            joinAlias = "sma_smev_response"
                                        )
                                    ),
                                    PredicateTree.Node.Value(
                                        Filter(
                                            field = 10008,
                                            operation = Filter.Operation.EMPTY,
                                            joinAlias = "fct_requests"
                                        )
                                    )
                                )
                            )
                        )
                    )
                ),
                aggregation = listOf(
                    Aggregation(aggFunction = "count", fieldAlias = "count"),
                    Aggregation(aggFunction = "min", field = 9986, fieldAlias = "min"),
                    Aggregation(aggFunction = "min", field = 10104, joinAlias = "sma_smev_response", fieldAlias = "min_1")
                ),
                groupBy = listOf(
                    GroupBy(field = 9987),
                    GroupBy(field = 9977),
                    GroupBy(field = 10104, joinAlias = "sma_smev_response")
                ),
                limit = 1000,
                orderBy = listOf(
                    OrderBy(order = OrderBy.Direction.ASC, fieldAlias = "count"),
                    OrderBy(order = OrderBy.Direction.ASC, field = 9987),
                    OrderBy(order = OrderBy.Direction.DESC, field = 10104, joinAlias = "sma_smev_response"),
                    OrderBy(order = OrderBy.Direction.DESC, fieldAlias = "min_1")
                )
            ),
            type = StructuredQuery.Type.QUERY
        )

        val result = converter.convert(structuredQuery)

        // Проверяем полученное
        val targetResult = """
            SELECT COUNT(*)                               AS "count",
                   MIN("sma_smev_request"."plangetdate")  AS "min",
                   MIN("sma_smev_response"."factgetdate") AS "min_1",
                   "sma_smev_request"."consumer_id"       AS "consumer_id",
                   "sma_smev_request"."supplier_id"       AS "supplier_id",
                   "sma_smev_response"."factgetdate"      AS "factgetdate"
            FROM "public"."sma_smev_request"
            LEFT JOIN "public"."sma_smev_response" AS "sma_smev_response" ON (("sma_smev_request"."task_id" = "sma_smev_response"."task_id"))
            INNER JOIN "public"."fct_requests" AS "fct_requests" ON (("sma_smev_request"."task_id" = "fct_requests"."task_id"))
            WHERE (("sma_smev_request"."id" >= '30') OR
                   (("sma_smev_request"."recipient_system" IN ('9000052', '9000053', '9000054'))) OR
                   (((("sma_smev_response"."response_message_id" IS NOT NULL AND
                       ("sma_smev_response"."response_message_id")::TEXT != '')) AND
                     (("fct_requests"."service_kod" IS NULL OR ("fct_requests"."service_kod")::TEXT = '')))))
            GROUP BY "sma_smev_request"."consumer_id", "sma_smev_request"."supplier_id", "sma_smev_response"."factgetdate"
            ORDER BY "count" ASC, "sma_smev_request"."consumer_id" ASC, "sma_smev_response"."factgetdate" DESC, "min_1" DESC
            LIMIT 1000 OFFSET 0
        """.trimIndent()

        // Лучшее, что смог придумать
        assertTrue(result.replace(Regex("\\s+"), "") == targetResult.replace(Regex("\\s+"), ""))
    }

    private fun createConverter(): Converter {
        val dialectHelperRegistry = ConverterDialectHelperRegistry(PostgresConverterDialectHelper())
        val metaHelperFactory = ConverterMetaHelperFactory(tableInfoRepository, columnInfoRepository)

        return Converter(dialectHelperRegistry, metaHelperFactory)
    }

    @Suppress("SameParameterValue")
    private fun createTableInfo(id: Long, schemaName: String, tableName: String): TableInfo {
        return TableInfo.builder()
            .id(id)
            .schemaName(schemaName)
            .tableName(tableName)
            .build()
    }

    private fun createColumnInfo(id: Long, tableInfo: TableInfo, columnName: String): ColumnInfo {
        return ColumnInfo.builder()
            .id(id)
            .tableInfo(tableInfo)
            .columnName(columnName)
            .build()
    }

}