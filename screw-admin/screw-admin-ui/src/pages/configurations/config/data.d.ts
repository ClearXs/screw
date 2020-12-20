import { PageParams } from '@/models/page';
import { AppServer } from '@/pages/configurations/server/data'

export interface AppConfig {
    id?: string;
    configName?: string;
    configKey?: string;
    configJson?: string;
    roleId?: number;
    serverId?: number;
    configVersionId?: string;
    createBy?: string;
    createTime?: string;
    updateBy?: string;
    updateTime?: string;
    deleted?: string;
    version?: number;
    isVersionModal: boolean;
    isConfigModal: boolean
    appConfigVersion?: AppConfigVersion[];
    appServer?: AppServer;
}

export interface AppConfigQueryParams extends PageParams {
    configName?: string | undefined;
    serverId: string | undefined;    
}

export interface AppConfigVersion {
    id?: string;
    configVersion?: string;
    configVersionStatus?: string;
    createBy?: string;
    createTime?: string;
    updateBy?: string;
    updateTime?: string;
    deleted?: string;
    version?: number;
    isModal: boolean;
    configData?: AppConfigData[];
    configId: string;
}

export interface AppConfigVersionQueryParams extends PageParams {
    configVersionStatus?: string;
    configId: string | undefined;
}

export interface AppConfigData {
    id?: string;
    configDataKey?: string;
    configDataType?: string | undefined;
    configDataValue?: string;
    configDataStoreState?: string | undefined;
    configVersionId?: string;
    remark?: string;
    createBy?: string;
    createTime?: string;
    updateBy?: string;
    updateTime?: string;
    deleted?: string;string
    version?: number;
    isDraw: boolean;
    configVersion: AppConfigVersion;
}

export interface AppConfigDataQueryParams extends PageParams {
    configVersionId: string | undefined;
}