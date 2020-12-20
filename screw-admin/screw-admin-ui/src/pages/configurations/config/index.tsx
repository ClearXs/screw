import { connect, Dispatch } from 'umi';
import React, { useState, useEffect, useRef } from 'react';
import { PageHeaderWrapper } from '@ant-design/pro-layout';
import ProTable, { ProColumns, ActionType } from '@ant-design/pro-table';
import Field, { ProFieldFCMode } from '@ant-design/pro-field';
import { ModalForm } from '@ant-design/pro-form';
import ProCard, { ProCardTabsProps } from '@ant-design/pro-card';
import { Spin, Button, Descriptions, Modal, Menu, message, Result } from 'antd';
import { ExclamationCircleOutlined } from '@ant-design/icons';
import ConfigForm from '@/pages/configurations/config/components/ConfigFrom';
import ConfigVersion from '@/pages/configurations/config/version/index';
import { AppServer } from '../server/data';
import { AppConfigState, AppConfigModelType } from '@/pages/configurations/config/models/config';
import { AppConfig, AppConfigQueryParams } from '@/pages/configurations/config/data'
import ConfigData from '@/pages/configurations/config/datas/index'
import _ from 'lodash';
import style from './less/index.less';

interface ConfigProps {
    dispatch: Dispatch;
    appConfigModel: AppConfigModelType;
}

const { confirm } = Modal;

