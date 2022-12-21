// @ts-ignore
/* eslint-disable */
import { request } from '@umijs/max';

/** Выполнение запроса POST /api/queries/execute */
export async function executeQuery(body: string, options?: { [key: string]: any }) {
  return request<API.QueryResultDto>('/api/queries/execute', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}
