import { connect, Dispatch } from 'umi';
import React, { useState, useEffect, useRef } from 'react';
import { MonitorState, MonitorModelType } from '@/pages/monitor/models/monitor';
import { PageHeaderWrapper } from '@ant-design/pro-layout';
import ProCard from '@ant-design/pro-card';
import { ServerMonitorQueryParams, ServerMonitorModel, Metrics, Sample, Tag } from '@/pages/monitor/data';
import { SettingOutlined, BranchesOutlined, AreaChartOutlined, CheckCircleOutlined, ExclamationCircleOutlined, ClockCircleOutlined  } from '@ant-design/icons';
import { Input, Spin, Statistic, Menu, Dropdown, Modal, Tag as TagCom, message, Empty } from 'antd';
import styles from '@/pages/monitor/less/index.less';
import JvmThreadChart from '@/pages/monitor/components/JvmThreadChart';
import JvmHeapMemoryChart from '@/pages/monitor/components/JvmHeapMemoryChart';
import JvmNonHeapMemoryChart from '@/pages/monitor/components/JvmNonHeapMemoryChart';
import ServerTracing from '@/pages/monitor/components/ServerTracing';
import _ from 'lodash';
import * as moment from 'moment';
import { byteTransfer } from '@/utils/utils'

interface MonitorProps {
    dispatch: Dispatch;
    appConfigModel: MonitorModelType;
}

/**
 * 指标数据
 */
type MetricsData = {
    name: string;
    value: string | number;
}

type ProCardLoading = {
    name: string;
    loading: boolean;
}

/**
 * 指标常量
 */
const METRIC = {
    CLASS_LOADER_METRICS: 'Class Loader Metrics',
    DISK_SPACE_METRICS: 'Disk Space Metrics',
    JVM_GC_METRICS: 'Jvm Gc Metrics',
    JVM_HEAP_MEMORY_METRICS: 'Jvm Heap Memory Metrics',
    JVM_NON_HEAP_MEMORY_METRICS: 'Jvm Non Heap Memory Metrics',
    JVM_THREAD_METRICS: 'Jvm Thread Metrics',
    PROCESSOR_METRICS: 'Processor Metrics',
    UPTIME_METRICS: 'UptimeMetrics'
}

const healthStatus = {
    'health': {
        color: 'success',
        text: '健康',
        component: <CheckCircleOutlined />
    },
    'close': {
        color: 'default',
        text: '关闭',
        component: <ClockCircleOutlined />
    },
    'warning': {
        color: 'warning',
        text: '警告',
        component: <ExclamationCircleOutlined />
    }
}

/**
 * 系统角色
 */
const systemRole =  {
    'consumer': {
        text: '消费者',
        color: '#55acee'
    },
    'provider': {
        text: '提供者',
        color: '#3b5999'
    }
}