const Config: React.FC<ConfigProps> = ( props ) => {

    const [pageNum, setPageNum] = useState<number>(1);

    const [pageSize, setPageSize] = useState<number>(15);

    const [pageTotal, setPageTotal] = useState<number>(0);

    const [createTime, setCreateTime] = useState<Date | string[]>();

    const [updateTime, setUpdateTime] = useState<Date | string[]>();

    const [isLoading, setIsLoading] = useState<boolean>(true);

    const [appConfigs, setAppConfigs] = useState<AppConfig[]>([]);

    const [serverDirectory, setServerDirectory] = useState<Map<string, AppServer[]>>();

    const [isQuery, setIsQuery] = useState<boolean>(false);

    const [isQueryMenu, setIsQueryMenu] = useState<boolean>(true);

    const [selectedKeys, setSelectKeys] = useState<string[]>();

    const [configName, setConfigName] = useState<string>();

    const [isInitVersion, setIsInitVersion] = useState<boolean>(false);

    const [isConfigFormVisible, setIsConfigFormVisible] = useState<boolean>(false);

    const [state] = useState<ProFieldFCMode>('read');
    
    const [plain] = useState<boolean>(false);

    const [tab, setTab] = useState('tab1');

    const [tabPosition] = useState<ProCardTabsProps['tabPosition']>('top');

    const [isInitData, setIsInitData] = useState<boolean>(false);

    const configDataRef = useRef<any>();

    const actionRef = useRef<ActionType>();

    const columns: ProColumns<AppConfig>[] = [
        {
            title: '序号',
            dataIndex: 'index',
            valueType: 'indexBorder',
        },
        {
            title: '配置名称',
            dataIndex: 'configName',
        },
        {
            title: '配置key',
            dataIndex: 'configKey',
            search: false,
        },
        {
            title: '配置数据',
            dataIndex: 'configJson',
            search: false,
            render: (_, record) => {
                return (
                    <a onClick={() => {
                        Modal.info({
                            title: `[${record.configName}-json数据]`,
                            content: (
                                <Descriptions>
                                    <Descriptions.Item>
                                        <Field
                                            text={record.configJson && record.configJson} 
                                            valueType="jsonCode"
                                            mode={state}
                                            plain={plain}
                                        />
                                    </Descriptions.Item>
                                </Descriptions>
                            ),
                            okText: '返回'
                        })
                    }}>查看</a>
                )
            }
        },
        {
            title: '配置名称',
            dataIndex: 'configName',
            search: false,
        },
        {
            title: '应用服务名称',
            dataIndex: 'appServer',
            search: false,
            render: (element: any) => {
                if (element.hasOwnProperty('serverName')) {
                    return element.serverName;
                } else {
                    return '当前配置不属于任何服务！';
                }
            }
        },
        {
            title: '版本号',
            dataIndex: 'appConfigVersion',
            search: false,
            render: (element: any) => {
                if (element.hasOwnProperty('configVersion')) {
                    return element.configVersion;
                } else {
                    return '当前配置还未发布版本!';
                }
            }
        },
        {
            title: '创建时间',
            dataIndex: 'createTime',
            valueType: 'dateTimeRange',
            render: (element: any) => {
                return (
                    <div>{element.props.text}</div>
                )
            }
        },
        {
            title: '更新时间',
            dataIndex: 'updateTime',
            valueType: 'dateTimeRange',
            render: (element: any) => {
                return (
                    <div>{element.props.text}</div>
                )
            }
        },
        {
            title: '操作',
            key: 'option',
            valueType: 'option',
            render: (text, row, index, action) => [
                <div className={style.version}>
                    <ModalForm
                        title={`${row.configName}版本`}
                        modalProps={{
                            footer: [
                            ]
                        }}
                        trigger={<a key="version" onClick={(event) => {
                            event.stopPropagation()
                            setIsInitVersion(true);
                            const newData = appConfigs.map((value, dataIndex) => {
                                if (dataIndex === index) {
                                    value.isVersionModal = true;
                                }
                                return value;
                            });
                            props.dispatch({
                                type: 'appConfig/changeConfig',
                                payload: newData,
                                callback: (data) => {
                                    setAppConfigs(data);
                                }
                            });
                        }}>版本</a>}
                        width={1420}
                        onVisibleChange={(visible: boolean) => {
                            setIsInitVersion(visible);
                            if (!visible) {
                                setIsLoading(true);
                                setIsQuery(true);
                            }
                            const newData = appConfigs.map((value, dataIndex) => {
                                if (dataIndex === index) {
                                    value.isVersionModal = false;
                                }
                                return value;
                            });
                            props.dispatch({
                                type: 'appConfig/changeConfig',
                                payload: newData,
                                callback: (data) => {
                                    setAppConfigs(data);
                                }
                            });
                        }}
                        visible={row.isVersionModal}
                    >
                        {(isInitVersion && row.isVersionModal) && (
                            <ConfigVersion 
                                appConfig={row} 
                                isInit = {isInitVersion}
                            />
                        )}
                    </ModalForm>
                </div>,
                <ModalForm
                    title="编辑配置文件"
                    width={900}
                    onFinish={async (value: any) => {
                        let isFinish = true;
                        if (configDataRef.current) {
                            isFinish = await configDataRef.current.saveTemp();
                        }
                        if (isFinish) {
                            for (let key in value) {
                                row[key] = value[key];
                            }
                            isFinish = await configFormSubmit(row, 'EDIT');
                        } else {
                            message.error('保存数据出错');
                        }
                        return isFinish;
                    }}
                    trigger={
                        <a onClick={(event) => {
                            event.stopPropagation();
                            const newData = appConfigs.map((value, dataIndex) => {
                                if (dataIndex === index) {
                                    value.isConfigModal = true;
                                }
                                return value;
                            });
                            props.dispatch({
                                type: 'appConfig/changeConfig',
                                payload: newData,
                                callback: (data) => {
                                    setAppConfigs(data);
                                }
                            });
                        }}>
                            编辑
                        </a>
                    }
                    onVisibleChange={(visible: boolean) => {
                        setIsInitVersion(visible);
                        const newData = appConfigs.map((value, dataIndex) => {
                            if (dataIndex === index) {
                                value.isConfigModal = false;
                            }
                            return value;
                        });
                        props.dispatch({
                            type: 'appConfig/changeConfig',
                            payload: newData,
                            callback: (data) => {
                                setAppConfigs(data);
                            }
                        });
                    }}
                    visible={row.isConfigModal}
                >
                    {
                        row.isConfigModal 
                        && 
                        <div>
                            <ProCard
                                tabs={{
                                    tabPosition,
                                    activeKey: tab,
                                    onChange: (key) => {
                                        setTab(key);
                                        if (key === 'tab2') {
                                            setIsInitData(true);
                                        } else {
                                            setIsInitData(false);
                                        }
                                    },
                                }}
                            >
                                <ProCard.TabPane key="tab1" tab="修改配置文件">
                                    <ConfigForm appConfig={row} />
                                </ProCard.TabPane>
                                <ProCard.TabPane key="tab2" tab="修改配置数据">
                                    {renderConfigData(row)}
                                </ProCard.TabPane>
                            </ProCard>
                        </div>
                    }
                </ModalForm>,
                <a key="delete" onClick={() => {
                    confirm({
                        title: '确认删除当前服务?',
                        icon: <ExclamationCircleOutlined />,
                        okText: '确认',
                        okType: 'danger',
                        cancelText: '取消',
                        onOk() {
                            deleteConfig(row);
                        },
                    });
                }}>删除</a>
            ],
        }
    ]

    
    const handlerQuery = () => {
        setIsLoading(true);
        const queryParams: AppConfigQueryParams = {
            pageNum: pageNum ? pageNum : 1,
            pageSize: pageSize ? pageSize : 15,
            createTime: createTime ? createTime : '',
            updateTime: updateTime ? updateTime : '',
            configName: configName ? configName : '',
            serverId: selectedKeys ? selectedKeys[0] : '',
        }
        props.dispatch({
            type: 'appConfig/queryConfig',
            payload: queryParams,
            callback: (result) => {
                const { appConfigs: { list, total } } = result;
                const newList = list.map((value) => {
                    value.isVersionModal = false;
                    value.isConfigModal = false;
                    return value;
                })
                setAppConfigs(newList);
                setPageTotal(total);
                setIsLoading(false);
            }
        })
    }

    const renderConfigData = (row: AppConfig): React.ReactNode => {
        if (row.appConfigVersion) {
            return (
                <ConfigData 
                    appVersion={row.appConfigVersion}
                    isInit={isInitData} 
                    configDataRef={configDataRef}
                    hiddenButtons={true}
                />
            )
        } else {
            return (
                <Result
                    status="warning"
                    title="当前配置没有判断数据，请创建版本"
                />
            )
        }
    }

    const handleQueryServerDirectory = () => {
        props.dispatch({
            type: 'appConfig/queryServerDirectory',
            payload: null,
            callback: (result) => {
                const { success, data } = result;
                if (success) {
                    setServerDirectory(data);
                    const keys = new Array<string>();
                    // 从其他页面调过来
                    const state = props['location'].state;
                    if (!_.isEmpty(state)) {
                        if (state.hasOwnProperty('serverId')) {
                            keys.push(state['serverId']);
                        }
                    } else if (!_.isEmpty(data)) {
                        for (let key in data) {
                            const id = data[key][0].id
                            keys.push(id)
                            break;
                        }
                    }
                    setSelectKeys(keys);
                    setIsQuery(true);
                }
            }
        })
    }

    const deleteConfig = (record: AppConfig) => {
        setIsLoading(true);
        props.dispatch({
            type: 'appConfig/deleteConfig',
            payload: {configId: record.id},
            callback: (result) => {
                const { success, msg } = result;
                if (success) {
                    message.success(msg);
                    setIsQuery(true);
                } else {
                    message.error(msg);
                    setIsLoading(false);
                }
            }
        })
    }

    const configFormSubmit = (params: any, operate: string): Promise<boolean> => {
        return new Promise((resolve, reject) => {
            if (_.isEmpty(selectedKeys)) {
                message.warn('关联服务为空，无法保存');
                resolve(false);
            } else {
                setIsLoading(true);
                if (operate === 'ADD') {
                    props.dispatch({
                        type: 'appConfig/addConfig',
                        payload: Object.assign(params, {serverId: selectedKeys[0]}),
                        callback: (result) => {
                            const { success, msg } = result;
                            if (success) {
                                message.success(msg);
                                setIsQuery(true);
                                resolve(true);
                            } else {
                                message.error(msg);
                                resolve(false);
                                setIsLoading(false);
                            }
                        }
                    })
                } else if (operate === 'EDIT') {
                    props.dispatch({
                        type: 'appConfig/updateConfig',
                        payload: params,
                        callback: (result) => {
                            const { success, msg } = result;
                            if (success) {
                                message.success(msg);
                                handlerQuery();
                                resolve(true);
                            } else {
                                message.error(msg);
                                resolve(false);
                                setIsLoading(false);
                            }
                        }
                    })
                } else {
                    message.warn('未知的操作')
                    setIsLoading(false);
                    resolve(false);
                }
            }
        })
    }

    const renderMenu = (): React.ReactNode => {
        if (_.isEmpty(serverDirectory)) {
            return null;
        }
        const menus = new Array<React.ReactNode>();
        for (let key in serverDirectory) {
            const menu: React.ReactNode = (
                <Menu.ItemGroup key={key} title={key}>
                    {
                        serverDirectory[key].map((appServer: AppServer) => (
                            <Menu.Item key={appServer.id}>{appServer.serverName}</Menu.Item>
                        ))
                    }
                </Menu.ItemGroup>
            )
            menus.push(menu);
        }
        return menus;
    }

    useEffect(() => {
        if (isLoading) {
            if (isQueryMenu) {
                setIsQueryMenu(false);
                handleQueryServerDirectory();
            }
            if (isQuery) {
                setIsQuery(false);
                handlerQuery();
            }
        }
    })

    return (
        <Spin spinning={isLoading}>
            <PageHeaderWrapper>
                <ProTable<AppConfig>
                    columns={columns}
                    actionRef={actionRef}
                    search={{
                        labelWidth: 'auto',
                    }}
                    pagination={{
                        current: pageNum,
                        pageSize: pageSize,
                        total: pageTotal,
                        onChange: (pageNum, pageSize) => {
                            setIsLoading(true);
                            setPageNum(pageNum);
                            setPageSize(pageSize);
                            setIsQuery(true);
                        }
                    }}
                    tableRender={(_, dom) => (
                        <div style={{display: 'flex', width: '100%'}}>
                            <Menu
                                selectedKeys={selectedKeys}
                                style={{ width: 256 }}
                                mode='inline'
                                onSelect={(item) => {
                                    setIsLoading(true);
                                    setIsQuery(true);
                                    setSelectKeys(item.selectedKeys);
                                }}
                            >
                                {
                                    renderMenu()
                                }
                            </Menu>
                            <div style={{flex: 1}}>
                                {dom}
                            </div>
                        </div>
                    )}
                    headerTitle={`配置文件`}
                    dataSource={appConfigs}
                    onSubmit={(params) => {
                        if (params.hasOwnProperty('configName')) {
                            setConfigName(params.configName);
                        }
                        if (params.hasOwnProperty('createTime')) {
                            setCreateTime(params.createTime);
                        }
                        if (params.hasOwnProperty('updateTime')) {
                            setUpdateTime(params.updateTime);
                        }
                        setIsLoading(true);
                        setIsQuery(true);
                    }}
                    onReset={() => {
                        setIsLoading(true);
                        setPageNum(1)
                        setPageSize(15);
                        setConfigName('');
                        setCreateTime([]);
                        setUpdateTime([]);
                        setIsQuery(true);
                    }}
                    toolBarRender={() => [
                        <ModalForm
                            title="新增配置文件"
                            onFinish={async (value: any) => {
                                const isFinish = await configFormSubmit(value, 'ADD');
                                return isFinish;
                            }}
                            trigger={
                                <Button type="primary" key="primary" onClick={(event) => {
                                    event.stopPropagation();
                                    setIsConfigFormVisible(true);
                                }}>
                                    创建配置文件
                                </Button>
                            }
                            onVisibleChange={(visible: boolean) => {
                                if (!visible) {
                                    setIsConfigFormVisible(false);
                                }
                            }}
                            visible={isConfigFormVisible}
                        >
                            {isConfigFormVisible && <ConfigForm />}
                        </ModalForm>
                    ]}                
                />
            </PageHeaderWrapper>
        </Spin>
    )
};

export default connect(({ isLoading, appConfigs, queryParams }: AppConfigState) => ({
    isLoading,
    appConfigs,
    queryParams
}))(Config);