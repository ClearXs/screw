import { connect, Dispatch } from 'umi';
import React, { useState, useEffect, useRef, useImperativeHandle } from 'react';
import { AppConfigDataState, AppConfigDataModelType } from '@/pages/configurations/config/datas/models/data';
import { AppConfigData, AppConfigDataQueryParams} from '@/pages/configurations/config/data'
import { FormatDTO } from '@/pages/configurations/config/datas/data';
import ProTable, { ProColumns, ActionType } from '@ant-design/pro-table';
import Field from '@ant-design/pro-field';
import { Spin, Tag, Button, Modal, Input, Select, Descriptions, message, Upload, Tooltip, Form, Tabs } from 'antd';
import { ExclamationCircleOutlined } from '@ant-design/icons';
import _ from 'lodash';
import Editor from 'for-editor'

interface DataProps {
    dispatch: Dispatch;
    appDataModel: AppConfigDataModelType;
}

const { confirm } = Modal;

const { Option } = Select;

const { TabPane } = Tabs;

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

    const [pageTotal, setPageTotal] = useState<number>(0);

    const [isLoading, setIsLoading] = useState<boolean>(true);

    const [appData, setAppData] = useState<AppConfigData[]>([]);

    const [queryParams, setQueryParams] = useState<AppConfigDataQueryParams>({
        pageNum: 1,
        pageSize: 300,
        configVersionId: props['appVersion'].id
    })

    const [state] = useState<ProFieldFCMode>('read');

    const [plain] = useState<boolean>(false);

    const [drawnVisible, setDrawVisible] = useState<boolean>(false);

    const [selectedData, setSelectedConfigData] = useState<AppConfigData[]>();

    const [tableVisible, setTableVisible] = useState<boolean>(true);

    const [propertiesData, setPropertiesData] = useState<string>();

    const [yamlData, setYamlData] = useState<string>();

    const [jsonData, setJsonData] = useState<string>();

    const [currentActiveKey, setActiveKey] = useState<string>('properties');

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
                if (record.configDataStoreState && record.configDataStoreState !== '') {
                    return (
                        <Form>
                            <Form.Item 
                                style={{marginBottom: '0'}}
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
                                        buildBulkEditData(newData);
                                        props.dispatch({
                                            type: 'appConfigData/changeConfigData',
                                            payload: newData,
                                            callback: (data) => {
                                                setAppData(data);
                                            }
                                        });
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
        // {
        //     title: '配置类型',
        //     dataIndex: 'configDataType',
        //     render: (text, record, index) => {
        //         if (record.configDataStoreState && record.configDataStoreState === 'SAVE_TEMP') {
        //             return (
        //                 <Select
        //                     defaultValue={['CUSTOM']}
        //                     onChange={(value) => {
        //                         appData[index].configDataType = value;
        //                         props.dispatch({
        //                             type: 'appConfigData/changeConfigData',
        //                             payload: appData,
        //                             callback: (data) => {
        //                                 setAppData(data);
        //                             }
        //                         })
        //                     }}
        //                     optionLabelProp="lable"
        //                 >
        //                     { dataType.map((item, index) => {
        //                         const disabled = item.value !== 'CUSTOM'
        //                         return (
        //                             <Option
        //                                 key={index}
        //                                 value={item.value}
        //                                 label={item.text}
        //                                 disabled={disabled}
        //                             >
        //                                 <span>{item.text}</span>
        //                             </Option>
        //                         )
        //                     })}
        //                 </Select>
        //             )
        //         } else {
        //             const filter = dataType.filter((item) => {
        //                 return item.value === text;
        //             });
        //             if (!_.isEmpty(filter)) {
        //                 return filter[0].text;
        //             } else {
        //                 return text;
        //             }
        //         }

        //     }
        // },
        // {
        //     title: '存储状态',
        //     dataIndex: 'configDataStoreState',
        //     render: (text, record) => {
        //         return <Tag color={dataStoreState[record.configDataStoreState].color}>{dataStoreState[record.configDataStoreState].text}</Tag>
        //     }
        // },
        {
            title: '配置数据',
            dataIndex: 'configDataValue',
            render: (text, record, index, action) => {
                if (record.configDataStoreState !== '') {
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
                                });
                            }}>
                                <Input 
                                    value={record.configDataValue} 
                                    onChange={(event) => {
                                        appData[index].configDataValue = event.target.value;
                                        if (!_.isEmpty(record.configDataKey)) {
                                            buildBulkEditData(appData);
                                        }
                                        props.dispatch({
                                            type: 'appConfigData/changeConfigData',
                                            payload: appData,
                                            callback: (data) => {
                                                setAppData(data);
                                            }
                                        });
                                    }} 
                                />
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
                        <Tooltip placement="top" title={
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
                        } color="#F6F8FA">
                            <a style={{cursor: 'default'}}>查看</a>
                        </Tooltip>
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
                            const appData = new Array();
                            appData.push(row);
                            row.index = index;
                            deleteData(appData);
                        },
                    });
                }}>
                    删除
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
                    return { ...value, isDraw: false, configVersion: props.appVersion, index: index};
                });
                setAppData(list);
                buildBulkEditData(list);
                setPageTotal(result.appConfigData.total);
                setIsLoading(false);
            }
        })
    }

    const addRow = (addData?: AppConfigData, isPut: boolean) => {
        const index = appData.length === 0 ? 0 : appData.length === 1 ? 1 : appData.reduce((v1: AppConfigData, v2: AppConfigData) => {
            return v1.index > v2.index ? v1.index : v2.index;
        }) + 1
        // 添加一行数据
        const newData: AppConfigData = {
            id: '',
            configDataKey: addData ? addData.configDataKey : '',
            configDataType: 'CUSTOM',
            configDataValue: addData ? addData.configDataValue : '',
            configDataStoreState: 'SAVE_TEMP',
            configVersionId: props.appVersion.id,
            configVersion: props.appVersion,
            isDraw: false,
            index: index
        }
        if (isPut) {
            props.dispatch({
                type: 'appConfigData/changeConfigData',
                payload: [...appData, newData],
                callback: (data) => {
                    setAppData(data);
                }
            })
        }
        return newData;
    }

    const deleteData = (records: AppConfigData[]) => {
        if (_.isEmpty(records)) {
            return;
        }
        setIsLoading(true);
        // 存储在数据库的数据
        const nonIdRecord = records.filter((record: AppConfigData) => {
            return record.id !== '';
        });
        // 前端缓存的数据
        const nullIdRecord = records.filter((record: AppConfigData) => {
            return record.id === '';
        });
        if (!_.isEmpty(nullIdRecord)) {
            const nullIndex = nullIdRecord.map((value) => {
                return value.index;
            });
            const filterData = appData.filter((value: AppConfigData, appIndex: number) => {
                return !nullIndex.includes(value.index);
            });
            props.dispatch({
                type: 'appConfigData/changeConfigData',
                payload: filterData,
                callback: (data) => {
                    setAppData(data);
                    setIsLoading(false);
                    message.success('删除成功');
                }
            });
        }
        if (!_.isEmpty(nonIdRecord)) {
            // 调用服务删除
            props.dispatch({
                type: 'appConfigData/deleteConfigData',
                payload: nonIdRecord,
                callback: (result) => {
                    if (result.success) {
                        message.success(result.msg);
                        handleQuery();
                    } else {
                        message.error(result.msg);
                        setIsLoading(false);
                    }
                }
            });
        }
    }

    const saveConfigData = (logicOperate: string) => {
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
                <Tooltip placement="top" title={`如果当前版本是启用的，那么根据当前配置创建一个没有版本号的未发布版本，否则保存当前配置`}>
                    <Button type="primary" onClick={() => {
                            const configVersionStatus = props.appVersion.configVersionStatus;
                            // 判断当前版本是否是启动的版本，若不是，则只是保存数据，否则开启一个未发布的版本
                            if (configVersionStatus === 'OPEN') {
                                saveConfigData('SAVE');
                            } else {
                                saveConfigData('EDIT');
                            }
                    }}>
                        保存
                    </Button>
                </Tooltip>
            )
        }
        nodes.push(
            <Tooltip placement="top">
                <Button type="primary" onClick={() => addRow(undefined, true)}>
                    添加
                </Button>
            </Tooltip>
        )
        nodes.push(
            <Button type="default" onClick={(event) => {
                event.stopPropagation();
                if (_.isEmpty(selectedData)) {
                    message.warn('请选择数据!');
                } else {
                    confirm({
                        title: `确认删除${selectedData.length}条当前配置数据?`,
                        icon: <ExclamationCircleOutlined />,
                        okText: '确认',
                        okType: 'danger',
                        cancelText: '取消',
                        onOk() {
                            deleteData(selectedData);
                        },
                    });
                }
            }}>批量删除</Button>
        )
        nodes.push(
            <Tooltip placement="top" title={`可以通过properties/yaml/json方式行进快速编辑，编辑完成后需要点击"完毕"按钮`}>
                <Button type="dashed" onClick={(event) => {
                    event.stopPropagation();
                    setTableVisible(false);
                }}>
                    批量编辑
                </Button>
            </Tooltip>

        )
        return nodes;
    }

    /**
     * 改变data的格式
     * 如 spring.datasource = 21 -> key: spring.datasource, value: 21
     * value: /n改变
     * type：'properties', 'yml', ''
     */
    const changeDataFormat = () => {
        // properties不需要进行转换
        if (currentActiveKey === 'properties') {
            buildTableData(propertiesData);
            setTableVisible(true);
        } else {
            parseFormat((result) => {
                const { success, data: { properties } } = result;
                if (success) {
                    buildTableData(properties);
                }
                setTableVisible(true);
            });
        }
    }

    const buildTableData = (value: string) => {
        if (_.isEmpty(value)) {
            setAppData([]);
            const deletedConfigData = appData.filter((item: AppConfigData) => {
                return item.configDataStoreState !== 'SAVE_TEMP'
            });
            deleteData(deletedConfigData);
            return ;
        }
        const enterArray = value.split('\n');
        // 解析每一行数据中的等号, 取0做key, 1做value
        if (_.isEmpty(enterArray)) {
            return;
        }
        let newAppData: AppConfigData[] = appData;
        // 做删除操作
        const keys = enterArray.map((item) => {
            return item.split('=')[0];
        });
        if (!_.isEmpty(keys)) {
            const includeConfigData = appData.filter((item: AppConfigData) => {
                return keys.includes(item.configDataKey);
            });
            newAppData = includeConfigData;
            if (_.isEmpty(newAppData)) {
                newAppData = appData;
            }
            // 获取那些存在于数据库的数据
            const deletedConfigData = appData.filter((item: AppConfigData) => {
                return !keys.includes(item.configDataKey) && item.configDataStoreState !== 'SAVE_TEMP';
            })
            deleteData(deletedConfigData);
        }
        enterArray.forEach((item, index) => {
            let key, value;
            try {
                const keyValue = item.split('=');
                key = keyValue[0];
                value = keyValue[1];
            } catch (error) {
                key = '';
                value = '';
            }
            if (_.isEmpty(key)) {
                return;
            }
            // 1.以key为基准找寻当前configData，如果值不同，那么进行更新
            // 2.否则就是新的row
            const sameKeyConfigData = newAppData.filter((item: AppConfigData) => {
                return item.configDataKey === key;
            });
            if (_.isEmpty(sameKeyConfigData)) {
                const addData: AppConfigData = {
                    configDataKey: key,
                    configDataValue: value,
                }
                newAppData.push(addRow(addData, false));
            } else {
                if (sameKeyConfigData.configDataValue !== value) {
                    newAppData = newAppData.map((item) => {
                        if (item.configDataKey === key) {
                            item.configDataValue = value;
                        }
                        return item;
                    });
                }
            }
        });
        setAppData(newAppData);
    }

    /**
     * 构建批量编辑的数据，通过产生properties进行构建
     * @param list 
     * @returns
     */
    const buildBulkEditData = (list: AppConfigData[]) => {
        if (_.isEmpty(list)) {
            return;
        }
        let propertiesData = '';
        list.forEach((item: AppConfigData, index: number) => {
            if (index != 0) {
                propertiesData += '\n';
            }
            propertiesData += item.configDataKey + '=' + item.configDataValue
        });
        const param: FormatDTO = {};
        param.properties = propertiesData;
        param.json = '';
        param.yaml = '';
        props.dispatch({
            type: 'appConfigData/transfer',
            payload: param,
            callback: (result) => {
                if (result.success) {
                    const data: FormatDTO = result.data;
                    setPropertiesData(data.properties);
                    setJsonData(data.json);
                    setYamlData(data.yaml);
                } else {
                    message.warn(result.msg);
                }
            }
        })
        setPropertiesData(propertiesData);
    }

    const parseFormat = async function(callback: Function) {
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
                            message.error(result.msg);
                            resolve(false);
                        }
                    }
                })
            })
        },
        checkedSaveStatus: (): Promise<boolean> => {
            return new Promise((resolve, reject) => {
                resolve(tableVisible);
            })
        }
    }))

    return (
        <Spin spinning={isLoading}>
            { tableVisible && <ProTable<AppConfigData>
                columns={columns}
                actionRef={actionRef}
                dataSource={appData}
                // pagination={{
                //     current: defaultPageNum,
                //     pageSize: defaultPageSize,
                //     total: pageTotal,
                //     onChange: (pageNum, pageSize) => {
                //         setIsLoading(true);
                //         setQueryParams(Object.assign(queryParams, {pageNum, pageSize}));
                //         handleQuery();
                //     }
                // }}
                pagination={false}
                search={false}
                options={false}
                toolbar={{
                    actions: renderToolbar()
                }}
                tableAlertRender={false}
                tableAlertOptionRender={false}
                rowSelection={{
                    onChange: (selectedKeys, selectedRow) => {
                        setSelectedConfigData(selectedRow);
                    },
                }}
                rowKey="index"
            /> }
            { !tableVisible && <Tabs
                defaultActiveKey={currentActiveKey}
                activeKey={currentActiveKey}
                tabBarExtraContent={
                    <Button type="link" onClick={(event) => {
                        event.stopPropagation();
                        changeDataFormat();
                    }}>完毕</Button>
                }
                onChange={(activeKey) => {
                    parseFormat((result) => {
                        const { success } = result;
                        if (success) {
                            setActiveKey(activeKey);
                        }
                    })
                }}
            >
                <TabPane tab="properties" key="properties" >
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
                <TabPane tab="yaml" key="yaml" >
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
            </Tabs> }
        </Spin>
    )
}

export default connect(({ isLoading, appConfigData, queryParams }: AppConfigDataState) => ({
    isLoading,
    appConfigData,
    queryParams
}))(Data);
