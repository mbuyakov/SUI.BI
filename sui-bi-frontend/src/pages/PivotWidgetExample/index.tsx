import React, {useCallback, useEffect, useState} from "react";
import ProCard from "@ant-design/pro-card";
import {IPivotWidgetExampleSettings} from "@/pages/PivotWidgetExample/types";
import {Button, Empty, Spin} from "antd";
import {SettingOutlined} from "@ant-design/icons";
import {SettingsDrawer} from "@/pages/PivotWidgetExample/SettingsDrawer";
import {executeQuery} from "@/services/api/QueryApi";
import PivotGrid from "devextreme-react/pivot-grid";
import PivotGridDataSource, {PivotGridArea} from "devextreme/ui/pivot_grid/data_source";
import {getColumnById} from "@/services/api/ColumnApi";

import {StructuredQuery} from "../../../types/structuredQuery";

import "devextreme/dist/css/dx.light.css";
import {SummaryType} from "devextreme/common/grids";

export default function PivotWidgetExample(): JSX.Element {
  const [settings, setSettings] = useState<IPivotWidgetExampleSettings>();
  const [settingsDrawerOpen, setSettingsDrawerOpen] = useState<boolean>(false);
  const [loading, setLoading] = useState<boolean>(false);
  const [dataSource, setDataSource] = useState<PivotGridDataSource>();

  const openSettingsDrawer = useCallback(() => setSettingsDrawerOpen(true), [setSettingsDrawerOpen]);
  const closeSettingsDrawer = useCallback(() => setSettingsDrawerOpen(false), [setSettingsDrawerOpen]);
  const onSettingsDrawerSubmit = useCallback((values: IPivotWidgetExampleSettings) => {
    setSettings(values);
    setSettingsDrawerOpen(false);
  }, []);

  useEffect(() => {
    if (!settings) {
      setDataSource(undefined);
      return;
    }

    setLoading(true);

    createDataSource(settings)
      .then(setDataSource)
      .catch((reason) => console.error(`Error in createDataSource`, settings, reason))
      .finally(() => setLoading(false));
  }, [settings]);

  return (
    <>
      <ProCard
        title="Сводная таблица"
        extra={(
          <Button
            type="text"
            icon={(<SettingOutlined/>)}
            onClick={openSettingsDrawer}
          />
        )}
      >
        <Spin spinning={loading}>
          <PivotGridRenderer dataSource={dataSource}/>
        </Spin>
      </ProCard>
      <SettingsDrawer
        open={settingsDrawerOpen}
        onClose={closeSettingsDrawer}
        onSubmit={onSettingsDrawerSubmit}
      />
    </>
  );
}

function PivotGridRenderer({dataSource}: { dataSource?: PivotGridDataSource }): JSX.Element {
  if (!dataSource) {
    return (<Empty description="Источник данных отсутствует"/>)
  }

  return (
    // @ts-ignore
    <PivotGrid
      dataSource={dataSource}
      allowSortingBySummary={true}
      allowSorting={true}
      allowFiltering={true}
      allowExpandAll={true}
      height={500}
      showBorders={true}
      fieldChooser={{enabled: false}}
    />
  );
}

async function createDataSource(settings: IPivotWidgetExampleSettings): Promise<PivotGridDataSource> {
  const {table, values} = settings;
  const rows = await Promise.all(settings.rows.map(it => getColumnById({id: Number(it)})));
  const columns = await Promise.all(settings.columns.map(it => getColumnById({id: Number(it)})));

  const structuredQuery: StructuredQuery = {
    database: 1,
    query: {
      "source-table": table,
      "group-by": [...new Set([...rows, ...columns].map(it => it.id))].map(it => ({field: it})),
      "aggregation": values.map((it, index) => {
        let aggFunction: string;
        let field: string | null;

        switch (it.type) {
          case "COUNT":
            aggFunction = "count";
            field = null;
            break;
          case "COUNT_DISTINCT":
            aggFunction = "count-distinct";
            field = null;
            break;
          case "COUNT_DISTINCT_BY_FIELD":
            aggFunction = "count-distinct";
            field = it.field;
            break;
          case "SUM_BY_FIELD":
            aggFunction = "sum";
            field = it.field;
            break;
          case "MIN_BY_FIELD":
            aggFunction = "min";
            field = it.field;
            break;
          case "MAX_BY_FIELD":
            aggFunction = "max";
            field = it.field;
            break;
          case "AVERAGE_BY_FIELD":
            aggFunction = "average";
            field = it.field;
            break;
        }

        return {
          "agg-function": aggFunction,
          field: field,
          "field-alias": `__value_${index}`
        };
      })
    },
    type: "query"
  };

  const queryResult = await executeQuery(JSON.stringify(structuredQuery));

  const data = queryResult.data.map(e => Object.fromEntries(queryResult.columns.map((it, i) => [it, e[i]])));

  return new PivotGridDataSource({
    fields: [
      ...rows.map(it => ({dataField: it.columnName, area: "row" as PivotGridArea})),
      ...columns.map(it => ({dataField: it.columnName, area: "column" as PivotGridArea})),
      ...values.map((it, index) => {
        let summaryType: SummaryType;

        switch (it.type) {
          case "MIN_BY_FIELD":
            summaryType = "min";
            break;
          case "MAX_BY_FIELD":
            summaryType = "max";
            break;
          case "AVERAGE_BY_FIELD":
            summaryType = "avg";
            break;
          default:
            summaryType = "sum";
        }

        return {
          dataField: `__value_${index}`,
          area: "data" as PivotGridArea,
          caption: it.label,
          summaryType
        };
      })
    ],
    store: data
  });
}
