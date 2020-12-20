import request from '@/utils/request';
import { AppConfig, AppConfigQueryParams } from '@/pages/configurations/config/data';
import { json2RequestParams } from '@/utils/utils';
import Qs from 'qs';

const baseUrl: string = '/api/appConfig';

const serverUrl: string = '/api/appServer'

/**
 * 查询所有配置
 * @param params
 */
export async function queryConfig(params: AppConfigQueryParams) {
    return request(`${baseUrl}/queryConfig?${json2RequestParams(params)}`, {
        method: 'GET',
    });
}

/**
 * 添加配置
 * @param params
 */
export async function addConfig(params: AppConfig) {
    return request(`${baseUrl}/addConfig`, {
        method: 'POST',
        data: params,
    });
}

/**
 * 修改配置
 * @param params
 */
export async function updateConfig(params: AppConfig) {
    return request(`${baseUrl}/updateConfig`, {
        method: 'PUT',
        data: params,
    });
}

/**
 * 删除数据源
 * @param params
 */
export async function deleteConfig(params: AppConfig) {
    return request(`${baseUrl}/deleteConfig`, {
        method: 'DELETE',
        headers:{
            'Content-type': 'application/x-www-form-urlencoded; charset=UTF-8'
        },
        data: Qs.stringify(params)    
    });
}

/**
 * 查询所有的服务及其配置
 * @param params
 */
export async function queryServerDirectory() {
    return request(`${serverUrl}/queryServerDirectory?operate=ALL`, {
        method: 'GET',
    });
}