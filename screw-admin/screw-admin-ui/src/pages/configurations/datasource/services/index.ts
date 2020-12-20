import request from '@/utils/request';
import { DataSource } from '@/pages/configurations/datasource/data';
import Qs from 'qs';

const baseUrl: string = '/api/datasource';

/**
 * 查询数据源
 * @param params
 */
export async function queryDatabase() {
    return request(`${baseUrl}/queryDatasource`, {
        method: 'GET',
        data: {},
    });
}

/**
 * 添加数据源
 * @param params
 */
export async function addDatabase(params: DataSource) {
    return request(`${baseUrl}/addDatasource`, {
        method: 'POST',
        data: params,
    });
}

/**
 * 修改数据源
 * @param params
 */
export async function editDatabase(params: DataSource) {
    return request(`${baseUrl}/editDatasource`, {
        method: 'PUT',
        data: params,
    });
}

/**
 * 删除数据源
 * @param params
 */
export async function deleteDatabase(params: DataSource) {
    return request(`${baseUrl}/deleteDatasource`, {
        method: 'DELETE',
        headers:{
            'Content-type': 'application/x-www-form-urlencoded; charset=UTF-8'
        },
        data: Qs.stringify({id: params.id})
    });
}

/**
 * 测试数据源
 * @param params
 */
export async function testConnect(params: DataSource) {
    return request(`${baseUrl}/testConnect`, {
        method: 'POST',
        headers:{
            'Content-type': 'application/x-www-form-urlencoded; charset=UTF-8'
        },
        data: Qs.stringify({dataSourceId: params.id})
    });
}