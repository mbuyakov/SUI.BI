// @ts-ignore
/* eslint-disable */
import { request } from '@umijs/max';

/** Получение таблицы по идентификатору GET /api/tables/${param0} */
export async function getTableById(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getTableByIdParams,
  options?: { [key: string]: any },
) {
  const { id: param0, ...queryParams } = params;
  return request<API.TableDto>(`/api/tables/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** Получение таблиц по идентификатору БД GET /api/tables/databaseId=${param0} */
export async function getTablesByDatabaseId(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getTablesByDatabaseIdParams,
  options?: { [key: string]: any },
) {
  const { databaseId: param0, ...queryParams } = params;
  return request<API.TableDto[]>(`/api/tables/databaseId=${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  });
}
