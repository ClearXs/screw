import React, { useState, useImperativeHandle, useEffect } from 'react';
import ProForm, { ProFormText, ProFormSelect } from '@ant-design/pro-form';
import { Button, Spin, notification, message, Collapse, Tabs, Popover, Table } from 'antd';
import { BulbTwoTone } from '@ant-design/icons';
import { FormatDTO } from '@/pages/configurations/config/datas/data';
import { DataSource } from '../data';
import { c3p0, HikariCP, druid, dbcp, connectionPoolColumn, HikariCPData, ConnectionPool, c3p0Data, druidData, dbcpData } from '@/pages/configurations/datasource/constant';
import _ from 'lodash';
import $ from 'jquery';
import Editor from 'for-editor'

const { Panel } = Collapse;

const { TabPane } = Tabs;

const ServerForm: React.FC<any> = ( props ) => {

    const { record } = props;

    const { activeKey, dispatch } = props;

    const [isLoading, setIsLoading] = useState<boolean>(false);

    const [loadingTip, setLoadingTip] = useState<string>('');

    const [isShowCollapse, setIsShowCollapse] = useState<boolean>(true);

    const [propertiesData, setPropertiesData] = useState<string>(HikariCP);

    const [yamlData, setYamlData] = useState<string>();

    const [jsonData, setJsonData] = useState<string>();

    const [currentActiveKey, setActiveKey] = useState<string>('properties');

    const [isInit, setIsInit] = useState<boolean>(true);

    const [dataSourceConnectType, setDataSourceConnectType] = useState<string>('default');

    const parseFormat = async (callback: Function) => {
        const format = () => {
            return new Promise((resolve, reject) => {
                const format: FormatDTO = {};
                if (currentActiveKey === 'properties') {
                    format.properties = propertiesData;
                    format.json = '';
                    format.yaml = '';
                } else if (currentActiveKey === 'json') {
                    format.json = jsonData;
                    format.properties = '';
                    format.yaml = '';
                } else if (currentActiveKey === 'yaml') {
                    format.yaml = yamlData;
                    format.properties = '';
                    format.json = '';
                }
                props.dispatch({
                    type: 'appConfigData/transfer',
                    payload: format,
                    callback: (result) => {
                        if (result.success) {
                            const data: FormatDTO = result.data;
                            setPropertiesData(data.properties);
                            setJsonData(data.json);
                            setYamlData(data.yaml);
                            let properties = data.properties;
                            if (_.isEmpty(properties)) {
                                properties = '';
                            }
                            resolve(true);
                        } else {
                            message.warn(result.msg);
                            resolve(false);
                        }
                        callback && callback(result);
                    }
                });
            });
        }
        await format();
    }

    useEffect(() => {
        if (record && isInit) {
            if (record.datasourceConnectType === 'druid') {
                setIsShowCollapse(true);
            }
            if (currentActiveKey !== 'json') {
                setActiveKey('json');
                setJsonData(record.datasourceConnectVariables);
            }
            setIsInit(false);
        }
    })

    useImperativeHandle(props.formRef, () => ({
        getVariables: (): Promise<string> => {
            return new Promise((resolve, reject) => {
                if (!isShowCollapse) {
                    return resolve('');
                }
                if (currentActiveKey === 'json') {
                    return resolve(jsonData);
                } else {
                    parseFormat((result) => {
                        const { success, data } = result;
                        if (success) {
                            return resolve(data.json);
                        } else {
                            return resolve('');
                        }
                    })
                }
            })
        }
    }))

    return (
        <Spin spinning={isLoading} tip={loadingTip} >
            <div>
                <ProForm.Group>
                    <ProFormText
                        key="name"
                        width="m"
                        name="datasourceName"
                        label="数据源名称"
                        placeholder="请输入数据源名称"
                        rules={[{ required: true, message: '请输入数据源名称!' }]}
                        initialValue={record && record.datasourceName}
                    />
                    <ProFormText
                        key="connectName"
                        width="m"
                        name="datasourceConnectName"
                        label={`${activeKey === 'oracle' ? '实例名称' : '数据库名称'}`}
                        placeholder={`请输入${activeKey === 'oracle' ? '实例名称' : '数据库名称'}`}
                        rules={[{ required: true, message: `请输入${activeKey === 'oracle' ? '实例名称' : '数据库名称'}` }]}
                        initialValue={record && record.datasourceConnectName}
                    />
                    <ProFormText
                        key="ip"
                        width="m"
                        name="datasourceIp"
                        label="数据源ip"
                        placeholder="请输入数据源ip"
                        rules={[{ required: true, message: '请输入数据源ip!' }]}
                        initialValue={record && record.datasourceIp}
                    />
                    <ProFormText
                        key="dbPort"
                        width="m"
                        name="datasourcePort"
                        label="数据源端口"
                        placeholder="请输入数据源端口"
                        rules={[{ required: true,  message: '请输入数据源端口!' }]}
                        initialValue={record && record.datasourcePort}
                    />
                    <ProFormText
                        key="username"
                        width="m"
                        name="datasourceUsername"
                        label="用户名"
                        placeholder="清输入用户名"
                        initialValue={record && record.datasourceUsername}
                        rules={[{ required: true, message: '清输入用户名!' }]}
                    />
                    <ProFormText
                        key="password"
                        width="m"
                        name="datasourcePassword"
                        label="密码"
                        placeholder="清输入密码"
                        initialValue={record && record.datasourcePassword}
                        rules={[{ required: true, message: '清输入密码!' }]}
                    />
                    <ProFormSelect
                        key="connectType"
                        name="datasourceConnectType"
                        label="数据源连接类型"
                        valueEnum={{
                            default: 'HikariCP',
                            druid: 'Druid',
                            c3p0: 'C3p0',
                            dbcp: 'Dbcp'
                        }}
                        placeholder="请输入数据源连接类型"
                        rules={[{ required: true, message: '请输入数据源连接类型!' }]}
                        initialValue={record ? record.datasourceConnectType : dataSourceConnectType}
                        disabled={record && record.datasourceConnectType}
                        fieldProps={{
                            onSelect: (value) => {
                                setDataSourceConnectType(value)
                                if (value === 'c3p0') {
                                    setPropertiesData(c3p0);
                                } else if (value === 'default') {
                                    setPropertiesData(HikariCP);
                                } else if (value === 'druid') {
                                    setPropertiesData(druid);
                                } else if (value === 'dbcp') {
                                    setPropertiesData(dbcp);
                                }
                            },
                        }}
                    />
                </ProForm.Group>
                {
                    isShowCollapse && 
                        <Collapse defaultActiveKey={['variables']} ghost>
                            <Panel header="数据源连接池配置" key="variables" extra={<Popover title={`数据源连接池的配置`} content={(
                                <Tabs defaultActiveKey={dataSourceConnectType} activeKey={dataSourceConnectType} >
                                    <TabPane tab="HikariCP" key="default">
                                        <Table<ConnectionPool> 
                                            columns={connectionPoolColumn}
                                            dataSource={HikariCPData}
                                        />
                                    </TabPane>
                                    <TabPane tab="druid" key="druid">
                                        <Table 
                                            columns={connectionPoolColumn}
                                            dataSource={druidData}
                                        />
                                    </TabPane>
                                    <TabPane tab="c3p0" key="c3p0">
                                        <Table 
                                            columns={connectionPoolColumn}
                                            dataSource={c3p0Data}
                                        />
                                    </TabPane>
                                    <TabPane tab="dbcp" key="dbcp">
                                        <Table 
                                            columns={connectionPoolColumn}
                                            dataSource={dbcpData}
                                        />
                                    </TabPane>
                                </Tabs>
                            )}><BulbTwoTone/></Popover>}>
                                <Tabs defaultActiveKey={currentActiveKey} activeKey={currentActiveKey} onChange={(activeKey) => {
                                    parseFormat((result) => {
                                        const { success } = result;
                                        if (success) {
                                            setActiveKey(activeKey);
                                        }
                                    })
                                }}>
                                    <TabPane tab="properties" key="properties">
                                        <Editor
                                            value={propertiesData}
                                            toolbar={{
                                                h1: false,
                                                h2: false,
                                                h3: false,
                                                h4: false,
                                                img: false,
                                                link: false,
                                                code: false,
                                                preview: true,
                                                expand: true,
                                                undo: false,
                                                redo: false,
                                                save: false,
                                                subfield: true
                                            }}
                                            onChange={(value) => {
                                                setPropertiesData(value);
                                            }}
                                        />
                                    </TabPane>
                                    <TabPane tab="yaml" key="yaml">
                                        <Editor
                                            value={yamlData}
                                            toolbar={{
                                                h1: false,
                                                h2: false,
                                                h3: false,
                                                h4: false,
                                                img: false,
                                                link: false,
                                                code: false,
                                                preview: true,
                                                expand: true,
                                                undo: false,
                                                redo: false,
                                                save: false,
                                                subfield: true
                                            }}
                                            onChange={(value) => {
                                                setYamlData(value);
                                            }}
                                        />
                                    </TabPane>
                                    <TabPane tab="json" key="json">
                                        <Editor
                                            value={jsonData}
                                            toolbar={{
                                                h1: false,
                                                h2: false,
                                                h3: false,
                                                h4: false,
                                                img: false,
                                                link: false,
                                                code: false,
                                                preview: true,
                                                expand: true,
                                                undo: false,
                                                redo: false,
                                                save: false,
                                                subfield: true
                                            }}
                                            onChange={(value) => {
                                                setJsonData(value);
                                            }}
                                        />
                                    </TabPane>
                                </Tabs>
                            </Panel>
                        </Collapse>
                }
                <Button type="primary" onClick={(event) => {
                    event.stopPropagation();
                    setIsLoading(true);
                    setLoadingTip('测试连接中...');
                    let testDataSource: DataSource = {
                        datasourceName: $('#datasourceName')[0]['defaultValue'],
                        datasourceConnectName: $('#datasourceConnectName')[0]['defaultValue'],
                        datasourceIp: $('#datasourceIp')[0]['defaultValue'],
                        datasourcePort: $('#datasourcePort')[0]['defaultValue'],
                        datasourceUsername: $('#datasourceUsername')[0]['defaultValue'],
                        datasourcePassword: $('#datasourcePassword')[0]['defaultValue']
                    };
                    testDataSource.datasourceType = activeKey;
                    dispatch({
                        type: 'dataSource/testConnectByEntity',
                        payload: testDataSource,
                        callback: (result) => {
                            setIsLoading(false);
                            setLoadingTip('');
                            const { success, data, msg } = result;
                            if (success) {
                                if (data) {
                                    notification.success({
                                        description: '数据源测试连接成功'
                                    });
                                } else {
                                    notification.error({
                                        description: '数据源测试连接失败，检查配置！'
                                    });
                                }
                            } else {
                                message.error(msg);
                            }
                        },
                    });
                }}>
                    测试数据源
                </Button>
            </div>
        </Spin>
    )
}

export default ServerForm;
