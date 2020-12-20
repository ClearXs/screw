import { connect, Dispatch } from 'umi';
import React, { useState, useEffect, useRef } from 'react';
import { DataSourceState, DataSourceModelType } from '@/pages/configurations/datasource/models/index';
import { PageHeaderWrapper } from '@ant-design/pro-layout';
import ProTable, { ProColumns, ActionType } from '@ant-design/pro-table';
import { ModalForm } from '@ant-design/pro-form';
import { PlusOutlined, ExclamationCircleOutlined, ExclamationCircleTwoTone } from '@ant-design/icons';
import { Spin, Button, message, Modal, Popover } from 'antd';
import { DataSource } from './data';
import DataSourceForm from './components/DataSourceForm';

interface DataSourceProps {
    dispatch: Dispatch;
    dataSourceModel: DataSourceModelType;
}

const { confirm } = Modal;

const Server: React.FC<DataSourceProps> = (props) => {
    const [isLoading, setIsLoading] = useState<boolean>(true);

    const [dataSources, setDataSources] = useState<DataSource[]>([]);

    const actionRef = useRef<ActionType>();

    const columns: ProColumns<DataSource>[] = [
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
            title: '数据源名称',
            dataIndex: 'datasourceName',
        },
        {
            title: '数据源类型',
            dataIndex: 'datasourceType',
        },
        {
            title: '数据源ip',
            dataIndex: 'datasourceIp',
            hideInSearch: false,
        },
        {
            title: '端口',
            dataIndex: 'datasourcePort',
            hideInSearch: false,
        },
        {
            title: '创建时间',
            dataIndex: 'createTime',
            hideInSearch: false,
        },
        {
            title: '操作',
            key: 'option',
            valueType: 'option',
            render: (_, record) => [
                <ModalForm
                    onFinish={async (value: any) => {
                        value.id = record.id;
                        value.version = record.version;
                        await fromSubmit(value);
                        return true;
                    }}
                    title={exampleDatasource()}
                    trigger={<a type="primary">编辑</a>}
                    key="details"
                >
                    <DataSourceForm {...record} />
                </ModalForm>,
                <a onClick={() => {
                    confirm({
                        title: '确认删除当前数据源?',
                        icon: <ExclamationCircleOutlined />,
                        okText: '确认',
                        okType: 'danger',
                        cancelText: '取消',
                        onOk() {
                            deleteDataSource(record);
                        },
                    })
                }}>
                    删除
                </a>,
                <a key="testConnection" onClick={() => testConnection(record)}>测试连接</a>,
            ],
        },
    ];

    const testConnection = (record: DataSource) => {
        props.dispatch({
            type: 'dataSource/testConnect',
            payload: record,
            callback: (result) => {
                const { success, data, msg } = result;
                if (success) {
                    if (data) {
                        message.success('数据源测试连接成功');
                    } else {
                        message.error('数据源测试连接失败，检查配置！');
                    }
                } else {
                    message.error(msg);
                }
            },
        });
    }

    const handleQuery = () => {
        const { dispatch } = props;
        dispatch({
            type: 'dataSource/queryDatabase',
            payload: {},
            callback: (dataSource: DataSourceState) => {
                setIsLoading(false);
                setDataSources(dataSource.dataSources);
            },
        });
    }

    const fromSubmit = (value: any, handlerState: number | undefined): Promise => {
        return new Promise<boolean>((resolve) => {
            setIsLoading(true);
            // 添加
            if (handlerState === 0) {
                props.dispatch({
                type: 'dataSource/addDatabase',
                payload: value,
                callback: ( result ) => {
                    const { success, msg } = result;
                    if (success) {
                        message.success(msg);
                        handleQuery();
                    } else {
                        message.error(msg);
                    }
                    
                }
                })
                // 编辑
            } else {
                props.dispatch({
                type: 'dataSource/editDatabase',
                payload: value,
                callback: ( result ) => {
                    const { success, msg } = result;
                    if (success) {
                        message.success(msg);
                        handleQuery();
                    } else {
                        message.error(msg);
                    }
                }
                })
            }
            resolve(true);
        })
    };

    const deleteDataSource = (value: any) => {
        return new Promise<boolean>((resolve) => {
            setIsLoading(true);
            props.dispatch({
                type: 'dataSource/deleteDatabase',
                payload: value,
                callback: (result) => {
                const { success, msg } = result;
                if (success) {
                    message.success(msg);
                    handleQuery();
                } else {
                    message.error(msg);
                }
                resolve(true);
                }
            });
        })
    }

    const exampleDatasource = ():React.ReactNode => {
        return (
            <div>
                [新增数据源]
                <Popover placement="bottom" title='示例' content={
                    (
                        <div>
                            <p>数据源名称：HBP</p>
                            <p>数据源类型：oracle</p>
                            <p>连接名称：orcl</p>
                            <p>数据源ip：localhost</p>
                            <p>数据源端口：1521</p>
                            <p>用户名：root</p>
                            <p>密码：123456</p>
                            <p>数据源会被解析成：jdbc:oracle:thin:@localhost:1521:orcl</p>
                        </div>
                    )
                }>
                    <ExclamationCircleTwoTone style={{marginLeft: '5px'}} twoToneColor="#f37b1d" />
                </Popover >
            </div>
        )
    }

    useEffect(() => {
        if (isLoading) {
            handleQuery();
        }
    });

    return (
        <Spin spinning={isLoading}>
            <PageHeaderWrapper>
                <ProTable<DataSource>
                    actionRef={actionRef}
                    columns={columns}
                    dataSource={dataSources}
                    pagination={false}
                    search={false}
                    options={false}
                    toolBarRender={() => [
                        <ModalForm
                            title={exampleDatasource()}
                            onFinish={async (value: any) => {
                                await fromSubmit(value, 0);
                                return true;
                            }}
                            trigger={
                                <Button type="primary">
                                    <PlusOutlined />
                                    新增数据源
                                </Button>
                            }
                        >
                            <DataSourceForm />
                        </ModalForm>,
                    ]}
                />
            </PageHeaderWrapper>
        </Spin>
    );
};

export default connect(({ isLoading, dataSources }: DataSourceState) => ({
    isLoading,
    dataSources,
}))(Server);
