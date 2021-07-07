import { Reducer, Effect } from 'umi';
import { queryDatabase, addDatabase, editDatabase, deleteDatabase, testConnect, testConnectByEntity } from '@/pages/configurations/datasource/services/index'
import { transfer } from '@/pages/configurations/config/datas/services/data';
import { message } from 'antd';
import { DataSource } from '@/pages/configurations/datasource/data';
import { ConnectState } from '@/models/connect';
import _ from 'lodash';

export interface DataSourceState {
    isLoading: boolean;
    dataSources: DataSource[];
};

export interface DataSourceModelType {
    namespace: 'dataSource';
    state: DataSourceState;
    effects: {
        queryDatabase: Effect;
        addDatabase: Effect;
        editDatabase: Effect;
        deleteDatabase: Effect;
        testConnect: Effect;
        testConnectByEntity: Effect;
        transfer: Effect;
    };
    reducers: {
        setState: Reducer<DataSourceState>;
    };
}

const defaultAppServerState: DataSourceState = {
    isLoading: true,
    dataSources: []
}

const DataSourceModel: DataSourceModelType = {
    namespace: 'dataSource',
    state: defaultAppServerState,
    effects: {
        *queryDatabase({ payload, callback }, { call, put, select }) {
            const result = yield call(queryDatabase, payload);
            const { success, msg, data } = result;
            if (!success) {
                message.error(`${msg}`);
            }
            yield put({
                type: 'setState',
                payload: {
                    isLoading: false,
                    dataSources: data
                },
            });
            yield select((state: ConnectState) => {
                callback && callback(state.dataSource);
            });
        },
        *addDatabase({ payload, callback }, { call }) {
            const result = yield call(addDatabase, payload);
            yield callback && callback(result);
        },
        *editDatabase({ payload, callback }, { call }) {
            const result = yield call(editDatabase, payload);
            yield callback && callback(result);
        },
        *deleteDatabase({ payload, callback }, { call }) {
            const result = yield call(deleteDatabase, payload);
            yield callback && callback(result);
        },
        *testConnect({ payload, callback }, { call }) {
            const result = yield call(testConnect, payload);
            yield callback && callback(result);
        },
        *testConnectByEntity({ payload, callback }, { call }) {
            const result = yield call(testConnectByEntity, payload);
            yield callback && callback(result);
        },
        *transfer({ payload, callback }, { call} ) {
            const result = yield call(transfer, encodeURIComponent(JSON.stringify(payload)));
            callback && callback(result);
        }
    },
    reducers: {
        setState(state, { payload }): DataSourceState {
            return {
                ...payload
            };
        },
    },
}


export default DataSourceModel;