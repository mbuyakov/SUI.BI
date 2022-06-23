--
-- Create table sui_meta.collection
--

CREATE TABLE sui_meta.collection
(
    id                BIGSERIAL NOT NULL PRIMARY KEY,
    name              TEXT      NOT NULL,
    description       TEXT,
    archived          BOOLEAN   NOT NULL DEFAULT FALSE,
    personal_owner_id BIGINT REFERENCES sui_security.users (id)
);

COMMENT ON TABLE sui_meta.collection IS 'Коллекции';
COMMENT ON COLUMN sui_meta.collection.id IS 'ИД (PK)';
COMMENT ON COLUMN sui_meta.collection.name IS 'Наименование коллекции';
COMMENT ON COLUMN sui_meta.collection.description IS 'Описание';
COMMENT ON COLUMN sui_meta.collection.archived IS 'В архиве';
COMMENT ON COLUMN sui_meta.collection.personal_owner_id IS 'Ссылка на юзера для эксклюзивной коллекции';

--
-- Create table sui_meta.sui_database
--

CREATE TABLE sui_meta.sui_database
(
    id                     BIGSERIAL   NOT NULL PRIMARY KEY,
    created                TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified               TIMESTAMPTZ NOT NULL DEFAULT now(),
    name                   TEXT        NOT NULL,
    description            TEXT,
    details                JSON,
    engine_id              BIGINT      NOT NULL REFERENCES sui_meta.engine (id),
    is_full_sync           BOOLEAN     NOT NULL,
    metadata_sync_schedule TEXT,
    timezone               TEXT        NOT NULL,
    is_on_demand           BOOLEAN     NOT NULL,
    auto_run_queries       BOOLEAN     NOT NULL,
    user_id                BIGINT REFERENCES sui_security.users (id)
);

COMMENT ON TABLE sui_meta.sui_database IS 'БД, поключенные к системе';
COMMENT ON COLUMN sui_meta.sui_database.id IS 'ИД (PK)';
COMMENT ON COLUMN sui_meta.sui_database.created IS 'Дата и время создания';
COMMENT ON COLUMN sui_meta.sui_database.modified IS 'Дата и время последнего изменения';
COMMENT ON COLUMN sui_meta.sui_database.name IS 'Наименование';
COMMENT ON COLUMN sui_meta.sui_database.description IS 'Описание';
COMMENT ON COLUMN sui_meta.sui_database.details IS 'Детали подключения к БД';
COMMENT ON COLUMN sui_meta.sui_database.engine_id IS 'Тип БД';
COMMENT ON COLUMN sui_meta.sui_database.is_full_sync IS 'БД синхронизирована и готова к использованию';
COMMENT ON COLUMN sui_meta.sui_database.metadata_sync_schedule IS 'Расписание для обновления метасхемы';
COMMENT ON COLUMN sui_meta.sui_database.timezone IS 'Tаймзона для данной БД';
COMMENT ON COLUMN sui_meta.sui_database.is_on_demand IS 'Whether we should do On-Demand caching of FieldValues for this DB. This means FieldValues are updated when their Field is used in a Dashboard or Card param.';
COMMENT ON COLUMN sui_meta.sui_database.auto_run_queries IS 'Перезапуск расчета запроса при выполнения фильтрации или каких то простых действий ';
COMMENT ON COLUMN sui_meta.sui_database.user_id IS 'Ссылка на пользователя, подключившего БД';

--
-- Create table sui_meta.report_card
--

CREATE TABLE sui_meta.report_card
(
    id                     BIGSERIAL   NOT NULL PRIMARY KEY,
    created                TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified               TIMESTAMPTZ NOT NULL DEFAULT now(),
    name                   TEXT        NOT NULL,
    description            TEXT,
    display                TEXT        NOT NULL,
    dataset_query          JSON        NOT NULL,
    visualization_settings JSON        NOT NULL,
    creator_id             BIGINT      NOT NULL REFERENCES sui_security.users (id),
    database_id            BIGINT      NOT NULL REFERENCES sui_meta.sui_database (id),
    table_id               BIGINT      NOT NULL REFERENCES sui_meta.table_info (id),
    query_type             TEXT,
    archived               BOOLEAN     NOT NULL DEFAULT FALSE,
    collection_id          BIGINT REFERENCES sui_meta.collection (id),
    public_uuid            UUID UNIQUE,
    made_public_by_id      INT,
    enable_embedding       BOOLEAN     NOT NULL DEFAULT FALSE,
    embedding_params       JSON,
    cache_ttl              INT,
    result_metadata        JSON,
    collection_position    SMALLINT,
    dataset                BOOLEAN     NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE sui_meta.report_card IS 'Виджеты';
COMMENT ON COLUMN sui_meta.report_card.id IS 'ИД (PK)';
COMMENT ON COLUMN sui_meta.report_card.created IS 'Дата и время создания';
COMMENT ON COLUMN sui_meta.report_card.modified IS 'Дата и время последнего изменения';
COMMENT ON COLUMN sui_meta.report_card.name IS 'Наименование';
COMMENT ON COLUMN sui_meta.report_card.description IS 'Описание';
COMMENT ON COLUMN sui_meta.report_card.display IS 'Представление';
COMMENT ON COLUMN sui_meta.report_card.dataset_query IS 'Запрос';
COMMENT ON COLUMN sui_meta.report_card.visualization_settings IS 'Настройки визуализации';
COMMENT ON COLUMN sui_meta.report_card.creator_id IS 'Пользователь, создавший запрос';
COMMENT ON COLUMN sui_meta.report_card.database_id IS 'Ссылка на подключенную БД';
COMMENT ON COLUMN sui_meta.report_card.table_id IS 'Ссылка на таблицу, на основании которой формируется запрос';
COMMENT ON COLUMN sui_meta.report_card.archived IS 'В архиве';