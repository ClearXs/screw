import { connect, Dispatch } from 'umi';
import React, { useState, useEffect, useRef, useImperativeHandle  } from 'react';
import { AppConfigDataState, AppConfigDataModelType } from '@/pages/configurations/config/datas/models/data';
import { AppConfigData, AppConfigDataQueryParams} from '@/pages/configurations/config/data'
import ProTable, { ProColumns, ActionType } from '@ant-design/pro-table';
import Field, { ProFieldFCMode } from '@ant-design/pro-field';
import { Spin, Tag, Button, Modal, Input, Select, Descriptions, Drawer, message, Upload, Tooltip, Form } from 'antd';
import { ExclamationCircleOutlined } from '@ant-design/icons';
import _ from 'lodash';
import Editor from 'for-editor'

interface DataProps {
    dispatch: Dispatch;
    appDataModel: AppConfigDataModelType;
}

const { confirm } = Modal;

const { Option } = Select;

const dataType = [
    {
        index: 0,
        value: 'HBP',
        text: 'HBP'
    },
    {
        index: 1,
        value: 'FILE',
        text: '文件'
    },
    {
        index: 2,
        value: 'CUSTOM',
        text: '自定义'
    }
]

const dataStoreState = {
    'DEPLOY': {
        index: 0,
        value: 'DEPLOY',
        text: '发布',
        color: 'blue',
    },
    'SAVE': {
        index: 1,
        value: 'SAVE',
        text: '保存',
        color: 'green'
    },
    'SAVE_TEMP': {
        index: 2,
        value: 'SAVE_TEMP',
        text: '暂存',
        color: ''
    }
}

