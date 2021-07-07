import { connect, Dispatch } from 'umi';
import React, { useState, useEffect, useRef } from 'react';
import { AppServerState, AppServerModelType } from '@/pages/configurations/server/models/index';
import { PageHeaderWrapper } from '@ant-design/pro-layout';
import ProTable, { ProColumns, ActionType } from '@ant-design/pro-table';
import { ModalForm, StepsForm, ProFormCheckbox } from '@ant-design/pro-form';
import { PlusOutlined, ExclamationCircleOutlined } from '@ant-design/icons';
import { Spin, Button, message, Modal, Tag, Popover } from 'antd';
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

  const [dataSourceLoading, setDataSourceLoading] = useState<boolean>(false);

  const [dataSourceTip, setDataSourceTip] = useState<string>();

  const [appServers, setAppServers] = useState<AppServer[]>([]);

  const [stepVisible, setStepVisible] = useState<boolean>(false);

  const [dataSources, setDataSources] = useState<DataSource[]>();

  const [defaultAppServer, setDefaultAppServer] = useState<Map<string, AppServer[]>>();

  const actionRef = useRef<ActionType>();

  const serverConfigRef = useRef<any>();

  const [stepCurrent, setStepCurrent] = useState<number>(0);

  const [selectedServer, setSelectedServer] = useState<AppServer[]>();

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
      sorter: (a, b) => a.serverName - b.serverName
    },
    {
      title: '应用code',
      dataIndex: 'serverCode',
    },
    // {
    //   title: 'ip',
    //   dataIndex: 'serverIp',
    //   hideInSearch: false,
    // },
    {
      title: '服务端口',
      dataIndex: 'serverPort',
      hideInSearch: false,
      tip: '该端口为服务启动端口',
      sorter: (a, b) => a.serverPort - b.serverPort
    },
    {
      title: '版本号',
      dataIndex: 'serverVersion',
      hideInSearch: false,
      sorter: (a, b) => a.serverVersion - b.serverVersion
    },
    // {
    //   title: '状态',
    //   dataIndex: 'serverState',
    //   hideInSearch: false,
    //   render: (_, record) => {
    //     return (
    //       <Tag color={serverStatusMap[record.serverState].color}>{serverStatusMap[record.serverState].text}</Tag>
    //     )
    //   }
    // },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      valueType: "dateTime",
      hideInSearch: false,
    },
    {
      title: '更新时间',
      dataIndex: 'updateTime',
      hideInSearch: false,
    },
    {
      title: '数据源',
      dataIndex: 'datasource',
      search: false,
      render: (datasource: any) => {
        if (_.isEmpty(datasource) || datasource === '-') {
          return '未选择数据源';
        } else {
          return datasource.map((o: DataSource, index: number) => {
            return (
              <div style={{marginTop: '5px'}}>
                <Popover title={o.datasourceName} content={(
                    <div>
                      <p>数据源名称：{o.datasourceName}</p>
                      <p>数据源类型：{o.datasourceType}</p>
                      <p>连接名称：{o.datasourceConnectName}</p>
                      <p>数据源ip：{o.datasourceIp}</p>
                      <p>数据源端口：{o.datasourcePort}</p>
                      <p>用户名：{o.datasourceUsername}</p>
                      <p>密码：{o.datasourcePassword}</p>
                  </div>
                )}>
                  <Tag style={{cursor: 'pointer'}}>
                    {o.datasourceName}
                  </Tag>
                </Popover>
              </div>
            )
          })
        }
      }
    },
    {
      title: '操作',
      key: 'option',
      valueType: 'option',
      render: (_, record) => [
        <a key="monitor" onClick={(event) => {
          event.stopPropagation();
          props['history'].push({
            pathname: '/monitor',
            state: {
              serverKey: record.serverCode
            }
          });
        }}>
          监控
        </a>,
        <a key="details" onClick={(event) => {
          event.stopPropagation();
          props['history'].push({
            pathname: '/config/configManagement',
            state: {
              serverId: record.id
            }
          });
        }}>配置明细</a>,
        <ModalForm
          onFinish={async (value: any) => {
            value.id = record.id;
            value.version = record.version;
            if (value.dataSourceId.length === 0) {
              value.dataSourceId = '';
            } else {
              if (value.dataSourceId.join) {
                value.dataSourceId = value.dataSourceId.join();
              }
            }
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
              <ProFormCheckbox.Group
                name="dataSourceId"
                label="数据源"
                initialValue={record.dataSourceId}
                options={
                    dataSources.map((dataSource) => {
                      return {label: dataSource.datasourceName + '-'  + '[' + dataSource.datasourceType + ']', value: dataSource.id}
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
              const records = new Array();
              records.push(record);
              deleteServer(records);
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
          dataSourceId: _.isEmpty(params.dataSource) ? '' : params.dataSource.map((o: DataSource) => o.id).join(),
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
        });
        // 编辑
      } else {
        params.serverIp = '';
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
        });
      }
    });
  };

  const deleteServer = (value: AppServer[]) => {
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
    });
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
      setDataSourceLoading(true);
      setDataSourceTip('测试连接中...')
      props.dispatch({
        type: 'server/testConnect',
        payload: dataSourceId,
        callback: (result) => {
          setDataSourceLoading(false);
          setDataSourceTip('');
          const success = result['success'];
          const data = result['data'];
          const msg = result['msg'];
          if (data && success) {
            resolve(true);
          } else {
            confirm({
              title: msg + '，确认是否走下一步?',
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
      });
    })
  }

  const queryDefaultServer = (): Promise<boolean> => {
    return new Promise((resolve, reject) => {
      props.dispatch({
        type: 'server/queryServerDirectory',
        payload: 'ALL',
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
      });
    });
  }

  useEffect(() => {
    if (isLoading) {
      handleQuery();
    }
  });

  return (
    <Spin spinning={isLoading} tip={''}>
      <PageHeaderWrapper>
        <ProTable<AppServer>
          actionRef={actionRef}
          columns={columns}
          dataSource={appServers}
          pagination={false}
          search={false}
          options={false}
          rowSelection={{
            onChange: (selectedKeys, selectedRow) => {
                setSelectedServer(selectedRow);
            },
          }}
          rowKey="id"
          tableAlertRender={false}
          tableAlertOptionRender={false}
          toolBarRender={() => [
            <div>
              <Button type="primary" style={{marginRight: '5px'}} onClick={(event) => {
                event.stopPropagation();
                setStepVisible(true);
                setStepCurrent(0);
              }}>
                  <PlusOutlined />
                  新增应用服务
              </Button>
              <Button type="default" onClick={(event) => {
                if (_.isEmpty(selectedServer)) {
                  message.warn('当前未选择数据!')
                } else {
                    confirm({
                      title: `确认删除${selectedServer.length}条当前应用?`,
                      icon: <ExclamationCircleOutlined />,
                      okText: '确认',
                      okType: 'danger',
                      cancelText: '取消',
                      onOk() {
                        deleteServer(selectedServer);
                      },
                  });
                }
              }}>批量删除</Button>
            </div>
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
                isFinish = await testDataSource(params.dataSource.map((o:DataSource) => o.id).join());
              }
              // 查找服务
              if (isFinish) {
                isFinish = await queryDefaultServer();
              }
              return isFinish;
            }}
          >
            <Spin spinning={dataSourceLoading} tip={dataSourceTip}>
              <DataSourceForm 
                  dataSources={dataSources}
                  history={props['history']}
                />
            </Spin>
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
