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
            <div style={{marginRight: '80px', marginLeft: '30px'}}>
                <ProFormText
                    key="name"
                    name="configName"
                    label="配置名称"
                    width="m"
                    placeholder="请输入名称"
                    rules={[{ required: true, message: '请输入名称!' }]}
                    initialValue={appConfig ? appConfig.configName : ''}
                />
            </div>
            {/* @ts-ignore*/}
            <ProFormText
                key="name"
                name="configKey"
                label="配置key"
                width="m"
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