const Data: React.FC<DataProps> = (props) => {

    const [defaultPageNum] = useState<number>(1);

    const [defaultPageSize] = useState<number>(15);

    const [pageTotal, setPageTotal] = useState<number>(0);

    const [isLoading, setIsLoading] = useState<boolean>(true);

    const [appData, setAppData] = useState<AppConfigData[]>([]);

    const [queryParams, setQueryParams] = useState<AppConfigDataQueryParams>({
        pageNum: defaultPageNum,
        pageSize: defaultPageSize,
        configVersionId: props['appVersion'].id
    })

    const [state] = useState<ProFieldFCMode>('read');

    const [plain] = useState<boolean>(false);

    const [drawnVisible, setDrawVisible] = useState<boolean>(false);

    let isGone = useRef<boolean>(false);

    const actionRef = useRef<ActionType>();

    const columns: ProColumns<AppConfigData>[] = [
        {
            title: '序号',
            dataIndex: 'index',
            valueType: 'indexBorder',
        },
        {
            title: '配置key',
            dataIndex: 'configDataKey',
            tip: '必填，如果没有值将不会保存上',
            render: (text, record, index) => {
                if (record.configDataStoreState && record.configDataStoreState !== 'DEPLOY') {
                    return (
                        <Form>
                            <Form.Item 
                                style={{marginBottom: '0'}}
                                rules={[{ required: true, message: 'Please input your username!' }]}
                                validateStatus="warning"
                            >
                                <Input
                                    defaultValue={record.configDataKey}
                                    placeholder='请输入key'
                                    value={record.configDataKey}
                                    onChange={(event) => {
                                        const newData = appData.map((value, dataIndex) => {
                                            if (dataIndex === index) {
                                                value.configDataKey = event.target.value;
                                            }
                                            return value;
                                        });
                                        props.dispatch({
                                            type: 'appConfigData/changeConfigData',
                                            payload: newData,
                                            callback: (data) => {
                                                setAppData(data);
                                            }
                                        })
                                    }}
                                />
                            </Form.Item>
                        </Form>
                    )
                } else {
                    return (
                        <div>{record.configDataKey}</div>
                    )
                }
            }
        },
        {
            title: '配置类型',
            dataIndex: 'configDataType',
            render: (text, record, index) => {
                if (record.configDataStoreState && record.configDataStoreState === 'SAVE_TEMP') {
                    return (
                        <Select
                            defaultValue={['CUSTOM']}
                            onChange={(value) => {
                                appData[index].configDataType = value;
                                props.dispatch({
                                    type: 'appConfigData/changeConfigData',
                                    payload: appData,
                                    callback: (data) => {
                                        setAppData(data);
                                    }
                                })

                            }}
                            optionLabelProp="lable"
                        >
                            { dataType.map((item, index) => {
                                const disabled = item.value !== 'CUSTOM'
                                return (
                                    <Option
                                        key={index}
                                        value={item.value}
                                        lable={item.text}
                                        disabled={disabled}
                                    >
                                        <span>{item.text}</span>
                                    </Option>
                                )
                            })}
                        </Select>
                    )
                } else {
                    const filter = dataType.filter((item) => {
                        return item.value === text;
                    });
                    if (!_.isEmpty(filter)) {
                        return filter[0].text;
                    } else {
                        return text;
                    }
                }

            }
        },
        {
            title: '存储状态',
            dataIndex: 'configDataStoreState',
            render: (text, record) => {
                return <Tag color={dataStoreState[record.configDataStoreState].color}>{dataStoreState[record.configDataStoreState].text}</Tag>
            }
        },
        {
            title: '配置数据',
            dataIndex: 'configDataValue',
            tip: '建议不要加上注释，否则不能正常解析数据',
            render: (text, record, index, action) => {
                if (record.configDataStoreState !== 'DEPLOY') {
                    if (record.configDataType === 'CUSTOM') {
                        return (
                            <a onClick={(event) => {
                                event.stopPropagation();
                                setDrawVisible(true);
                                const newData = appData.map((value, dataIndex) => {
                                    if (dataIndex === index) {
                                        value.isDraw = true;
                                    }
                                    return value;
                                });
                                props.dispatch({
                                    type: 'appConfigData/changeConfigData',
                                    payload: newData,
                                    callback: (data) => {
                                        setAppData(data);
                                    }
                                })
                            }}>
                                {_.isEmpty(record.configDataValue) ? '创建' : '修改'}
                                { (drawnVisible && record.isDraw) &&
                                    <Drawer
                                        title="配置数据"
                                        placement='bottom'
                                        closable={true}
                                        onClose={(event) => {
                                            event.stopPropagation();
                                            setDrawVisible(false);
                                            const newData = appData.map((value, dataIndex) => {
                                                if (dataIndex === index) {
                                                    value.isDraw = false;
                                                }
                                                return value;
                                            });
                                            props.dispatch({
                                                type: 'appConfigData/changeConfigData',
                                                payload: newData,
                                                callback: (data) => {
                                                    setAppData(data);
                                                }
                                            })
                                        }}
                                        height={500}
                                        visible={drawnVisible}
                                    >
                                        <Editor
                                            value={record.configDataValue}
                                            toolbar={{
                                                h1: false,
                                                h2: false,
                                                h3: false,
                                                h4: false,
                                                img: false,
                                                link: false,
                                                code: true,
                                                preview: true,
                                                expand: true,
                                                undo: true,
                                                redo: true,
                                                save: true,
                                                subfield: true
                                            }}
                                            onChange={(value) => {
                                                appData[index].configDataValue = value;
                                                props.dispatch({
                                                    type: 'appConfigData/changeConfigData',
                                                    payload: appData,
                                                    callback: (data) => {
                                                        setAppData(data);
                                                    }
                                                })
                                            }}
                                        />
                                    </Drawer>
                                }
                            </a>
                        )
                    } else if (record.configDataType === 'FILE') {
                        return (
                            <Upload>
                                <a>上传附件</a>
                            </Upload>
                        )
                    }

                } else {
                    return (
                        <a onClick={() => {
                            Modal.info({
                                title: `${record.configDataKey}数据`,
                                content: (
                                    <Descriptions>
                                        <Descriptions.Item>
                                            <Field
                                                text={record.configDataValue && record.configDataValue}
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
        },
        {
            title: '备注',
            dataIndex: 'remark',
            render: (text, record, index, action) => [
                <Form>
                    <Form.Item 
                        style={{marginBottom: '0'}}
                    >
                        <Input
                            defaultValue={record.remark}
                            placeholder='请输入备注'
                            value={record.remark}
                            onChange={(event) => {
                                const newData = appData.map((value, dataIndex) => {
                                    if (dataIndex === index) {
                                        value.remark = event.target.value;
                                    }
                                    return value;
                                });
                                props.dispatch({
                                    type: 'appConfigData/changeConfigData',
                                    payload: newData,
                                    callback: (data) => {
                                        setAppData(data);
                                    }
                                })
                            }}
                        />
                    </Form.Item>
                </Form>
            ]
        },
        {
            title: '操作',
            key: 'option',
            valueType: 'option',
            render: (text, row, index, action) => [
                <a onClick={() => {
                    confirm({
                        title: '确认删除当前数据?',
                        icon: <ExclamationCircleOutlined />,
                        okText: '确认',
                        okType: 'danger',
                        cancelText: '取消',
                        onOk() {
                            deleteData(row, index);
                        },
                    });
                }}>
                    <Tooltip placement="top" title={props.hiddenButtons ? `删除后，建议点击确认保存数据，否则配置数据不会实时更新` : `保存、发布的数据将进行物理删除，暂存的进行逻辑删除`}>删除</Tooltip>
                </a>
            ]
        }
    ]

    const handleQuery = () => {
        props.dispatch({
            type: 'appConfigData/queryConfigData',
            payload: queryParams,
            callback: (result) => {
                let list = result.appConfigData.list;
                list = list.map((value, index) => {
                    return { ...value, isDraw: false, configVersion: props.appVersion};
                })
                setAppData(list);
                setPageTotal(result.appConfigData.total);
                setIsLoading(false);
            }
        })
    }

    const addRow = () => {
        // 添加一行数据
        const newData: AppConfigData = {
            id: '',
            configDataKey: '',
            configDataType: 'CUSTOM',
            configDataValue: '',
            configDataStoreState: 'SAVE_TEMP',
            configVersionId: props.appVersion.id,
            configVersion: props.appVersion,
            isDraw: false,
        }
        props.dispatch({
            type: 'appConfigData/changeConfigData',
            payload: [...appData, newData],
            callback: (data) => {
                setAppData(data);
            }
        })
    }

    const deleteData = (record: AppConfigData, index: number) => {
        setIsLoading(true);
        if (_.isEmpty(record.id)) {
            const filterData = appData.filter((value, appIndex: number) => {
                return index !== appIndex;
            })
            props.dispatch({
                type: 'appConfigData/changeConfigData',
                payload: filterData,
                callback: (data) => {
                    setAppData(data);
                    setIsLoading(false);
                    message.success('删除成功');
                }
            })
        } else {
            // 调用服务删除
            props.dispatch({
                type: 'appConfigData/deleteConfigData',
                payload: record,
                callback: (result) => {
                    if (result.success) {
                        message.success(result.msg);
                        handleQuery();
                    } else {
                        message.error(result.msg);
                        setIsLoading(false);
                    }
                }
            })
        }
    }

    const saveConfigData = (logicOperate: string) =>{
        if (appData.length < 1) {
            message.warn(`当前没有数据，无法进行操作`);
            return ;
        }
        setIsLoading(true);
        // 更改数据中的存储状态
        props.dispatch({
            type: 'appConfigData/saveConfigData',
            payload: { appData, logicOperate},
            callback: (result) => {
                setIsLoading(false);
                if (result.success) {
                    message.success(result.msg);
                    props.refreshVersion();
                } else {
                    message.error(result.msg);
                }
            }
        })
    }

    const renderToolbar = () => {
        const nodes = new Array<React.ReactNode>();
        // 进行的页面与版本状态判断是否需要隐藏发布与保存
        if (props.appVersion.configVersionStatus !== 'NON_DEPLOY') {
            if (!props.hiddenButtons) {
                nodes.push(
                    <Tooltip placement="top" title={`根据当前配置创建一个开启状态的版本`}>
                        <Button type="primary" onClick={() => saveConfigData('DEPLOY')}>
                            发布
                        </Button>
                    </Tooltip>
                )
            }

        }
        if (!props.hiddenButtons) {
            nodes.push(
                <Tooltip placement="top" title={`根据当前配置创建一个没有版本号的未发布版本`}>
                    <Button type="primary" onClick={() => saveConfigData('SAVE')}>
                        保存
                    </Button>
                </Tooltip>
    
            )
        }
        nodes.push(
            <Tooltip placement="top" title={
                props.hiddenButtons ? `点击提交将会保存数据` : `关闭弹框将会自动保存修改的配置`
            }>
                <Button type="primary" onClick={() => addRow()}>
                    添加
                </Button>
            </Tooltip>

        )
        return nodes;
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
        return () => {
            isGone.current = true;
        };
    })

    useImperativeHandle(props.configDataRef, () => ({
        saveTemp: (): Promise<boolean> => {
            return new Promise((resolve, reject) => {
                props.dispatch({
                    type: 'appConfigData/saveConfigData',
                    payload: { appData, logicOperate: 'EDIT'},
                    callback: (result) => {
                        if (result.success) {
                            resolve(true);
                        } else {
                            resolve(false);
                        }
                    }
                })
            })
        }
    }))

    return (
        <Spin spinning={isLoading}>
            <ProTable<AppConfigData>
                columns={columns}
                actionRef={actionRef}
                dataSource={appData}
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
                toolbar={{
                    actions: renderToolbar()
                }}
            />
        </Spin>
    )

}

export default connect(({ isLoading, appConfigData, queryParams }: AppConfigDataState) => ({
    isLoading,
    appConfigData,
    queryParams
}))(Data);
