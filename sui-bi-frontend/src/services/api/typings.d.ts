declare namespace API {
  type ColumnDto = {
    /** Идентификатор */
    id: number;
    /** Дата и время создания */
    created: string;
    /** Идентификатор создателя */
    creatorId?: number;
    /** Дата и время последнего изменения */
    lastModified: string;
    /** Идентификатор последнего изменившего */
    lastModifierId?: number;
    /** Идентификатор таблицы */
    tableId: number;
    /** Наименование */
    columnName: string;
    /** Тип */
    columnType:
      | 'BOOLEAN'
      | 'INTEGER'
      | 'DECIMAL'
      | 'STRING'
      | 'BINARY'
      | 'DATE'
      | 'TIME'
      | 'TIME_WITH_TIMEZONE'
      | 'TIMESTAMP'
      | 'TIMESTAMP_WITH_TIMEZONE'
      | 'OTHER';
    /** Сырой тип */
    rawColumnType: string;
    /** Значение может отсутствовать */
    nullable: boolean;
  };

  type ErrorDto = {
    /** Дата и время возникновения */
    timestamp: string;
    /** Статус */
    status: string;
    /** Код статуса */
    statusCode: number;
    /** Сообщение */
    message: string;
    /** Детальная информация */
    details: string;
  };

  type getColumnByIdParams = {
    /** Идентификатор колонки */
    id: number;
  };

  type getColumnsByTableIdParams = {
    /** Идентификатор таблицы */
    tableId: number;
  };

  type getTableByIdParams = {
    /** Идентификатор таблицы */
    id: number;
  };

  type getTablesByDatabaseIdParams = {
    /** Идентификатор БД */
    databaseId: number;
  };

  type QueryResultDto = {
    /** Список колонок */
    columns: string[];
    /** Данные */
    data: any[][];
  };

  type TableDto = {
    /** Идентификатор */
    id: number;
    /** Дата и время создания */
    created: string;
    /** Идентификатор создателя */
    creatorId?: number;
    /** Дата и время последнего изменения */
    lastModified: string;
    /** Идентификатор последнего изменившего */
    lastModifierId?: number;
    /** Идентификатор БД */
    databaseId: number;
    /** Схема */
    tableSchema?: string;
    /** Наименование */
    tableName: string;
    /** Тип */
    tableType?: string;
  };
}