const Monitor: React.FC<MonitorProps> = ( props ) => {

    const [isInit, setIsInit] = useState<boolean>(true);

    const [serverMonitors, setServerMonitors] = useState<ServerMonitorModel[]>([]);

    const [isLoading, setIsLoading] = useState<boolean>(true);

    /**
     * 健康列表定时器
     */
    let [asyncQueryInterval, setAsyncQueryInterval] = useState<NodeJS.Timeout>();

    const [isShowMetrics, setIsShowMetrics] = useState<boolean>(false);

    const [isShowTracing, setIsShowTracing] = useState<boolean>(false);

    /**
     * 指标定时器，用于收集指标数据
     */
    let [metricsTimer, setMetricsTimer] = useState<NodeJS.Timeout>();

    const [activeMetrics, setActiveMetrics] = useState<Map<string, Metrics[]>>();

    const [cardLoading, setCardLoading] = useState<ProCardLoading[]>();

    const [activeServerMonitor, setActiveSeverMonitor] = useState<ServerMonitorModel>();

    const [queryKey, setQueryKey] = useState<string>();

    const traceRef = useRef<any>();

    const queryMonitorServer = (params?: ServerMonitorQueryParams) => {
        setIsLoading(true);
        props.dispatch({
            type: 'monitor/getMonitorList',
            payload: params,
            callback: (result: ServerMonitorModel[]) => {
                setServerMonitors(result);
                setIsLoading(false);
            }
        })
    }

    const backQuery = (): Promise<boolean> => {
        return new Promise((resolve, reject) => {
            queryMonitorServer();
            resolve(true);
        })
    }

    const init = () => {
        if (props['location']) {
            const state = props['location'].state;
            if (state) {
                const queryParams: ServerMonitorQueryParams = {
                    serverKey: state.serverKey
                }
                setQueryKey(state.serverKey);
                queryMonitorServer(queryParams);
            } else {
                setQueryKey('');
                queryMonitorServer();
            }
        }
        setIsInit(false);
        const interval = setInterval(() => {
            backQuery();
        }, 3000);
        setAsyncQueryInterval(interval);
        initCardLoading();
    }

    const initCardLoading = () => {
        setCardLoading([
            {
                name: METRIC.CLASS_LOADER_METRICS,
                loading: true,
            }, {
                name: METRIC.DISK_SPACE_METRICS,
                loading: true
            }, {
                name: METRIC.JVM_GC_METRICS,
                loading: true
            }, {
                name: METRIC.JVM_HEAP_MEMORY_METRICS,
                loading: true
            }, {
                name: METRIC.JVM_NON_HEAP_MEMORY_METRICS,
                loading: true
            }, {
                name: METRIC.JVM_THREAD_METRICS,
                loading: true
            }, {
                name: METRIC.PROCESSOR_METRICS,
                loading: true
            }, {
                name: METRIC.UPTIME_METRICS,
                loading: true
            }
        ])
    }

    const loadServerMetrics = (serverMonitor: ServerMonitorModel) => {
        const serverKey = serverMonitor.providerKey ? serverMonitor.providerKey : serverMonitor.consumerKey;
        if (serverMonitor.health === 'close') {
            message.warn(`${serverKey}已经关闭无法查看性能指标`);
            return;
        }
        setIsShowMetrics(true);
        setActiveSeverMonitor(serverMonitor);
        const { dispatch } = props;
        const queryParams: ServerMonitorQueryParams = {
            serverKey: serverKey,
            serverHost: serverMonitor.host,
            serverPort: serverMonitor.port
        }
        dispatch({
            type: 'monitor/getActiveServerMetrics',
            payload: queryParams,
            callback: (serverMetrics: ServerMonitorModel) => {
                setActiveMetrics(serverMetrics.metrics);
            }
        });
        const interval = setInterval(() => {
            dispatch({
                type: 'monitor/getActiveServerMetrics',
                payload: queryParams,
                callback: (serverMetrics: ServerMonitorModel) => {
                    setActiveMetrics(serverMetrics.metrics);
                }
            });
        }, 30000);
        setMetricsTimer(interval);
    }

    const loadServerTracing = (serverMonitor: ServerMonitorModel) => {
        setIsShowTracing(true);
        setActiveSeverMonitor(serverMonitor);
        if (!_.isEmpty(traceRef.current)) {
            traceRef.current.open();
        }
    }

    const loadServerMetricsByKey = (key: string): React.ReactNode => {
        if (_.isEmpty(activeMetrics)) {
            return;
        }
        let reactNode: React.ReactNode;
        let metrics;
        if (key === METRIC.JVM_NON_HEAP_MEMORY_METRICS || key === METRIC.JVM_HEAP_MEMORY_METRICS) {
            metrics = activeMetrics['Jvm Memory Metrics'];
        } else {
            metrics = activeMetrics[key];
        }
        switch (key) {
            case METRIC.CLASS_LOADER_METRICS:
                reactNode = processClassLoaderMetrics(metrics);
                break;
            case METRIC.DISK_SPACE_METRICS:
            reactNode = processDiskSpaceMetrics(metrics);
                break;
            case METRIC.JVM_GC_METRICS:
                reactNode = processJvmGcMetrics(metrics);
                break;
            case METRIC.JVM_HEAP_MEMORY_METRICS:
                reactNode = processJvmHeapMemoryMetrics(metrics);
                break;
            case METRIC.JVM_NON_HEAP_MEMORY_METRICS:
                reactNode = processJvmNonHeapMemoryMetrics(metrics);
                break;
            case METRIC.JVM_THREAD_METRICS:
                reactNode = processJvmThreadMetrics(metrics);
                break;
            case METRIC.PROCESSOR_METRICS:
                reactNode = processProcessorMetrics(metrics);
                break;
            case METRIC.UPTIME_METRICS:
                reactNode = processUptimeMetrics(metrics);
                break;
            default:
                break;
        }
        return reactNode;
    }

    const processClassLoaderMetrics = (metrics: Metrics[]): React.ReactNode => {
        const node: Array<React.ReactNode> = [];
        let metricsData: MetricsData;
        metrics.forEach((o: Metrics, index: number) => {
            if (o.name === 'jvm.classes.loaded') {
                metricsData = {
                    name: '类加载数',
                    value: o.measurements ? o.measurements.reduce((a: number, b: Sample) => {
                        return a += b.value;
                    }, 0) + '/个': 0
                }
            } else if (o.name === 'jvm.classes.unloaded') {
                metricsData = {
                    name: '类未加载数',
                    value: o.measurements ? o.measurements.reduce((a: number, b: Sample) => {
                        return a += b.value;
                    }, 0) + '/个': 0
                }
            }
            node.push(
                <ProCard>
                    <Statistic value={metricsData.value} title={metricsData.name} />
                </ProCard>
            )
        });
        dismissCardLoading(METRIC.CLASS_LOADER_METRICS);
        return node;
    }

    const processDiskSpaceMetrics = (metrics: Metrics[]): React.ReactNode => {
        const node: Array<React.ReactNode> = [];
        let metricsData: MetricsData;
        let diskTotal: number = 0;
        let diskFree: number = 0;
        metrics.forEach((o: Metrics, index: number) => {
            const value = o.measurements.reduce((a: number, b: Sample) => {
                return a += b.value;
            }, 0);
            if (o.name === 'disk.total') {
                diskTotal = value;
                metricsData = {
                    name: '当前路径磁盘总空间',
                    value: o.measurements ? byteTransfer(value) : 0
                }
                node.push(
                    <ProCard>
                        <Statistic value={o.availableTags[0].value} title={'路径'}/>
                    </ProCard>
                )
            } else if (o.name === 'disk.free') {
                diskFree = value;
                metricsData = {
                    name: '当前路径磁盘可用大小',
                    value: o.measurements ? byteTransfer(value) : 0
                }
            }
            node.push(
                <ProCard>
                    <Statistic value={metricsData.value} title={metricsData.name} />
                </ProCard>
            )
        });
        node.push(
            <ProCard>
                <Statistic value={`${diskTotal === 0 ? 0 : ((diskFree / diskTotal) * 100).toFixed(2)}%`} title={`磁盘可用率`} />
            </ProCard>
        )
        dismissCardLoading(METRIC.DISK_SPACE_METRICS);
        return node;
    }

    const processJvmGcMetrics = (metrics: Metrics[]): React.ReactNode => {
        const node: Array<React.ReactNode> = [];
        let metricsData: MetricsData;
        metrics.forEach((o: Metrics, index: number) => {
            const value = o.measurements.reduce((a: number, b: Sample) => {
                return a += b.value;
            }, 0);
            if (o.name === 'jvm.gc.memory.promoted') {
                metricsData = {
                    name: 'GC时老年代分配的总内存空间',
                    value: o.measurements ? byteTransfer(value) : 0
                }
            } else if (o.name === 'jvm.gc.max.data.size') {
                metricsData = {
                    name: 'GC时老年代的最大总内存空间',
                    value: o.measurements ? byteTransfer(value) : 0
                }
            } else if (o.name === 'jvm.gc.live.data.size') {
                metricsData = {
                    name: 'GC时老年代的总内存空间',
                    value: o.measurements ? byteTransfer(value) : 0
                }
            } else if (o.name === 'jvm.gc.memory.allocated') {
                metricsData = {
                    name: 'GC时年轻代分配的总内存空间',
                    value: o.measurements ? byteTransfer(value) : 0
                }
            }
            node.push(
                <ProCard>
                    <Statistic value={metricsData.value} title={metricsData.name} />
                </ProCard>
            )
        });
        dismissCardLoading(METRIC.JVM_GC_METRICS);
        return node;
    }

    const processJvmHeapMemoryMetrics = (metrics: Metrics[]): React.ReactNode => {
        const time = moment(new Date()).format('YYYY-MM-DD HH:mm:ss');
        const memorySeries: MetricsData[] = processJvmMemoryMetrics(metrics, 'heap');
        dismissCardLoading(METRIC.JVM_HEAP_MEMORY_METRICS);
        return (
            <JvmHeapMemoryChart xHeapMemoryAxis={time} heapMemorySeries={memorySeries} />
        )
    }

    const processJvmNonHeapMemoryMetrics = (metrics: Metrics[]): React.ReactNode => {
        const time = moment(new Date()).format('YYYY-MM-DD HH:mm:ss');
        const memorySeries: MetricsData[] = processJvmMemoryMetrics(metrics, 'nonheap');
        dismissCardLoading(METRIC.JVM_NON_HEAP_MEMORY_METRICS);
        return (
            <JvmNonHeapMemoryChart xNonHeapMemoryAxis={time} nonHeapMemorySeries={memorySeries} />
        )
    }

    const processJvmMemoryMetrics = (metrics: Metrics[], factor: string): MetricsData[] => {
        const memorySeries: MetricsData[] = [];
        metrics.forEach((o: Metrics) => {
            let prefix = '';
            let name = '';
            let goon = true;
            o.availableTags.forEach((tag: Tag) => {
                if (tag.key === 'area') {
                    if (tag.value !== factor) {
                        goon = false;
                    }
                }
                if (tag.key == 'id') {
                    // 去掉Parallel Scavenge
                    name = tag.value.replace('PS ', '');
                }
            });
            if (!goon) {
                return;
            }
            if (o.name.indexOf('jvm.memory.max') > -1) {
                prefix = 'Max ';
            } else if (o.name.indexOf('jvm.memory.used') > -1) {
                prefix = 'Used ';
            } else if (o.name.indexOf('jvm.memory.committed') > -1) {
                prefix = 'Committed ';
            }
            const series: MetricsData = {
                name: prefix + name,
                value: o.measurements ? (o.measurements.reduce((a: number, b: Sample) => {
                    return a += b.value;
                }, 0) / 1048576).toFixed(0) : 0
            }
            memorySeries.push(series);
        });
        return memorySeries;
    }

    const processJvmThreadMetrics = (metrics: Metrics[]): React.ReactNode => {
        const threadSeries = [];
        metrics.forEach((o: Metrics) => {
            let name;
            if (o.name.indexOf('jvm.threads.states') > -1) {
                name = o.availableTags[0].value
            } else {
                if (o.name === 'jvm.threads.live') {
                    name = 'live';
                }
                if (o.name === 'jvm.threads.peak') {
                    o.name = 'peak';
                }
            }
            threadSeries.push({
                name: name,
                value: o.measurements ? o.measurements.reduce((a: number, b: Sample) => {
                    return a += b.value
                }, 0) : 0
            })
        });
        dismissCardLoading(METRIC.JVM_THREAD_METRICS);
        return (
            <JvmThreadChart xThreadAxis={moment(new Date()).format('YYYY-MM-DD HH:mm:ss')} threadSeries={threadSeries} />
        )
    }

    const processProcessorMetrics = (metrics: Metrics[]): React.ReactNode => {
        const node: Array<React.ReactNode> = [];
        let metricsData: MetricsData;
        metrics.forEach((o: Metrics, index: number) => {
            if (o.name === 'system.cpu.count') {
                metricsData = {
                    name: 'cpu核心数',
                    value: o.measurements ? o.measurements.reduce((a: number, b: Sample) => {
                        return a += b.value;
                    }, 0) + '/个': 0
                }
            } else if (o.name === 'system.cpu.usage') {
                metricsData = {
                    name: '系统cpu使用率',
                    value: o.measurements ? (o.measurements.reduce((a: number, b: Sample) => {
                        return a += b.value;
                    }, 0) * 100).toFixed(2) + '/%' : 0
                }
            } else if (o.name === 'process.cpu.usage') {
                metricsData = {
                    name: '应用cpu使用率',
                    value: o.measurements ? (o.measurements.reduce((a: number, b: Sample) => {
                        return a += b.value;
                    }, 0) * 100).toFixed(2) + '/%' : 0
                }
            }
            node.push(
                <ProCard>
                    <Statistic value={metricsData.value} title={metricsData.name} />
                </ProCard>
            )
        });
        dismissCardLoading(METRIC.PROCESSOR_METRICS);
        return node;
    }

    const processUptimeMetrics = (metrics: Metrics[]): React.ReactNode => {
        const node: Array<React.ReactNode> = [];
        let metricsData: MetricsData;
        metrics.forEach((o: Metrics, index: number) => {
            if (o.name === 'process.uptime') {
                metricsData = {
                    name: '应用运行时间',
                    value: o.measurements ? moment.duration(o.measurements.reduce((a: number, b: Sample) => {
                        return a += b.value;
                    }, 0) * 1000).minutes() + '/min': 0
                }
            } else if (o.name === 'process.start.time') {
                metricsData = {
                    name: '应用启动时间',
                    value: o.measurements ? moment(new Date(o.measurements.reduce((a: number, b: Sample) => {
                        return a += b.value;
                    }, 0) * 1000)).format('YYYY-MM-DD HH:mm:ss')  : 0
                }
            }
            node.push(
                <ProCard>
                    <Statistic value={metricsData.value} title={metricsData.name} />
                </ProCard>
            )
        });
        dismissCardLoading(METRIC.UPTIME_METRICS);
        return node;
    }

    const dismissCardLoading = (key: string) => {
        const cardFilter = cardLoading?.filter((o: ProCardLoading) => {
            return o.name === key;
        });
        if (_.isEmpty(cardFilter)) {
            return;
        }
        if (cardFilter[0].loading) {
            setCardLoading(cardLoading?.map((o: ProCardLoading) => {
                if (o.name === key) {
                    o.loading = false;
                }
                return o;
            }))
        }
    }

    const processCardLoading = (key: string): boolean => {
        if (_.isEmpty(cardLoading)) {
            return true;
        }
        const cardFilter = cardLoading?.filter((o: ProCardLoading) => {
            return o.name === key;
        });
        if (_.isEmpty(cardFilter)) {
            return true;
        }
        return cardFilter[0].loading;
    }

    const renderServer = (): React.ReactNode => {
        if (_.isEmpty(serverMonitors)) {
            return <Empty />;
        }
        const menus = new Array<React.ReactNode>();

        serverMonitors.forEach((serverMonitor: ServerMonitorModel, index: number) => {
            menus.push((
                <ProCard.Group
                    className={styles.card}
                    key={index}
                    title={
                        (
                            <div style={{display: 'flex'}}>
                                {
                                    serverMonitor.providerKey && <div style={{display: '-webkit-inline-box'}}><h4>{serverMonitor.providerKey}</h4><TagCom style={{marginLeft: '10px'}} color={systemRole[serverMonitor.providerRole].color}>{systemRole[serverMonitor.providerRole].text}</TagCom></div>
                                }
                                {
                                    serverMonitor.consumerKey && <div style={{display: '-webkit-inline-box', marginLeft: '10px'}}><h4>{serverMonitor.consumerKey}</h4><TagCom style={{marginLeft: '10px'}} color={systemRole[serverMonitor.consumerRole].color}>{systemRole[serverMonitor.consumerRole].text}</TagCom></div>
                                }
                            </div>
                        )
                    }
                    extra={
                        <Dropdown overlay={
                            <Menu>
                                <Menu.Item icon={<AreaChartOutlined />} onClick={() => loadServerMetrics(serverMonitor)}>
                                    性能指标
                                </Menu.Item>
                                <Menu.Item icon={<BranchesOutlined />} onClick={() => loadServerTracing(serverMonitor)}>
                                    链路追踪
                                </Menu.Item>
                            </Menu>
                        }>
                            <a>
                                <SettingOutlined />
                            </a>
                        </Dropdown>
                    }
                    headerBordered={true}
                >
                    <ProCard>
                        <Statistic title="ip" value={serverMonitor.host} />
                    </ProCard>
                    <ProCard>
                        <Statistic title="提供者端口" value={serverMonitor.port} />
                    </ProCard>
                    <ProCard>
                        <Statistic title="健康状况" formatter={(value) => <TagCom icon={healthStatus[value].component} color={healthStatus[value].color}>{healthStatus[value].text}</TagCom>} value={serverMonitor.health}/>
                    </ProCard>
                    <ProCard>
                        <Statistic title="最后更新时间" value={serverMonitor.lastUpdateTime}/>
                    </ProCard>
                </ProCard.Group>
            ))
        });
        return menus;
    }

    useEffect(() => {
        if (isLoading && isInit) {
            init();
        }
        return () => {
            clearInterval(asyncQueryInterval);
        }
    })

    return (
        <Spin spinning={isLoading}>
            <PageHeaderWrapper>
                <div style={{marginBottom: '20px'}}>
                    <ProCard>
                        <Input.Search 
                            value={queryKey} 
                            placeholder="请输入服务key"
                            onSearch={(value, event) => {
                                event?.stopPropagation();
                                const params: ServerMonitorQueryParams = {
                                    serverKey: value
                                }
                                queryMonitorServer(params);
                            }} 
                            onChange={(event) => {
                                setQueryKey(event.target.value);
                            }}
                        />
                    </ProCard>
                </div>
                <div className={styles.monitor}>
                    {renderServer()}
                </div>
            </PageHeaderWrapper>
            <Modal
                visible={isShowMetrics}
                footer={null}
                onCancel={() => {
                    setIsShowMetrics(false)
                    clearInterval(metricsTimer);
                    setActiveMetrics(undefined);
                    initCardLoading();
                }}
                width={1200}
                title={`[性能指标-${activeServerMonitor?.providerKey? activeServerMonitor.providerKey : activeServerMonitor?.consumerKey}]`}
            >
                    <ProCard style={{ marginTop: 4 }} gutter={8}>
                        <ProCard title={`Uptime`} colSpan={{ xs: 2, sm: 4, md: 6, lg: 8, xl: 24 }} layout="center" bordered headerBordered={true} loading={processCardLoading(METRIC.UPTIME_METRICS)}>
                            {
                                loadServerMetricsByKey(METRIC.UPTIME_METRICS)
                            }
                        </ProCard>
                    </ProCard>
                    <ProCard style={{ marginTop: 4 }} gutter={8}>
                        <ProCard title={`Jvm Thread`} colSpan={{ xs: 2, sm: 4, md: 6, lg: 8, xl: 24 }} layout="center" bordered headerBordered={true} loading={processCardLoading(METRIC.JVM_THREAD_METRICS)}>
                            {
                                loadServerMetricsByKey(METRIC.JVM_THREAD_METRICS)
                            }
                        </ProCard>
                    </ProCard>
                    <ProCard style={{ marginTop: 4 }} gutter={8}>
                        <ProCard title={`Class Loader`} colSpan={{ xs: 2, sm: 4, md: 6, lg: 8, xl: 12 }} layout="center" bordered headerBordered={true} loading={processCardLoading(METRIC.CLASS_LOADER_METRICS)}>
                            {
                                loadServerMetricsByKey(METRIC.CLASS_LOADER_METRICS)
                            }
                        </ProCard>
                        <ProCard title={`Processor`} colSpan={{ xs: 2, sm: 4, md: 6, lg: 8, xl: 12 }} layout="center" bordered headerBordered={true} loading={processCardLoading(METRIC.PROCESSOR_METRICS)}>
                            {
                                loadServerMetricsByKey(METRIC.PROCESSOR_METRICS)
                            }
                        </ProCard>
                    </ProCard>
                    <ProCard style={{ marginTop: 4 }} gutter={8}>
                        <ProCard title={`Jvm Heap Memory`} colSpan={{ xs: 2, sm: 4, md: 6, lg: 8, xl: 24 }} layout="center" bordered headerBordered={true} loading={processCardLoading(METRIC.JVM_HEAP_MEMORY_METRICS)}>
                            {
                                loadServerMetricsByKey(METRIC.JVM_HEAP_MEMORY_METRICS)
                            }
                        </ProCard>
                    </ProCard>
                    <ProCard style={{ marginTop: 4 }} gutter={8}>
                        <ProCard title={`Jvm Non Heap Memory`} colSpan={{ xs: 2, sm: 4, md: 6, lg: 8, xl: 24 }} layout="center" bordered headerBordered={true} loading={processCardLoading(METRIC.JVM_NON_HEAP_MEMORY_METRICS)}>
                            {
                                loadServerMetricsByKey(METRIC.JVM_NON_HEAP_MEMORY_METRICS)
                            }
                        </ProCard>
                    </ProCard>
                    <ProCard style={{ marginTop: 4 }} gutter={8}>
                        <ProCard title={`JVM GC`} colSpan={{ xs: 2, sm: 4, md: 6, lg: 8, xl: 24 }} layout="center" bordered headerBordered={true} loading={processCardLoading(METRIC.JVM_GC_METRICS)}>
                            {
                                loadServerMetricsByKey(METRIC.JVM_GC_METRICS)
                            }
                        </ProCard>
                    </ProCard>
                    <ProCard style={{ marginTop: 4 }} gutter={8}>
                        <ProCard title={`Disk Space`} colSpan={{ xs: 2, sm: 4, md: 6, lg: 8, xl: 24 }} layout="center" bordered headerBordered={true} loading={processCardLoading(METRIC.DISK_SPACE_METRICS)}>
                            {
                                loadServerMetricsByKey(METRIC.DISK_SPACE_METRICS)
                            }
                        </ProCard>

                    </ProCard>
            </Modal>

            <Modal
                visible={isShowTracing}
                footer={null}
                onCancel={() => {
                    setIsShowTracing(false);
                    traceRef.current.cancel();
                }}
                width={1400}
                title={`[链路追踪-${activeServerMonitor?.consumerKey? activeServerMonitor.consumerKey : activeServerMonitor?.providerKey}]`}
            >
                <ServerTracing serverMonitor={activeServerMonitor} dispatch={props.dispatch} traceRef={traceRef} />
            </Modal>
        </Spin>
    )
}

export default connect(({ isLoading, serverModels, queryParams, tracingModel }: MonitorState) => ({
    isLoading,
    serverModels,
    queryParams,
    tracingModel
}))(Monitor);
