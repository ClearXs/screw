import { Reducer, Effect } from 'umi';
import { queryConfig, addConfig, updateConfig, deleteConfig, queryServerDirectory } from '@/pages/configurations/config/services/config'
import { message } from 'antd';
import { AppConfig, AppConfigQueryParams } from '@/pages/configurations/config/data';
import { ConnectState } from '@/models/connect';
import { deleteAttr } from '@/utils/utils';

export interface AppConfigState {
    isLoading: boolean;
    appConfigs: AppConfig[];
    queryParams: AppConfigQueryParams;
    defaultPageSize: number;
};

export interface AppConfigModelType {
    namespace: 'appConfig';
    state: AppConfigState;
    effects: {
        queryConfig: Effect;
        addConfig: Effect;
        updateConfig: Effect;
        deleteConfig: Effect;
        queryServerDirectory: Effect;
        changeConfig: Effect;
    };
    reducers: {
        setState: Reducer<AppConfigState>;
    };
}

const defaultAppServerState: AppConfigState = {
    isLoading: true,
    appConfigs: [],
    defaultPageSize: 15,
    queryParams: {
        pageNum: 1,
        pageSize: 15,
    },
}

const AppConfigModel: AppConfigModelType = {
    namespace: 'appConfig',
    state: defaultAppServerState,
    effects: {
        *queryConfig({ payload, callback }, { call, put, select }) {
            const result = yield call(queryConfig, payload);
            const { success, msg, data } = result;
            if (!success) {
                message.error(`${msg}`);
            }
            yield put({
                type: 'setState',
                payload: {
                    isLoading: false,
                    appConfigs: data
                },
            });
            yield select((state: ConnectState) => {
                callback && callback(state.appConfig);
            });
        },
        *addConfig({ payload, callback }, { call }) {
            const result = yield call(addConfig, payload);
            yield callback && callback(result);

        },
        *updateConfig({ payload, callback }, { call }) {
            payload = deleteAttr(payload, 'isConfigModal');
            payload = deleteAttr(payload, 'createBy');
            payload = deleteAttr(payload, 'createTime');
            payload = deleteAttr(payload, 'updateBy');
            payload = deleteAttr(payload, 'updateTime');
            payload = deleteAttr(payload, 'deleted');
            payload = deleteAttr(payload, 'roleId');
            payload = deleteAttr(payload, 'appConfigVersion');
            payload = deleteAttr(payload, 'appServer');
            payload = deleteAttr(payload, 'isVersionModal');
            
            const result = yield call(updateConfig, payload);
            yield callback && callback(result);
        },
        *deleteConfig({ payload, callback }, { call }) {
            const result = yield call(deleteConfig, payload);
            yield callback && callback(result);
        },
        *queryServerDirectory({ payload, callback }, { call }) {
            const result = yield call(queryServerDirectory);
            callback && callback(result);
        },
        *changeConfig({ payload, callback}, { put }) {
            yield put({
                type: 'setState',
                appConfigs: payload
            });
            callback && callback(payload);
        }
    },
    reducers: {
        setState(state, { payload }): AppConfigState {
            return {
                ...payload
            };
        },
    },
}


export default AppConfigModel;