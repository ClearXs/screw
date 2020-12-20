// https://umijs.org/config/
import { defineConfig } from 'umi';
import defaultSettings from './defaultSettings';
import proxy from './proxy';
const { REACT_APP_ENV } = process.env;
export default defineConfig({
  hash: true,
  antd: {},
  dva: {
    hmr: true,
  },
  // locale: {
  //   // default zh-CN
  //   default: 'zh-CN',
  //   // default true, when it is true, will use `navigator.language` overwrite default
  //   antd: true,
  //   baseNavigator: true,
  // },
  dynamicImport: {
    loading: '@/components/PageLoading/index',
  },
  targets: {
    ie: 11,
  },
  // umi routes: https://umijs.org/docs/routing
  routes: [
    {
      path: '/',
      component: '../layouts/BasicLayout',
      routes: [
        {
          path: '/',
          redirect: '/welcome',
          component: '@/pages/Welcome',
        },
        {
          name: '配置中心',
          icon: 'setting',
          path: '/config',
          routes: [
            {
              name: '应用服务管理',
              icon: 'setting',
              path: '/config/server',
              component: '@/pages/configurations/server',
            },
            {
              name: '配置管理',
              icon: 'setting',
              path: '/config/configManagement',
              component: '@/pages/configurations/config',
            },
            {
              name: '数据源管理',
              icon: 'setting',
              path: '/config/datasource',
              component: '@/pages/configurations/datasource',
            },
          ],
        },
        {
          component: '@/pages/404',
        },
      ],
    },
  ],
  // Theme for antd: https://ant.design/docs/react/customize-theme-cn
  theme: {
    // ...darkTheme,
    'primary-color': defaultSettings.primaryColor,
  },
  // @ts-ignore
  title: false,
  ignoreMomentLocale: false,
  proxy: proxy[REACT_APP_ENV || 'dev'],
  manifest: {
    basePath: '/',
  },
});
