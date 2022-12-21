
// StructuredQuery

export interface StructuredQuery {
  database: string | number;
  query: Query;
  type: "query" | "native";
}

// Query

export interface Query {
  "source-table": string | number;
  fields?: Field[] | null;
  joins?: Join[] | null;
  aggregation?: Aggregation[] | null;
  "group-by"?: GroupBy[] | null;
  filter?: FilterTree;
  "order-by"?: OrderBy[];
  limit?: number | null;
  offset?: number | null;
}

// Field

export interface Field {
  field: string | number;
  "join-alias"?: string | null;
}

// Join

export type JoinStrategy = "INNER_JOIN" | "LEFT_JOIN" | "RIGHT_JOIN" | "FULL_JOIN";

export interface JoinOn {
  field: string | number;
  alias?: string | null;
}

export interface Join {
  "source-table": string | number;
  strategy?: JoinStrategy | null;
  "left-on": JoinOn;
  "right-on": JoinOn;
  alias: string;
}

// Aggregation

export interface AsteriskAggregation {
  "agg-function": string;
  "field-alias": string;
}

export interface FieldAggregation {
  "agg-function": string;
  "field-alias": string;
  field: string | number;
  "join-alias"?: string | null;
}

export type Aggregation = AsteriskAggregation | FieldAggregation;

// GroupBy

export interface GroupBy {
  field: string | number;
  "join-alias"?: string | null;
}

// Filter

export type FilterOperation = "in"
  | "notIn"
  | "equal"
  | "notEqual"
  | "empty"
  | "notEmpty"
  | "greaterThan"
  | "greaterThanOrEqual"
  | "lessThan"
  | "lessThanOrEqual"
  | "contains";

export interface Filter {
  field: string | number;
  "join-alias"?: string | null;
  operation: FilterOperation;
  value?: string | number | boolean | null | Array<string | number | boolean | null>;
}

export type FilterTreePredicate = "and" | "or";

type FilterTree = { [key in FilterTreePredicate]: Array<FilterTree | Filter>; };

// OrderBy

export type OrderDirection = "asc" | "desc";

export interface FieldOrderBy {
  order?: OrderDirection | null;
  field: string | number;
  "join-alias"?: string | null;
}

export interface AliasOrderBy {
  order?: OrderDirection | null;
  "field-alias": string | number;
}

export type OrderBy = FieldOrderBy | AliasOrderBy;
