import { Reducer, Effect } from 'umi';
import { getMonitorList, getActiveServerMetrics, getServerTracing } from '@/pages/monitor/services/monitor';
import { ServerMonitorModel, TracingModel, ServerMonitorQueryParams } from '@/pages/monitor/data';
import { ConnectState } from '@/models/connect';
import { message } from 'antd';

export interface MonitorState {
    isLoading: boolean;
    serverModels: ServerMonitorModel[];
    queryParams: ServerMonitorQueryParams;
    tracingModel: TracingModel;
    serverMetrics: ServerMonitorModel;
}

export interface MonitorModelType {
    namespace: 'monitor';
    state: MonitorState;
    effects: {
        getMonitorList: Effect;
        getActiveServerMetrics: Effect;
        getServerTracing: Effect;
    };
    reducers: {
        setState: Reducer<MonitorState>;
    };
}

const defaultMonitorState: MonitorState = {
    isLoading: true,
    serverModels: [],
    queryParams: {},
    tracingModel: {},
    serverMetrics: {}
}

const MonitorModel: MonitorModelType = {
    namespace: 'monitor',
    state: defaultMonitorState,
    effects: {
        *getMonitorList({ payload, callback }, { call, put, select }) {
            const result = yield call(getMonitorList, payload);
            const { success, msg, data } = result;
            if (!success) {
                message.error(`${msg}`);
            } else {
                yield put({
                    type: 'setState',
                    payload: {
                        serverModels: data
                    },
                });
                yield select((state: ConnectState) => {
                    callback && callback(state.monitor.serverModels);
                });
            }
        },
        *getActiveServerMetrics({ payload, callback }, { call, put, select }) {
            const result = yield call(getActiveServerMetrics, payload);
            const { success, msg, data } = result;
            if (!success) {
                message.error(`${msg}`);
            } else {
                yield put({
                    type: 'setState',
                    payload: {
                        serverMetrics: data
                    },
                });
                yield select((state: ConnectState) => {
                    callback && callback(state.monitor.serverMetrics);
                });
            }

        },
        *getServerTracing({ payload, callback }, { call, put, select }) {
            const result = yield call(getServerTracing, payload);
            const { success, msg, data } = result;
            if (!success) {
                message.error(`${msg}`);
            } else {
                yield put({
                    type: 'setState',
                    payload: {
                        tracingModel: data
                    },
                });
                yield select((state: ConnectState) => {
                    callback && callback(state.monitor.tracingModel);
                });
            }
        },
    },
    reducers: {
        setState(state, { payload }): MonitorState {
            return {
                ...payload
            };
        },
    }
}

export default MonitorModel;
