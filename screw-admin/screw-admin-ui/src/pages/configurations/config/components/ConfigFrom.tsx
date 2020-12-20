import React from 'react';
import ProForm, { ProFormText } from '@ant-design/pro-form';
import { AppConfig } from '@/pages/configurations/config/data';
import { ExclamationCircleTwoTone } from '@ant-design/icons';
import { Tooltip } from 'antd';

const ConfigForm: React.FC<any> = ( props ) => {

    const appConfig: AppConfig = props.appConfig;

    return (
        <ProForm.Group>
            {/* @ts-ignore*/}
            <ProFormText
                key="name"
                width="m"
                name="configName"
                label="配置名称"
                placeholder="请输入名称"
                rules={[{ required: true, message: '请输入名称!' }]}
                initialValue={appConfig ? appConfig.configName : ''}
            />
            {/* @ts-ignore*/}
            <ProFormText
                key="name"
                width="m"
                name="configKey"
                label="配置key"
                placeholder="请输入key"
                rules={[{ required: true, message: '请输入key!' }]}
                initialValue={appConfig ? appConfig.configKey : ''}
                fieldProps={{
                    suffix: (
                        <Tooltip placement="top" title="保持各个配置key必须唯一">
                            <ExclamationCircleTwoTone twoToneColor="#f37b1d" />
                        </Tooltip>
                    ),
                }}
                disabled={appConfig ? true : false}
            />
        </ProForm.Group>
    )
}

export default ConfigForm;
