import request from '@/utils/request';
import { AppConfigVersion, AppConfigVersionQueryParams } from '@/pages/configurations/config/data';
import { json2RequestParams } from '@/utils/utils';

const baseUrl: string = '/api/appConfig/version';

/**
 * 查询所有配置版本
 * @param params
 */
export async function queryConfigVersions(params: AppConfigVersionQueryParams) {
    return request(`${baseUrl}/queryConfigVersions?${json2RequestParams(params)}`, {
        method: 'GET',
        data: {},
    });
}


/**
 * 修改配置版本
 * @param params
 */
export async function openedVersion(params: AppConfigVersion) {
    return request(`${baseUrl}/openedVersion`, {
        method: 'POST',
        data: params,
    });
}