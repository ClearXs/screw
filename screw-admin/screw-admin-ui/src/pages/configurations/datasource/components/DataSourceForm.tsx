import React from 'react';
import ProForm, { ProFormText, ProFormRadio } from '@ant-design/pro-form';
import { ExclamationCircleTwoTone } from '@ant-design/icons';
import { Tooltip } from 'antd';
import { DataSource } from '../data';
import _ from 'lodash'

const ServerForm: React.FC<any> = ( props ) => {

    const dataSource: DataSource = props;

    return (
        <ProForm.Group>
            <ProFormText
                key="name"
                width="m"
                name="datasourceName"
                label="数据源名称"
                placeholder="请输入数据源名称"
                rules={[{ required: true, message: '请输入数据源名称!' }]}
                initialValue={dataSource.datasourceName}
            />
            <ProFormRadio.Group
                key="dbType"
                name="datasourceType"
                width="m"
                label="数据源类型"
                radioType="button"
                options={[
                    {
                        label: 'mysql',
                        value: 'mysql',
                    },
                    {
                        label: 'oracle',
                        value: 'oracle',
                    },
                    {
                        label: 'sqlserver',
                        value: 'sqlserver',
                    },
                ]}
                placeholder="请选择数据源类型"
                rules={[{ required: true, message: '请选择数据源类型!' }]}
                initialValue={dataSource.datasourceType}
            />
            <ProFormText
                key="connectName"
                width="m"
                name="datasourceConnectName"
                label="连接名称"
                placeholder="请输入连接名称"
                rules={[{ required: true, message: '请输入连接名称!' }]}
                initialValue={dataSource.datasourceConnectName}
                fieldProps={{
                    suffix: (
                        <Tooltip placement="top" title="数据库名称或sid(SERVICE_NAME)名称">
                            <ExclamationCircleTwoTone twoToneColor="#f37b1d" />
                        </Tooltip>
                    )
                }}
            />
            <ProFormText
                key="ip"
                width="m"
                name="datasourceIp"
                label="数据源ip"
                placeholder="请输入数据源ip"
                rules={[{ required: true, message: '请输入数据源ip!' }]}
                initialValue={dataSource.datasourceIp}
            />
            <ProFormText
                key="dbPort"
                width="m"
                name="datasourcePort"
                label="数据源端口"
                placeholder="请输入数据源端口"
                rules={[{ required: true,  message: '请输入数据源端口!' }]}
                initialValue={dataSource.datasourcePort}
            />
            <ProFormText
                key="username"
                width="m"
                name="datasourceUsername"
                label="用户名"
                placeholder="清输入用户名"
                initialValue={dataSource.datasourceUsername}
                rules={[{ required: true, message: '清输入用户名!' }]}
            />
            <ProFormText
                key="password"
                width="m"
                name="datasourcePassword"
                label="密码"
                placeholder="清输入密码"
                initialValue={dataSource.datasourcePassword}
                rules={[{ required: true, message: '清输入密码!' }]}
            />
        </ProForm.Group>
    )
}

export default ServerForm;
