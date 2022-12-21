type IPivotWidgetExampleValueType = { type: "COUNT" }
  | { type: "COUNT_DISTINCT" }
  | { type: "COUNT_DISTINCT_BY_FIELD", field: string; }
  | { type: "SUM_BY_FIELD", field: string; }
  | { type: "MIN_BY_FIELD", field: string; }
  | { type: "MAX_BY_FIELD", field: string; }
  | { type: "AVERAGE_BY_FIELD", field: string; };

export type IPivotWidgetExampleValue = IPivotWidgetExampleValueType & { label: string };

export type IPivotWidgetExampleSettings = {
  table: string;
  rows: string[];
  columns: string[];
  values: IPivotWidgetExampleValue[];
}
