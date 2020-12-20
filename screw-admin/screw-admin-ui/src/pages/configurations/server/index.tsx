import { connect, Dispatch } from 'umi';
import React, { useState, useEffect, useRef } from 'react';
import { AppServerState, AppServerModelType } from '@/pages/configurations/server/models/index';
import { PageHeaderWrapper } from '@ant-design/pro-layout';
import ProTable, { ProColumns, ActionType } from '@ant-design/pro-table';
import { ModalForm, StepsForm, ProFormRadio } from '@ant-design/pro-form';
import { PlusOutlined, ExclamationCircleOutlined } from '@ant-design/icons';
import { Spin, Button, message, Tag, Modal } from 'antd';
import { AppServer, AddAppServer } from './data';
import { DataSource } from '@/pages/configurations/datasource/data'
import ServerForm from './components/ServerForm';
import DataSourceForm from './components/DataSourceForm';
import ServerConfigTable from './components/ServerConfigTable';
import _ from 'lodash';

interface ServerProps {
  dispatch: Dispatch;
  appServerModel: AppServerModelType;
}
const { confirm } = Modal;

const Server: React.FC<ServerProps> = (props) => {

  const [isLoading, setIsLoading] = useState<boolean>(true);

  const [appServers, setAppServers] = useState<AppServer[]>([]);

  const [stepVisible, setStepVisible] = useState<boolean>(false);

  const [dataSources, setDataSources] = useState<DataSource[]>();

  const [defaultAppServer, setDefaultAppServer] = useState<Map<string, AppServer[]>>();

  const actionRef = useRef<ActionType>();

  const serverConfigRef = useRef<any>();

  const [stepCurrent, setStepCurrent] = useState<number>(0);

  const serverStatusMap = {
    0: {
      color: '',
      text: '关机',
    },
    1: {
      color: 'green',
      text: '开机',
    },
    2: {
      color: 'volcano',
      text: '异常',
    },
  };

  const columns: ProColumns<AppServer>[] = [
    {
      title: '序号',
      dataIndex: 'index',
      valueType: 'indexBorder',
    },
    {
      title: 'id',
      dataIndex: 'id',
      hideInTable: true,
    },
    {
      title: '应用名称',
      dataIndex: 'serverName',
    },
    {
      title: '应用code',
      dataIndex: 'serverCode',
    },
    {
      title: 'ip',
      dataIndex: 'serverIp',
      hideInSearch: false,
    },
    {
      title: '端口',
      dataIndex: 'serverPort',
      hideInSearch: false,
    },
    {
      title: '版本号',
      dataIndex: 'serverVersion',
      hideInSearch: false,
    },
    {
      title: '状态',
      dataIndex: 'serverState',
      hideInSearch: false,
      render: (_, record) => {
        return (
          <Tag color={serverStatusMap[record.serverState].color}>{serverStatusMap[record.serverState].text}</Tag>
        )
      }
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      hideInSearch: false,
    },
    {
      title: '更新时间',
      dataIndex: 'updateTime',
      hideInSearch: false,
    },
    {
      title: '数据源名称',
      dataIndex: 'datasource',
      search: false,
      render: (record: any) => {
        if (_.isEmpty(record) || record === '-') {
          return '未选择数据源'
        } else {
          return record.datasourceName;
        }
      }
    },
    {
      title: '操作',
      key: 'option',
      valueType: 'option',
      render: (_, record) => [
        <a key="details" onClick={(event) => {
          event.stopPropagation();
          props['history'].push({
            pathname: '/config/configManagement',
            state: {
              serverId: record.id
            }
          });
        }}>明细</a>,
        <ModalForm
          onFinish={async (value: any) => {
            value.id = record.id;
            value.version = record.version;
            await fromSubmit(value, undefined);
            return true;
          }}
          title={`[${record.serverName}服务编辑]`}
          trigger={<a type="edit" onClick={(event) => {
            // 加载数据源 
            loadDataSources();
          }} >编辑</a>}
          key="details"
        >
          <ServerForm {...record} />
          {
            dataSources && (
              <ProFormRadio.Group
                name="dataSourceId"
                label="数据源"
                radioType="button"
                options={
                  dataSources.map((dataSource) => {
                    return {label: dataSource.datasourceName, value: dataSource.id}
                })
                }
              />
            )
          }

        </ModalForm>,
        <a onClick={() => {
          confirm({
            title: '确认删除当前服务?',
            icon: <ExclamationCircleOutlined />,
            okText: '确认',
            okType: 'danger',
            cancelText: '取消',
            onOk() {
              deleteServer(record);
            },
          });
        }}>
          删除
        </a>,
      ],
    },
  ];

  const handleQuery = () => {
      const { dispatch } = props;
      dispatch({
        type: 'server/queryAppServer',
        payload: {},
        callback: (server: AppServerState) => {
          setIsLoading(server.isLoading);
          setAppServers(server.appServers);
        },
      });
  }

  const fromSubmit = (params: any, handlerState: number | undefined) => {
    return new Promise<boolean>((resolve) => {
      setIsLoading(true);
      // 添加
      if (handlerState === 0) {
        // 组装成添加服务的对象
        const addAppServer: AddAppServer = {
          serverName: params.serverName,
          serverIp: params.serverIp,
          serverPort: params.serverPort,
          serverVersion: params.serverVersion,
          systemId: '',
          systemName: '',
          dataSourceId: params.dataSource,
          appConfig: params.configs
        }
        props.dispatch({
          type: 'server/addAppServer',
          payload: addAppServer,
          callback: ( result ) => {
            const success = result['success'];
            const msg = result['msg'];
            if (success) {
              message.success(msg);
              handleQuery();
              resolve(true);
            } else {
              message.error(msg);
              resolve(false);
            }
          }
        })
        // 编辑
      } else {
        props.dispatch({
          type: 'server/editAppServer',
          payload: params,
          callback: ( result ) => {
            const success = result['success'];
            const msg = result['msg'];
            if (success) {
              message.success(msg);
              handleQuery();
              resolve(true);
            } else {
              message.error(msg);
              resolve(false);
            }
          }
        })
      }
    })
  };

  const deleteServer = (value: any) => {
    return new Promise<boolean>((resolve) => {
      setIsLoading(true);
      props.dispatch({
        type: 'server/deleteAppServer',
        payload: value,
        callback: (result) => {
          const success = result['success'];
          const msg = result['msg'];
          if (success) {
            message.success(msg);
            handleQuery();
          } else {
            message.error(msg);
          }
          resolve(true);
        }
      });
    })
  }

  const isExistServer = (params: any): Promise<boolean> => {
    return new Promise((resolve, reject) => {
      const addAppServer: AddAppServer = {
        serverName: params.serverName,
        serverIp: params.serverIp,
        serverPort: params.serverPort,
        serverVersion: params.serverVersion,
        systemId: '',
        systemName: '',
        dataSourceId: '',
        appConfig: []
      }
      props.dispatch({
        type: 'server/isExist',
        payload: addAppServer,
        callback: (result) => {
          const success = result['success'];
          const msg = result['msg'];
          if (success) {
            handleQuery();
            resolve(true);
          } else {
            message.error(msg);
            resolve(false);
          }
        }
      });
    })
  }

  const loadDataSources = (): Promise<boolean> => {
    return new Promise((resolve, reject) => {
      props.dispatch({
        type: 'server/loadDataSource',
        payload: '',
        callback: (result) => {
          const success = result['success'];
          const msg = result['msg'];
          const data = result['data'];
          if (success) {
            setDataSources(data);
            resolve(true);
          } else {
            message.error(msg);
            resolve(false);
          }
        }
      })
    })
  }

  const testDataSource = (dataSourceId: string): Promise<boolean> => {
    return new Promise((resolve, reject) => {
      props.dispatch({
        type: 'server/testConnect',
        payload: dataSourceId,
        callback: (result) => {
          const success = result['success'];
          const data = result['data'];
          if (data && success) {
            resolve(true);
          } else {
            confirm({
              title: '当前数据库测试异常，确认是否走下一步?',
              icon: <ExclamationCircleOutlined />,
              okText: '确认',
              okType: 'danger',
              cancelText: '取消',
              onOk() {
                resolve(true);
              },
              onCancel() {
                resolve(false);
              }
            });
          }

        }
      })
    })
  }

  const queryDefaultServer = (): Promise<boolean> => {
    return new Promise((resolve, reject) => {
      props.dispatch({
        type: 'server/queryServerDirectory',
        payload: '',
        callback: (result) => {
          const success = result['success'];
          const msg = result['msg'];
          const data = result['data'];
          if (success) {
            setDefaultAppServer(data);
            resolve(true);
          } else {
            message.error(msg);
            resolve(false);
          }
        }
      })
    })
  }

  useEffect(() => {
    if (isLoading) {
      handleQuery();
    }
  });

  return (
    <Spin spinning={isLoading}>
      <PageHeaderWrapper>
        <ProTable<AppServer>
          actionRef={actionRef}
          columns={columns}
          dataSource={appServers}
          pagination={false}
          search={false}
          options={false}
          toolBarRender={() => [
            <Button type="primary" onClick={(event) => {
              event.stopPropagation();
              setStepVisible(true);
              setStepCurrent(0);
            }}>
                <PlusOutlined />
                新增应用服务
            </Button>
          ]}
        />
      </PageHeaderWrapper>
      <StepsForm
        current={stepCurrent}
        onCurrentChange={(current: number) => {
          setStepCurrent(current);
        }}
        stepsFormRender={(dom, submitter) => {
          return (
            <Modal
              title="应用服务"
              width={800}
              onCancel={() => setStepVisible(false)}
              visible={stepVisible}
              footer={submitter}
              destroyOnClose
            >
              {dom}
            </Modal>
          );
        }}
        onFinish={async (params: any) => {
          const configs = await serverConfigRef.current.getSelectedRows();
          params.configs = configs;
          const isFinish = await fromSubmit(params, 0);
          if (isFinish) {
            setStepVisible(false)
          }
          return isFinish;
        }}
      >
        <StepsForm.StepForm
          name="server"
          title="创建应用服务"
          onFinish={async (params: any) => {
            let isFinish: boolean = await isExistServer(params);
            if (isFinish) {
              // 加载数据源
              isFinish = await loadDataSources();
            }
            return isFinish;
          }}
        >
          <ServerForm />
        </StepsForm.StepForm>
        <StepsForm.StepForm
          name="datasource"
          title="选择数据源"
          onFinish={async (params: any) => {
            const confirmNext = (): Promise<boolean> => {
              return new Promise((resolve) => {
                confirm({
                  title: '当前数据库数据源为空，确认是否走下一步?',
                  icon: <ExclamationCircleOutlined />,
                  okText: '确认',
                  okType: 'danger',
                  cancelText: '取消',
                  onOk() {
                    resolve(true);
                  },
                  onCancel() {
                    resolve(false);
                  }
                });
              })
            }
            let isFinish: boolean;
            if (_.isEmpty(params)) {
              isFinish = await confirmNext();
            } else {
              // 测试连接
              isFinish = await testDataSource(params);
            }
            // 查找数据源
            if (isFinish) {
              isFinish = await queryDefaultServer();
            }
            return isFinish;
          }}
        >
          <DataSourceForm 
            dataSources={dataSources}
            history={props['history']}
          />
        </StepsForm.StepForm>
        <StepsForm.StepForm
          name="config"
          title="选择默认配置"
        >
          <ServerConfigTable 
            defaultServer={defaultAppServer} 
            serverConfigRef={serverConfigRef} 
            init={defaultAppServer ? true : false}
          />
        </StepsForm.StepForm>
      </StepsForm>
    </Spin>
  );
};

export default connect(({ isLoading, appServers }: AppServerState) => ({
  isLoading,
  appServers,
}))(Server);
