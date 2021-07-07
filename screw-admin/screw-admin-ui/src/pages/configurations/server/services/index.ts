import request from '@/utils/request';
import { AppServer } from '@/pages/configurations/server/data';
import Qs from 'qs';

const baseUrl = '/api/appServer';

const dataSourceUrl = '/api/datasource'

/**
 * 查询应用服务
 * @param params
 */
export async function queryAppServer(params: AppServer) {
    return request(`${baseUrl}/queryAppServer`, {
        method: 'GET',
        data: params,
    });
}

/**
 * 修改应用服务
 * @param params
 */
export async function editAppServer(params: AppServer) {
    return request(`${baseUrl}/editAppServer`, {
        method: 'PUT',
        data: params,
    });
}

/**
 * 新增应用服务
 * @param params
 */
export async function addAppServer(params: AppServer) {
    return request(`${baseUrl}/addAppServer`, {
        method: 'POST',
        data: params,
    });
}

/**
 * 删除应用服务
 * @param params
 */
export async function deleteAppServer(params: AppServer[]) {
    return request(`${baseUrl}/deleteAppServer`, {
        method: 'DELETE',
        headers:{
            'Content-type': 'application/x-www-form-urlencoded; charset=UTF-8'
        },
        data: Qs.stringify({serverIds: params.map((value: AppServer) => {
            return value.id;
        }).join()})       
    });
}

/**
 * 加载数据源
 * @param params
 */
export async function loadDataSource() {
    return request(`${dataSourceUrl}/queryDatasource`, {
        method: 'GET',
    });
}

/**
 * 测试数据源
 * @param params
 */
export async function testConnect(params) {
    return request(`${dataSourceUrl}/testConnect`, {
        method: 'POST',
        data: params   
    });
}

/**
 * 查询默认的服务及其配置
 * @param params
 */
export async function queryServerDirectory(operate: string) {
    return request(`${baseUrl}/queryServerDirectory?operate=${operate}`, {
        method: 'GET',
    });
}

/**
 * 查询默认的服务及其配置
 * @param params
 */
export async function isExist(params: AppServer) {
    return request(`${baseUrl}/isExist`, {
        method: 'POST',
        data: params,
    });
}