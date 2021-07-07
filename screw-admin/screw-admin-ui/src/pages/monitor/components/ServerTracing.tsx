import React, { useEffect, useState, useRef, useImperativeHandle } from 'react';
import { Spin, Menu, Empty , Drawer, Descriptions, Tag, List } from 'antd';
import ProTable, { ProColumns, ActionType } from '@ant-design/pro-table';
import { TracingModel, ServerMonitorModel, ServerMonitorQueryParams, TracingSpan, TracingLog } from '@/pages/monitor/data';
import styles from '@/pages/monitor/less/index.less';
import * as moment from 'moment';
import _ from 'lodash';

/**
 * tracing的常量
 */
const screwTracing = {
    'code': 'code',
    'status': 'status',
    'invokeId': '请求id',
    'serviceName': '请求服务名',
    'methodName': '请求方法名',
    'parameters': '请求参数',
    'returnType': '返回类型',
    'result': '结果',
    'error': '错误'
}

/**
 * rpc调用code
 */
const code = {
    0: {
        name: 'UNKNOWN',
        text: '未知'
    },
    1: {
        name: 'FAILED',
        text: '失败'
    },
    2: {
        name: 'HEART_BEATS',
        text: '心跳'
    },
    66: {
        name: 'RPC_REQUEST',
        text: '远程请求调用'
    },
    67: {
        name: 'RPC_RESPONSE',
        text: '远程调用响应'
    },
    68: {
        name: 'SERVICE_REGISTER',
        text: '生产者服务注册'
    },
    69: {
        name: 'SERVICE_SUBSCRIBE',
        text: '消费者服务订阅'
    },
    70: {
        name: 'RESPONSE_SUBSCRIBE',
        text: '注册中心响应订阅'
    },
    71: {
        name: 'RESPONSE_SUBSCRIBE',
        text: '服务下线通知'
    }, 
    72: {
        name: 'UNICAST',
        text: '单播'
    },
    73: {
        name: 'BROADCAST',
        text: '广播'
    },
    74: {
        name: 'METRICS',
        text: '性能指标'
    },
    75: {
        name: 'TRACING',
        text: '链路追踪'
    },
    76: {
        name: 'MONITOR_ADDRESS',
        text: '监控中心地址'
    }
}

const rpcInvokeStatus = {
    32: {
        name: 'OK',
        text: '成功',
        color: '#87d068'
    },
    33: {
        name: 'service invoke error',
        text: '服务调用错误',
        color: '#f5222d'

    },
    64: {
        name: 'service not found',
        text: '服务未找到',
        color: '#ffbb96'
    },
    65: {
        name: 'service illegal access',
        text: '服务调用访问错误',
        color: '#f50'
    }
}

interface ExceptionTrace {
    cause: string;
    traceStacks: string[];
}

