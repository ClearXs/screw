import { Reducer, Effect } from 'umi';
import { queryConfigVersions, openedVersion } from '@/pages/configurations/config/version/services/version'
import { message } from 'antd';
import { AppConfigVersion, AppConfigVersionQueryParams } from '@/pages/configurations/config/data';
import { ConnectState } from '@/models/connect';
import { deleteAttr } from '@/utils/utils'

export interface AppConfigVersionState {
    isLoading: boolean;
    appConfigVersions: AppConfigVersion[];
    queryParams: AppConfigVersionQueryParams;
    defaultPageSize: number;
};

export interface AppConfigVersionModelType {
    namespace: 'appConfigVersion';
    state: AppConfigVersionState;
    effects: {
        queryConfigVersions: Effect;
        openedVersion: Effect;
        changeConfigVersions: Effect;
    };
    reducers: {
        setState: Reducer<AppConfigVersionState>;
    };
}

const defaultAppConfigVersionState: AppConfigVersionState = {
    isLoading: true,
    appConfigVersions: [],
    defaultPageSize: 15,
    queryParams: {
        pageNum: 1,
        pageSize: 15,
        configId: ''
    },
}

const AppConfigVersionModel: AppConfigVersionModelType = {
    namespace: 'appConfigVersion',
    state: defaultAppConfigVersionState,
    effects: {
        *queryConfigVersions({ payload, callback }, { call, put, select }) {
            const result = yield call(queryConfigVersions, payload);
            const { success, msg, data } = result;
            if (!success) {
                message.error(`${msg}`);
            }
            yield put({
                type: 'setState',
                payload: {
                    isLoading: false,
                    appConfigVersions: data
                },
            });
            yield select((state: ConnectState) => {
                callback && callback(state.appConfigVersion);
            });
        },
        *openedVersion({ payload, callback }, { call, put }) {
            payload = deleteAttr(payload, 'createBy');
            payload = deleteAttr(payload, 'createTime');
            payload = deleteAttr(payload, 'updateBy');
            payload = deleteAttr(payload, 'updateTime');
            payload = deleteAttr(payload, 'deleted');
            payload = deleteAttr(payload, 'isModal');
            payload = deleteAttr(payload, 'appConfigData');
            const result = yield call(openedVersion, payload);
            callback && callback(result);
        },
        *changeConfigVersions({ payload, callback}, { put }) {
            yield put({
                type: 'setState',
                appConfigVersions: payload
            });
            callback && callback(payload);
        },
    },
    reducers: {
        setState(state, { payload }): AppConfigVersionState {
            return {
                ...payload
            };
        },
    },
}


export default AppConfigVersionModel;