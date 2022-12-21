// @ts-ignore
/* eslint-disable */
import { request } from '@umijs/max';

/** Получение колонки по идентификатору GET /api/columns/${param0} */
export async function getColumnById(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getColumnByIdParams,
  options?: { [key: string]: any },
) {
  const { id: param0, ...queryParams } = params;
  return request<API.ColumnDto>(`/api/columns/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** Получение колонок по идентификатору таблицы GET /api/columns/tableId=${param0} */
export async function getColumnsByTableId(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getColumnsByTableIdParams,
  options?: { [key: string]: any },
) {
  const { tableId: param0, ...queryParams } = params;
  return request<API.ColumnDto[]>(`/api/columns/tableId=${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  });
}
