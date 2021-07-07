import request from '@/utils/request';
// @ts-ignore
import { AppConfigData, AppConfigDataQueryParams } from '@/pages/configurations/config/data';
import { json2RequestParams } from '@/utils/utils';

const baseUrl: string = '/api/appConfig/data';

/**
 * 查询所有配置数据
 * @param params
 */
export async function queryConfigData(queryParams: AppConfigDataQueryParams) {
    return request(`${baseUrl}/queryConfigData?${json2RequestParams(queryParams)}`, {
        method: 'GET',
    });
}

/**
 * 保存配置文件
 * @param params
 */
export async function saveConfigData({submitData, logicOperate}: AppConfigData) {
    return request(`${baseUrl}/saveConfigData?logicOperate=${logicOperate}`, {
        method: 'POST',
        data: submitData,
    });
}

/**
 * 删除数据
 * @param params
 */
export async function deleteConfigData(params: AppConfigData[]) {
    return request(`${baseUrl}/deleteConfigData`, {
        method: 'DELETE',
        data: params,
    });
}

/**
 * 解析器
 * @param params 
 * @returns
 */
export async function transfer(params) {
    return request(`/api/parser/transfer?format=${params}`, {
        method: 'GET'
    });
}