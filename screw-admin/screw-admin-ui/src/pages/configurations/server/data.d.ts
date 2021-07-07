import DataSource from '@/pages/configurations/datasource/data'
import AppConfig from '@/pages/configurations/config/data';

export interface AppServer {
    id?: string;
    serverCode?: string;
    serverName?: string;
    serverIp?: string;
    serverPort?: number;
    serverState: number;
    serverVersion?: string;
    dataSourceId?: string;
    serverId?: string;
    serverName?: string;
    createBy?: string;
    createTime?: string;
    updateBy?: string;
    updateTime?: string;
    deleted?: string;
    version?: number;
    datasource?: DataSource[];
    AppConfig?: AppConfig[]
}

export interface AddAppServer {
    serverName: string;
    serverIp: string;
    serverPort: number;
    serverVersion: number;
    systemId: string;
    systemName: string;
    dataSourceId: string;
    appConfig: AppConfig[]
}