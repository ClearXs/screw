import { PageParams } from '@/models/page';

export interface ServerMonitorQueryParams {
    serverKey?: string | undefined;
    serverHost?: string | undefined;    
    serverPort?: number | undefined;
}

/**
 * 监控服务model
 */
export interface ServerMonitorModel  {
    providerKey?: string;
    providerRole?: string;
    consumerKey?: string;
    consumerRole?: string
    host?: string;
    port?: number;
    health?: string;
    lastUpdateTime?: string;
    metrics?: Map<string, Metrics[]>;
}

export interface Metrics {
    name: string;
    description: string;
    unit: string | undefined;
    measurements: Sample[];
    availableTags: Tag[];
}

export interface Sample {
    tagValueRepresentation: string;
    value: number;
}

interface Tag {
    key: string;
    value: string;
}

/**
 * 服务链路追踪数据
 */
export interface TracingModel {
    serverKey?: string;
    serverAddress?: UnresolvedAddress;
    tracers?: Map<string, TracingSpan>;
}

interface UnresolvedAddress {
    host: string;
    port: number;
}

export interface TracingSpan extends TraceItem {
    tags: Map<string, Object>;
    baggage: Map<string, string>;
    logs: TracingLog[];
    startTime: number;
    endTime: number;
    childSpans: TracingSpan[];
    context: TraceContext
}

export interface TraceItem  {
    spanId: string;
    operationName: string;
    costTime?: number;
} 

export interface TraceContext {
    spanId: string;
    tracerId: string;
}

export interface TracingLog {
    fields: Map<string, ?>
    logTime: number;
}