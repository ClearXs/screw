import { connect, Dispatch } from 'umi';
import React, { useState, useEffect, useRef } from 'react';
import { AppConfigVersionState, AppConfigVersionModelType } from '@/pages/configurations/config/version/models/version';
import { AppConfig, AppConfigVersion, AppConfigVersionQueryParams} from '@/pages/configurations/config/data'
import ProTable, { ProColumns, ActionType } from '@ant-design/pro-table';
import { ModalForm } from '@ant-design/pro-form';
import { Spin, Tag, message } from 'antd';
import ConfigData from '@/pages/configurations/config/datas/index'

interface VersionProps {
    dispatch: Dispatch;
    appConfigVersionModel: AppConfigVersionModelType;
    appConfig: AppConfig;
    isInit: boolean;
}

const Version: React.FC<VersionProps> = (props) => {

    const [defaultPageNum] = useState<number>(1);

    const [defaultPageSize] = useState<number>(15);

    const [pageTotal, setPageTotal] = useState<number>(0);

    const [isLoading, setIsLoading] = useState<boolean>(true);

    const [appConfigVersions, setAppConfigVersion] = useState<AppConfigVersion[]>([]);

    const [queryParams, setQueryParams] = useState<AppConfigVersionQueryParams>({
        pageNum: defaultPageNum,
        pageSize: defaultPageSize,
        configId: props.appConfig.id,
    })

    const [isInitData, setIsInitData] = useState<boolean>(false);

    const versionStatusMap = {
        'OPEN': {
            color: 'green',
            text: '开启',
        },
        'CLOSED': {
            color: 'volcano',
            text: '关闭',
        },
        'NON_DEPLOY': {
            color: 'red',
            text: '未发布'
        }
    };

    let isGone = useRef<boolean>(false);

    const actionRef = useRef<ActionType>();

    const configDataRef = useRef<any>();

    const columns: ProColumns<AppConfigVersion>[] = [
        {
            title: '序号',
            dataIndex: 'index',
            valueType: 'indexBorder',
        },
        {
            title: '版本号',
            dataIndex: 'configVersion',
            search: false,
        },
        {
            title: '状态',
            dataIndex: 'configVersionStatus',
            search: false,
            render: (_, record) => {
                return (
                    <Tag color={versionStatusMap[record.configVersionStatus].color}>
                        {versionStatusMap[record.configVersionStatus].text}
                    </Tag>
                )
            }
        },        
        {
            title: '创建时间',
            dataIndex: 'createTime',
            search: false,
        },
        {
            title: '更新时间',
            dataIndex: 'updateTime',
            search: false,
        },
        {
            title: '操作',
            key: 'option',
            valueType: 'option',
            render: (text, row, index, action) => renderOption(row, index)
        }
    ]

    const renderOption = (row: AppConfigVersion, index: number) => {
        const nodes = new Array<React.ReactNode>();
        if (row.configVersionStatus !== 'OPEN') {
            nodes.push(
                <a onClick={(event) => {
                    setIsLoading(true);
                    event.stopPropagation();
                    props.dispatch({
                        type: 'appConfigVersion/openedVersion',
                        payload: row,
                        callback: (result) => {
                            setIsLoading(false);
                            const { success, msg } = result;
                            if (success) {
                                message.success(msg);
                                handleQuery();
                            } else {
                                message.error(msg);
                            }
                        }
                    })
                }}>开启</a>
            )
        }
        nodes.push(
            <ModalForm
                title={`${props.appConfig.configName}数据`}
                onVisibleChange={(visible: boolean) => {
                    setIsInitData(visible);
                    const newData = appConfigVersions.map((value, dataIndex) => {
                        if (dataIndex === index) {
                            value.isModal = false;
                        }
                        return value;
                    });
                    configDataRef.current.saveTemp();
                    props.dispatch({
                        type: 'appConfigVersion/changeConfigVersions',
                        payload: newData,
                        callback: (data) => {
                            setAppConfigVersion(data)
                        }
                    });
                    if (!visible) {
                    }
                }}
                width={850}
                trigger={
                    <a onClick={(event) => {
                        event.stopPropagation();
                        setIsInitData(true);
                        const newData = appConfigVersions.map((value, dataIndex) => {
                            if (dataIndex === index) {
                                value.isModal = true;
                            }
                            return value;
                        });
                        props.dispatch({
                            type: 'appConfigVersion/changeConfigVersions',
                            payload: newData,
                            callback: (data) => {
                                setAppConfigVersion(data)
                            }
                        });
                    }}>编辑</a>
                }
                visible={row.isModal}
            >
                {
                    (isInitData && row.isModal) && (
                        <ConfigData 
                            appVersion={row} 
                            isInit={isInitData} 
                            refreshVersion={handleQuery}
                            configDataRef={configDataRef} 
                        />
                    )
                }
            </ModalForm>)
        return nodes;
    }

    const handleQuery = () => {
        setIsLoading(true);
        props.dispatch({
            type: 'appConfigVersion/queryConfigVersions',
            payload: queryParams,
            callback: (result) => {
                const list = result.appConfigVersions.list.map((value) => {
                    value.isModal = false;
                    return value;
                })
                setPageTotal(result.appConfigVersions.total)
                setAppConfigVersion(list);
                setIsLoading(false);
            }
        })
    }

    useEffect(() => {
        if (!isGone.current) {
            if (props.isInit) {
                setIsLoading(true);
                handleQuery();
            }
        } else {
            isGone.current = false;
        }
        return unload;
    })

    const unload = () => {
        isGone.current = true;
    }

    return (
        <Spin spinning={isLoading}>
            <ProTable<AppConfigVersion>
                columns={columns}
                actionRef={actionRef}
                pagination={{
                    current: defaultPageNum,
                    pageSize: defaultPageSize,
                    total: pageTotal,
                    onChange: (pageNum, pageSize) => {
                        setIsLoading(true);
                        setQueryParams(Object.assign(queryParams, {pageNum, pageSize}));
                        handleQuery();
                    }
                }}
                search={false}
                options={false}
                dataSource={appConfigVersions}
            />
        </Spin>
    )
}

export default connect(({ isLoading, appConfigVersions, queryParams }: AppConfigVersionState) => ({
    isLoading,
    appConfigVersions,
    queryParams
}))(Version);