const ServerTracing: React.FC<any> = ( props ) => {

    const actionRef = useRef<ActionType>();

    const [isInit, setIsInit] = useState<boolean>(true);

    const [isLoading, setIsLoading] = useState<boolean>(true);

    const [serverTrace, setServerTrace] = useState<TracingModel>();

    const [spans, setSpans] = useState<TracingSpan[]>();

    const [selectedKeys, setSelectedKeys] = useState<string[]>([]);

    const [spanKeys, setSpanKeys] = useState<string[]>([]);

    const [isShowDrawn, setIsShowDrawn] = useState<boolean>(false);

    const [clickSpan, setClickSpan] = useState<TracingSpan>();

    const columns: ProColumns<TracingSpan>[] = [
        {
            title: 'span id',
            dataIndex: 'spanId',
            search: false,
            render: (dom, record) => {
                return (
                    <div style={{ color: '#448dfe'}}>{record.context.spanId}</div>
                )
            }
        }, 
        {
            title: '操作名称',
            dataIndex: 'operationName',
            search: false,
            render: (dom, record) => {
                return (
                    <div style={{ color: '#448dfe'}}>{record.operationName}</div>
                )
            }
        }, 
        {
            title: '花费时间',
            dataIndex: 'costTime',
            search: false,
            render: (dom, record) => {
                return (
                    <Tag color="#3b5999">{moment(record.endTime - record.startTime).millisecond()}/ms</Tag>
                )
            }
        },
        {
            title: '状态',
            dataIndex: 'status',
            search: false,
            render: (dom, record) => {
                return (
                    <Tag color={rpcInvokeStatus[record.tags['status']].color}>{rpcInvokeStatus[record.tags['status']].text}</Tag>
                )
            }
        }
    ]

    useEffect(() => {
        if (isInit) {
            const serverMonitor: ServerMonitorModel = props.serverMonitor
            const dispatch = props.dispatch;
            if (!_.isEmpty(serverMonitor)) {
                const serverKey = serverMonitor.consumerKey ? serverMonitor.consumerKey : serverMonitor.providerKey;
                const queryParams: ServerMonitorQueryParams = {
                    serverKey: serverKey,
                    serverHost: serverMonitor.host,
                    serverPort: serverMonitor.port
                }
                dispatch({
                    type: 'monitor/getServerTracing',
                    payload: queryParams,
                    callback: (trace: TracingModel) => {
                        setIsInit(false);
                        setIsLoading(false);
                        if (!_.isEmpty(trace)) {
                            for (let key in trace?.tracers) {
                                spanKeys?.push(key);
                            }
                            const traceSpans: TracingSpan[] = getSpans(spanKeys[0], trace);
                            setSpanKeys(spanKeys);
                            setSelectedKeys([spanKeys[0]]);
                            setSpans(traceSpans);
                            setServerTrace(trace);
                        } else {
                            setSpans(undefined);
                            setServerTrace(undefined); 
                        }
                    }
                });
            }
        }
    })

    useImperativeHandle(props.traceRef, () => ({
        open: () => {
            setIsInit(true);
        },
        cancel: () => {
            setIsInit(false);
            setSpanKeys([]);
        }
    }))

    const renderMenu = (): React.ReactNode => {
        if (_.isEmpty(serverTrace)) {
            return null;
        }
        if (_.isEmpty(spanKeys)) {
            return null;
        }
        const menus = new Array<React.ReactNode>();
        spanKeys?.forEach((key: string) => {
            const menu: React.ReactNode = (
                <Menu.Item key={key} style={{height: 60}}>
                    <List.Item extra={`trace id: [${serverTrace?.tracers[key] && serverTrace?.tracers[key].context.tracerId}]`}>
                        <List.Item.Meta
                            title={`/${serverTrace?.tracers[key] ? serverTrace.tracers[key].operationName : ''}`}
                            description={`${serverTrace?.tracers[key] && moment(serverTrace.tracers[key].startTime).format('YYYY-MM-DD HH:mm:ss') }`}
                        />
                    </List.Item>
                </Menu.Item>
            )
            menus.push(menu);
        })
        return menus;
    }

    const getSpans = (itemKey: string, trace?: TracingModel): TracingSpan[] => {
        let tracers: Map<string, TracingSpan>;
        if (_.isEmpty(trace)) {
            // @ts-ignore
            tracers = serverTrace?.tracers;
        } else {
            // @ts-ignore
            tracers = trace?.tracers;
        }
        if (_.isEmpty(tracers)) {
            return [];
        }
        const traceSpans = new Array<TracingSpan>();
        const span: TracingSpan = tracers[itemKey];
        traceSpans.push(span);
        return traceSpans;
    }

    const render = (): React.ReactNode => {
        if (_.isEmpty(serverTrace)) {
            return (
                <Empty />
            )
        } else {
            return (
                <ProTable<TracingSpan>
                    columns={columns}
                    actionRef={actionRef}
                    search={false}
                    pagination={false}
                    options={false}
                    tableAlertRender={false}
                    tableAlertOptionRender={false}
                    tableRender={(_, dom) => (
                        <div style={{display: 'flex', width: '100%'}}>
                            <Menu
                                selectedKeys={selectedKeys}
                                style={{ maxWidth: 400, minHeight: 60}}
                                mode='inline'
                                onSelect={(item) => {
                                    // @ts-ignore
                                    setSelectedKeys(item.selectedKeys);
                                    // @ts-ignore
                                    setSpans(getSpans(item.selectedKeys[0]));
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
                    dataSource={spans}
                    onRow={(record, index) => {
                        return {
                            onClick: () => {
                                setIsShowDrawn(true);
                                setClickSpan(record);
                            }
                        }
                    }}
                    rowClassName={styles.traceColumn}
                    expandable={{
                        childrenColumnName: 'childSpans'
                    }}
                />
            )
        }
    }

    const renderDescriptionTags = (map: Map<string, object>): React.ReactNode => {
        const items: Array<React.ReactNode> = [];
        for (let key in map) {
            let value = map[key];
            let label = screwTracing[key];
            if (key === 'code') {
                value = code[map[key]].text; 
            } else if (key === 'status') {
                value = rpcInvokeStatus[map[key]].text;
            } else if (key === 'result') {
                value = JSON.stringify(map[key]);
            }
            items.push(
                <Descriptions.Item label={label}>{value}</Descriptions.Item>
            )
        }
        return items;
    }

    const renderDescriptionLogs = (logs: TracingLog[]): React.ReactNode => {
        if (_.isEmpty(logs)) {
            return null;
        }
        let items: Array<React.ReactNode> = [];
        logs.map((log: TracingLog) => {
            const fields = log.fields;
            for (let key in fields) {
                if (key === 'stack') {
                    const exceptionTrace: ExceptionTrace = fields[key];
                    items = [];
                    items.push(
                        <Descriptions.Item label={key}>{exceptionTrace.cause}<br/>{exceptionTrace.traceStacks.map((value: string) => {
                            return <div>{'  ' + value}</div>;
                        })}</Descriptions.Item>
                    )
                    break;
                } else {
                    items.push(
                        <Descriptions.Item label={key}>{fields[key]}</Descriptions.Item>
                    )
                }
            }
        });
        return items;
    }

    return (
        <Spin spinning={isLoading}>
            {
                render()
            }
            <Drawer
                closable={true}
                placement='right'
                visible={isShowDrawn}
                onClose={() => {
                    setIsShowDrawn(false);
                    setClickSpan(undefined);
                }}
                width={900}
            >
                {clickSpan && (
                    <Descriptions title={`/${clickSpan.operationName}-${clickSpan.context.spanId}-链路信息`}>
                        <Descriptions.Item label={`操作名称`}>{clickSpan.operationName}</Descriptions.Item>
                        <Descriptions.Item label={`开始时间`}>{moment(new Date(clickSpan.startTime)).format('YYYY-MM-DD hh:mm:ss')}</Descriptions.Item>
                        <Descriptions.Item label={`结束时间`}>{moment(new Date(clickSpan.endTime)).format('YYYY-MM-DD hh:mm:ss')}</Descriptions.Item>
                    </Descriptions>
                )}
                {clickSpan && (
                    <Descriptions title={`span-context`}>
                        <Descriptions.Item label={`spanId`}>{clickSpan.context.spanId}</Descriptions.Item>
                        <Descriptions.Item label={`traceId`}>{clickSpan.context.tracerId}</Descriptions.Item>
                    </Descriptions>
                )}
                {clickSpan && (
                    <Descriptions title={`span-tags`}>
                        {
                            renderDescriptionTags(clickSpan.tags)
                        }
                    </Descriptions>
                )}
                {clickSpan && (
                    <Descriptions title={`span-logs`}>
                        {
                            renderDescriptionLogs(clickSpan.logs)
                        }
                    </Descriptions>
                )}
            </Drawer>
        </Spin>
    )
}

export default ServerTracing;