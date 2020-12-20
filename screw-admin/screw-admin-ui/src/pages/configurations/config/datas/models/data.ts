import { Reducer, Effect } from 'umi';
import { queryConfigData, saveConfigData, deleteConfigData } from '@/pages/configurations/config/datas/services/data'
import { message } from 'antd';
// @ts-ignore
import { AppConfigData, AppConfigDataQueryParams } from '@/pages/configurations/config/data';
import { ConnectState } from '@/models/connect';
import { deleteAttr } from '@/utils/utils';

export interface AppConfigDataState {
    isLoading: boolean;
    appConfigData: AppConfigData[];
    queryParams: AppConfigDataQueryParams;
    defaultPageSize: number;
};

export interface AppConfigDataModelType {
    namespace: 'appConfigData';
    state: AppConfigDataState;
    effects: {
        queryConfigData: Effect;
        saveConfigData: Effect;
        deleteConfigData: Effect;
        changeConfigData: Effect;
    };
    reducers: {
        setState: Reducer<AppConfigDataState>;
    };
}

const defaultAppConfigDataState: AppConfigDataState = {
    isLoading: true,
    appConfigData: [],
    defaultPageSize: 15,
    queryParams: {
        pageNum: 1,
        pageSize: 15,
        configVersionId: ''
    },
}

const AppConfigDataModel: AppConfigDataModelType = {
    namespace: 'appConfigData',
    state: defaultAppConfigDataState,
    effects: {
        *queryConfigData({ payload, callback }, { call, put, select }) {
            const result = yield call(queryConfigData, payload);
            const { success, msg, data } = result;
            if (!success) {
                message.error(`${msg}`);
            }
            yield put({
                type: 'setState',
                payload: {
                    isLoading: false,
                    appConfigData: data
                },
            });
            yield select((state: ConnectState) => {
                callback && callback(state.appConfigData);
            });
        },
        *saveConfigData({ payload, callback}, { put, call}) {
            const { appData, logicOperate } = payload;
            let submitData = appData.map((value: any) => {
                value = deleteAttr(value, 'isDraw');
                value = deleteAttr(value, 'createBy');
                let configVersion = value.configVersion;
                if (configVersion) {
                    configVersion = deleteAttr(configVersion, 'isModal');
                    value.configVersion = configVersion;
                }
                value = deleteAttr(value, 'createTime');
                value = deleteAttr(value, 'updateBy');
                value = deleteAttr(value, 'updateTime');
                value = deleteAttr(value, 'isDraw');
                value = deleteAttr(value, 'deleted');
                return value;
            });
            const result = yield call(saveConfigData, {submitData, logicOperate});
            callback && callback(result);
        },
        *deleteConfigData({ payload, callback }, { call }) {
            const configData = new Array<AppConfigData>();
            // 删除appData.isDraw属性
            payload = deleteAttr(payload, 'createBy');
            payload = deleteAttr(payload, 'createTime');
            payload = deleteAttr(payload, 'updateBy');
            payload = deleteAttr(payload, 'updateTime');
            payload = deleteAttr(payload, 'isDraw');
            payload = deleteAttr(payload, 'configVersion');
            configData.push(payload);
            const result = yield call(deleteConfigData, configData);
            callback && callback(result);
        },
        *changeConfigData({ payload, callback }, { put }) {
            yield put({
                type: 'setState',
                payload: {
                    appConfigData: payload
                },
            });
            callback && callback(payload);
        }
    },
    reducers: {
        setState(state, { payload }): AppConfigDataState {
            return {
                ...payload
            };
        },
    },
}


export default AppConfigDataModel;
