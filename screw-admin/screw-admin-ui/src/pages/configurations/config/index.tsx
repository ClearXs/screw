import { connect, Dispatch } from 'umi';
import React, { useState, useEffect, useRef } from 'react';
import { PageHeaderWrapper } from '@ant-design/pro-layout';
import ProTable, { ProColumns, ActionType } from '@ant-design/pro-table';
import Field from '@ant-design/pro-field';
import { ModalForm } from '@ant-design/pro-form';
import ProCard from '@ant-design/pro-card';
import { Spin, Button, Modal, Menu, message, Result, Popover } from 'antd';
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

    const [plain] = useState<boolean>(false);

    const [isInitData, setIsInitData] = useState<boolean>(false);

    const [selectedConfigs, setSelectedConfigs] = useState<AppConfig[]>();

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
        // {
        //     title: '配置数据',
        //     dataIndex: 'configJson',
        //     search: false,
        //     render: (_, record) => {
        //         return (
        //             <Tooltip title={
        //                 <Descriptions>
        //                     <Descriptions.Item>
        //                         <Field
        //                             text={record.configJson && record.configJson}
        //                             valueType="jsonCode"
        //                             plain={plain}
        //                         />
        //                     </Descriptions.Item>
        //                 </Descriptions>
        //             } color="#F6F8FA">
        //                 <a style={{cursor: 'default'}}>查看</a>
        //             </Tooltip>
        //         )
        //     }
        // },
        // {
        //     title: '应用服务名称',
        //     dataIndex: 'appServer',
        //     search: false,
        //     render: (element: any) => {
        //         if (element.hasOwnProperty('serverName')) {
        //             return element.serverName;
        //         } else {
        //             return '当前配置不属于任何服务！';
        //         }
        //     }
        // },
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
            },
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
                <ModalForm
                    className={style.version}
                    title={`${row.configName}版本`}
                    trigger={<a key="version" onClick={(event) => {
                        event.stopPropagation();
                    }}>版本</a>}
                    width={1000}
                    onVisibleChange={(visible: boolean) => {
                        setIsInitVersion(visible);
                        if (!visible) {
                            setIsLoading(true);
                            setIsQuery(true);
                        }
                        const newData = appConfigs.map((value, dataIndex) => {
                            if (dataIndex === index) {
                                value.isVersionModal = visible;
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
                            isInit={isInitVersion}
                        />
                    )}
                </ModalForm>,
                <ModalForm
                    title="编辑配置文件"
                    width={1000}
                    onFinish={async (value: any) => {
                        if (event.type === 'keypress' && event.keyCode === 13) {
                            return false;
                        }
                        let isFinish = true;
                        if (configDataRef.current) {
                            isFinish = await configDataRef.current.checkedSaveStatus();
                            if (!isFinish) {
                                message.warn('点击完毕，进行数据保存');
                                return isFinish;
                            }
                            isFinish = await configDataRef.current.saveTemp();
                        }
                        if (isFinish) {
                            for (let key in value) {
                                row[key] = value[key];
                            }
                            isFinish = await configFormSubmit(row, 'EDIT');
                        }
                        return isFinish;
                    }}
                    modalProps={{
                        keyboard: false,
                    }}
                    trigger={
                        <a onClick={(event) => {
                            event.stopPropagation();
                        }}>
                            编辑
                        </a>
                    }
                    onVisibleChange={(visible: boolean) => {
                        setIsInitVersion(visible);
                        const newData = appConfigs.map((value, dataIndex) => {
                            if (dataIndex === index) {
                                value.isConfigModal = visible;
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
                            <ProCard>
                                <ConfigForm appConfig={row} />
                                {renderConfigData(row)}
                            </ProCard>
                        </div>
                    }
                </ModalForm>,
                <a key="delete" onClick={() => {
                    confirm({
                        title: '确认删除当前配置?',
                        icon: <ExclamationCircleOutlined />,
                        okText: '确认',
                        okType: 'danger',
                        cancelText: '取消',
                        onOk() {
                            const appConfig = new Array();
                            appConfig.push(row);
                            deleteConfig(appConfig);
                        },
                    });
                }}>删除</a>,
                <Popover title={row.configName} color="#F6F8FA" content={(
                    <Field
                        text={row.configJson && row.configJson}
                        valueType="jsonCode"
                        plain={plain}
                    />
                )} style={{width: '50%'}}>
                    <a style={{cursor: 'default'}}>详情</a>
                </Popover>
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
        setIsInitData(true);
        if (row.appConfigVersion && isInitData) {
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
                            if (data[key].length === 0) {
                                continue;
                            }
                            const id = data[key][0].id
                            keys.push(id)
                            break;
                        }
                    }
                    setSelectKeys(keys);
                    setIsQuery(true);
                }
            }
        });
    }

    const deleteConfig = (records: AppConfig[]) => {
        if (_.isEmpty(records)) {
            return;
        }
        setIsLoading(true);
        props.dispatch({
            type: 'appConfig/deleteConfig',
            payload: {configIds: records.map((value: AppConfig) => {
                return value.id;
            }).join()},
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
        });
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
                                    setSelectedConfigs([]);
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
                    rowSelection={{
                        onChange: (selectedKeys, selectedRow) => {
                            setSelectedConfigs(selectedRow);
                        },
                    }}
                    tableAlertRender={false}
                    tableAlertOptionRender={false}
                    rowKey="id"
                    options={false}
                    toolBarRender={() => [
                        <ModalForm
                            title="新增配置文件"
                            width={1000}
                            onFinish={async (value: any) => {
                                const isFinish = await configFormSubmit(value, 'ADD');
                                return isFinish;
                            }}
                            trigger={
                                <div>
                                    <Button type="primary" key="primary" size="middle" style={{marginRight: '5px'}} onClick={(event) => {
                                        event.stopPropagation();
                                        setIsConfigFormVisible(true);
                                    }}>
                                        创建配置文件
                                    </Button>
                                    <Button type="default" size="middle" onClick={(event) => {
                                        event.stopPropagation();
                                        if (_.isEmpty(selectedConfigs)) {
                                            message.warn('当前未选择数据!');
                                        } else {
                                            confirm({
                                                title: `确认删除${selectedConfigs.length}条当前配置?`,
                                                icon: <ExclamationCircleOutlined />,
                                                okText: '确认',
                                                okType: 'danger',
                                                cancelText: '取消',
                                                onOk() {
                                                    deleteConfig(selectedConfigs);
                                                },
                                            });
                                        }
                                    }}>批量删除</Button>
                                </div>
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
