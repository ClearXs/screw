import request from '@/utils/request';
import { json2RequestParams } from '@/utils/utils';
import { ServerMonitorQueryParams } from '@/pages/monitor/data';

const baseUrl: string = '/api/webapi/monitor';

/**
 * 获取监控中心列表
 * @param params
 */
export async function getMonitorList(params: ServerMonitorQueryParams) {
    return request(`${baseUrl}/getMonitorList?${json2RequestParams(params)}`, {
        method: 'GET',
    });
}

/**
 * 获取激活服务性能指标
 * @param params
 */
export async function getActiveServerMetrics(params: ServerMonitorQueryParams) {
    return request(`${baseUrl}/getActiveServerMetrics?${json2RequestParams(params)}`, {
        method: 'GET',
    });
}


/**
 * 获取服务链路追踪
 * @param params
 */
export async function getServerTracing(params: ServerMonitorQueryParams) {
    return request(`${baseUrl}/getServerTracing?${json2RequestParams(params)}`, {
        method: 'GET',
    });
}