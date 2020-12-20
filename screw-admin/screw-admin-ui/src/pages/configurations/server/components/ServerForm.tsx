import React from 'react';
import ProForm, { ProFormText } from '@ant-design/pro-form';
import { ExclamationCircleTwoTone } from '@ant-design/icons';
import { Tooltip } from 'antd';
import { AppServer } from '../data';
import _ from 'lodash';

const ServerForm: React.FC<any> = ( props ) => {

    const server: AppServer = props;

    return (
        <ProForm.Group>
            {/* @ts-ignore */}
            <ProFormText
                key="name"
                width="m"
                name="serverName"
                label="应用服务"
                placeholder="请输入名称"
                rules={[{ required: true, message: '请输入名称!' }]}
                initialValue={server.serverName}
                fieldProps={{
                    suffix: (
                        <Tooltip placement="top" title="保持服务名不重复">
                            <ExclamationCircleTwoTone twoToneColor="#f37b1d" />
                        </Tooltip>
                    )
                }}
            />
            {/* @ts-ignore */}
            <ProFormText
                key="ip"
                width="m"
                name="serverIp"
                label="ip"
                placeholder="请输入ip"
                rules={[{ required: true, message: '请输入ip!' }]}
                initialValue={server.serverIp}
            />
            {/* @ts-ignore */}
            <ProFormText
                key="port"
                width="m"
                name="serverPort"
                label="端口"
                placeholder="请输入port"
                rules={[{ required: true,  message: '请输入port!' }]}
                initialValue={server.serverPort}
            />
            {/* @ts-ignore */}
            <ProFormText
                key="version"
                width="m"
                name="serverVersion"
                label="版本"
                placeholder="清输入版本号!"
                initialValue={server.serverVersion}
                rules={[{ required: true, message: '清输入版本号!' }]}
                fieldProps={{
                    suffix: (
                        <Tooltip placement="top" title="服务版本号">
                            <ExclamationCircleTwoTone twoToneColor="#f37b1d" />
                        </Tooltip>
                    )
                }}
            >
            </ProFormText>
        </ProForm.Group>
    )
}

export default ServerForm;
