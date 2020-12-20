import React, { useEffect, useState, useRef, useImperativeHandle } from 'react';
import ProTable, { ProColumns, ActionType } from '@ant-design/pro-table';
import ProForm from '@ant-design/pro-form';
import Field, { ProFieldFCMode } from '@ant-design/pro-field';
import { Result, Modal, Descriptions, Menu } from 'antd';
import { AppServer } from '@/pages/configurations/server/data';
// @ts-ignore
import { AppConfig } from '@/pages/configurations/config/data'
import _ from 'lodash'

const ServerForm: React.FC<any> = ( props ) => {

    const defaultServer: Map<string, AppServer[]> = props.defaultServer;

    const actionRef = useRef<ActionType>();

    const [state] = useState<ProFieldFCMode>('read');

    const [plain] = useState<boolean>(false);

    const [selectedKeys, setSelectedKeys] = useState<string[]>();

    const [init, setInit] = useState<boolean>(true);

    const [selectedConfigs, setSelectedConfigs] = useState<AppConfig[]>();

    const [configs, setConfigs] = useState<AppConfig[]>();

    const columns: ProColumns<AppConfig>[] = [
        {
            title: '序号',
            dataIndex: 'index',
            valueType: 'indexBorder',
        },
        {
            title: 'id',
            dataIndex: 'id',
            hideInTable: true,
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
        }
    ]

    const renderMenu = (): React.ReactNode => {
        if (_.isEmpty(defaultServer)) {
            return null;
        }
        const menus = new Array<React.ReactNode>();
        for (let key in defaultServer) {
            const menu: React.ReactNode = (
                <Menu.ItemGroup key={key} title={key}>
                    {
                        defaultServer[key].map((appServer: AppServer) => (
                            <Menu.Item key={appServer.id}>{appServer.serverName}</Menu.Item>
                        ))
                    }
                </Menu.ItemGroup>
            )
            menus.push(menu);
        }
        return menus;
    }

    // @ts-ignore
    const getConfigs = (keys?: string[]): AppConfig[] => {
        let id;
        if (!_.isEmpty(keys)) {
            id = keys[0];
        } else if (!_.isEmpty(selectedKeys)) {
            id = selectedKeys[0];
        } else {
            return [];
        }
        for (let key in defaultServer) {
            const appServers = defaultServer[key]
            for (let index in appServers) {
                if (appServers[index].id === id) {
                    return appServers[index].appConfigs;
                }
            }
        }
    }

    const getKeys = () => {
        if (_.isEmpty(selectedKeys)) {
            const keys = new Array();
            for (let key in defaultServer) {
                const appServers = defaultServer[key]
                keys.push(appServers[0].id);
                break;
            }
            return keys;
        }
        return selectedKeys;
    }

    const render: React.ReactNode = () => {
        if (_.isEmpty(defaultServer)) {
            return (
                <Result
                    status="warning"
                    title="默认服务为空，点击提交创建服务"
                />
            )
        } else {
            return (
                <ProForm.Group>
                    <ProTable<AppConfig>
                        columns={columns}
                        actionRef={actionRef}
                        tableRender={(_, dom) => (
                            <div style={{display: 'flex', width: '100%'}}>
                                <Menu
                                    selectedKeys={selectedKeys}
                                    style={{ width: 100 }}
                                    mode='inline'
                                    onSelect={(item) => {
                                        // @ts-ignore
                                        setSelectedKeys(item.selectedKeys);
                                        // @ts-ignore
                                        const configs = getConfigs(item.selectedKeys);
                                        setConfigs(configs);
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
                        dataSource={configs}
                        search={false}
                        options={false}
                        pagination={false}
                        tableStyle={{width: 600}}
                        rowSelection={{
                            onChange: (selectedKeys, selectedRow) => {
                                setSelectedConfigs(selectedRow);
                            }
                        }}
                        tableAlertRender={false}
                        tableAlertOptionRender={false}
                        rowKey="id"
                    >

                    </ProTable>
                </ProForm.Group>
            )
        }
    }

    useEffect(() => {
        if (init && props.init) {
            const keys = getKeys();
            setSelectedKeys(keys);
            const configs = getConfigs(keys);
            setConfigs(configs);
            setInit(false)
        }
    })

    useImperativeHandle(props.serverConfigRef, () => ({
        getSelectedRows: () => {
            return new Promise((resolve) => {
                resolve(selectedConfigs);
            })
        }
    }))

    return (
        <div>
            {render()}
        </div>
    )
}

export default ServerForm;
