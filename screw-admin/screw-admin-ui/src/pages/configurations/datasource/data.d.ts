export interface DataSource {
    id?: string;
    datasourceName?: string;
    datasourceType?: string;
    datasourceConnectName?: string;
    datasourceIp?: string;
    datasourcePort?: string;
    datasourceUsername?: string;
    datasourcePassword?: string;
    datasourceConnectType?: string;
    datasourceConnectVariables?: string;
    createBy?: string;
    createTime?: string;
    updateBy?: string;
    updateTime?: string;
    deleted?: string;
    version?: number;
}