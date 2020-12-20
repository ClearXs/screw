import { MenuDataItem, Settings as ProSettings } from '@ant-design/pro-layout';
import { GlobalModelState } from './global';
import { UserModelState } from './user';
import { StateType } from './login';
import { AppServerState } from '@/pages/configurations/server/models/index';
import { DataSourceState } from '@/pages/configurations/datasource/models/index';
import { AppConfigState } from '@/pages/configurations/config/models/config';
import { AppConfigVersionState } from '@/pages/configurations/config/version/models/version';
import { AppConfigDataState } from '@/pages/configurations/config/datas/models/data';


export { GlobalModelState, UserModelState };

export interface Loading {
  global: boolean;
  effects: { [key: string]: boolean | undefined };
  models: {
    global?: boolean;
    menu?: boolean;
    setting?: boolean;
    user?: boolean;
    login?: boolean;
  };
}

export interface ConnectState {
  global: GlobalModelState;
  loading: Loading;
  settings: ProSettings;
  user: UserModelState;
  login: StateType;
  server: AppServerState;
  dataSource: DataSourceState;
  appConfig: AppConfigState;
  appConfigData: AppConfigDataState;
  appConfigVersions: AppConfigVersionState;
}

export interface Route extends MenuDataItem {
  routes?: Route[];
}
