export const HikariCP = 
    "spring.datasource.autoCommit=true\n" +
    "spring.datasource.connectionTimeout=30000\n" +
    "spring.datasource.idleTimeout=600000\n" +
    "spring.datasource.maxLifetime=1800000\n" +
    "spring.datasource.connectionTestQuery=null\n" +
    "spring.datasource.minimumIdle=-1\n" +
    "spring.datasource.maximumPoolSize=-1\n" +
    "spring.datasource.metricRegistry=null\n" +
    "spring.datasource.healthCheckRegistry=null\n" +
    "spring.datasource.poolName=ool-1\n" +
    "spring.datasource.initializationFailTimeout=1\n" +
    "spring.datasource.isolateInternalQueries=false\n" +
    "spring.datasource.allowPoolSuspension=false\n" +
    "spring.datasource.readOnly=false\n" +
    "spring.datasource.registerMbeans=false\n" +
    "spring.datasource.catalog=null\n" +
    "spring.datasource.connectionInitSql=null\n" +
    "spring.datasource.driverClassName=null\n" +
    "spring.datasource.transactionIsolation=null\n" +
    "spring.datasource.validationTimeout=5000\n" +
    "spring.datasource.leakDetectionThreshold=0\n" +
    "spring.datasource.dataSource=null\n" +
    "spring.datasource.schema=null\n" +
    "spring.datasource.threadFactory=null\n" + 
    "spring.datasource.scheduledExecutor=null\n";

export interface ConnectionPool {
    key: number;
    name: string;
    value: string;
    description: string;
}

export const connectionPoolColumn: ColumnsType<ConnectionPool> = [
    {
        title: '名称',
        dataIndex: 'name',
    }, 
    {
        title: '默认值',
        dataIndex: 'value'
    },
    {
        title: '描述',
        dataIndex: 'description'
    }
]

export const HikariCPData: ConnectionPool[] = [
    {
        key: 1,
        name: 'autoCommit',
        value: 'true',
        description: '自动提交从池中返回的连接'
    },
    {
        key: 2,
        name: 'connectionTimeout',
        value: '30000',
        description: '等待来自池的连接的最大毫秒数'
    },
    {
        key: 3,
        name: 'idleTimeout',
        value: '600000',
        description: '连接允许在池中闲置的最长时间'
    },
    {
        key: 4,
        name: 'connectionTestQuery',
        value: 'null',
        description: '如果您的驱动程序支持JDBC4，我们强烈建议您不要设置此属性'
    },
    {
        key: 5,
        name: 'minimumIdle',
        value: '-1',
        description: '池中维护的最小空闲连接数'
    },
    {
        key: 6,
        name: 'maximumPoolSize',
        value: '-1',
        description: '池中最大连接数，包括闲置和使用中的连接'
    },
    {
        key: 7,
        name: 'metricRegistry',
        value: 'null',
        description: '该属性允许您指定一个 Codahale / Dropwizard MetricRegistry 的实例，供池使用以记录各种指标'
    },
    {
        key: 8,
        name: 'healthCheckRegistry',
        value: 'null',
        description: '该属性允许您指定池使用的Codahale / Dropwizard HealthCheckRegistry的实例来报告当前健康信息'
    },
    {
        key: 9,
        name: 'poolName',
        value: 'null',
        description: '连接池的用户定义名称，主要出现在日志记录和JMX管理控制台中以识别池和池配置'
    },
    {
        key: 10,
        name: 'initializationFailTimeout',
        value: '1',
        description: '如果池无法成功初始化连接，则此属性控制池是否将 fail fast'
    },
    {
        key: 11,
        name: 'isolateInternalQueries',
        value: 'false',
        description: '是否在其自己的事务中隔离内部池查询，例如连接活动测试'
    },
    {
        key: 12,
        name: 'allowPoolSuspension',
        value: 'false',
        description: '控制池是否可以通过JMX暂停和恢复'
    },
    {
        key: 13,
        name: 'readOnly',
        value: 'false',
        description: '从池中获取的连接是否默认处于只读模式'
    },
    {
        key: 14,
        name: 'registerMbeans',
        value: 'false',
        description: '是否注册JMX管理Bean（MBeans）'
    },
    {
        key: 15,
        name: 'catalog',
        value: 'null',
        description: '为支持 catalog 概念的数据库设置默认 catalog'
    },
    {
        key: 16,
        name: 'connectionInitSql',
        value: 'null',
        description: '该属性设置一个SQL语句，在将每个新连接创建后，将其添加到池中之前执行该语句。'
    },
    {
        key: 17,
        name: 'driverClassName',
        value: 'null',
        description: 'HikariCP将尝试通过仅基于jdbcUrl的DriverManager解析驱动程序，但对于一些较旧的驱动程序，还必须指定driverClassName'
    },
    {
        key: 18,
        name: 'transactionIsolation',
        value: 'null',
        description: '控制从池返回的连接的默认事务隔离级别'
    },
    {
        key: 19,
        name: 'validationTimeout',
        value: '5000',
        description: '连接将被测试活动的最大时间量'
    },
    {
        key: 20,
        name: 'leakDetectionThreshold',
        value: '0',
        description: '记录消息之前连接可能离开池的时间量，表示可能的连接泄漏'
    },
    {
        key: 21,
        name: 'dataSource',
        value: 'null',
        description: '这个属性允许你直接设置数据源的实例被池包装，而不是让HikariCP通过反射来构造它'
    },
    {
        key: 22,
        name: 'schema',
        value: 'null',
        description: '该属性为支持模式概念的数据库设置默认模式'
    },
    {
        key: 23,
        name: 'threadFactory',
        value: 'null',
        description: '此属性允许您设置将用于创建池使用的所有线程的java.util.concurrent.ThreadFactory的实例。'
    },
    {
        key: 24,
        name: 'scheduledExecutor',
        value: 'null',
        description: '此属性允许您设置将用于各种内部计划任务的java.util.concurrent.ScheduledExecutorService实例'
    },
] 

export const c3p0 = 
    "spring.datasource.initialPoolSize=3\n" + 
    "spring.datasource.maxPoolSize=15\n" + 
    "spring.datasource.minPoolSize=3\n" + 
    "spring.datasource.acquireIncrement=3\n" +
    "spring.datasource.maxIdleTime=0\n" +
    "spring.datasource.maxConnectorAge=0\n" +
    "spring.datasource.maxIdleTimeExcessConnection=0\n" +
    "spring.datasource.automaticTestTable=null\n" +
    "spring.datasource.connectionTesterClassName=com.mchange.v2.c3p0.impl.DefaultConnectionTester\n" +
    "spring.datasource.idleConnectionTestPeriod=0\n" +
    "spring.datasource.preferredTestQuery=null\n" +
    "spring.datasource.testConnectionOnCheckin=false\n" +
    "spring.datasource.testConnectionOnCheckout=false\n" +
    "spring.datasource.maxStatements=0\n" +
    "spring.datasource.maxStatementsPerConnection=0\n" +
    "spring.datasource.statementCacheNumDeferredCloseThreads=0\n" +
    "spring.datasource.acquireRetryAttempts=30\n" +
    "spring.datasource.acquireRetryDelay=1000\n" +
    "spring.datasource.breakAfterAcquireFailure=false\n" +
    "spring.datasource.autoCommitOnClose=false\n" +
    "spring.datasource.forceIgnoreUnresolvedTransactions=false\n" +
    "spring.datasource.checkoutTimeout=0\n" +
    "spring.datasource.factoryClassLocation=0\n" +
    "spring.datasource.numHelperThreads=3\n";

export const c3p0Data: ConnectionPool[] = [
    {
        key: 1,
        name: 'initialPoolSize',
        value: '3',
        description: '连接池初始化时创建的连接数'
    },
    {
        key: 2,
        name: 'maxPoolSize',
        value: '15',
        description: '连接池中拥有的最大连接数，如果获得新连接时会使连接总数超过这个值则不会再获取新连接，而是等待其他连接释放，所以这个值有可能会设计地很大'
    },
    {
        key: 3,
        name: 'minPoolSize',
        value: '3',
        description: '连接池保持的最小连接数，后面的maxIdleTimeExcessConnections跟这个配合使用来减轻连接池的负载'
    },
    {
        key: 4,
        name: 'acquireIncrement',
        value: '3',
        description: '连接池在无空闲连接可用时一次性创建的新数据库连接数'
    },
    {
        key: 5,
        name: 'maxIdleTime',
        value: '0',
        description: '连接的最大空闲时间，如果超过这个时间，某个数据库连接还没有被使用，则会断开掉这个连接如果为0，则永远不会断开连接'
    },
    {
        key: 6,
        name: 'maxConnectorAge',
        value: '0',
        description: '连接的最大绝对年龄，单位是秒，0表示绝对年龄无限大'
    },
    {
        key: 7,
        name: 'maxIdleTimeExcessConnection',
        value: '0',
        description: '单位秒，为了减轻连接池的负载，当连接池经过数据访问高峰创建了很多连接，但是后面连接池不需要维护这么多连接，必须小于maxIdleTime.配置不为0，则将连接池的数量保持到minPoolSize'
    },
    {
        key: 8,
        name: 'automaticTestTable',
        value: 'null',
        description: '如果不为null，c3p0将生成指定名称的空表，使用该表来测试连接'
    },
    {
        key: 9,
        name: 'connectionTesterClassName',
        value: 'com.mchange.v2.c3p0.impl.DefaultConnectionTester',
        description: '-通过实现ConnectionTester或QueryConnectionTester的类来测试连接。类名需制定全路径。'
    },
    {
        key: 10,
        name: 'idleConnectionTestPeriod',
        value: '0',
        description: '每个几秒检查所有连接池中的空闲连接'
    },
    {
        key: 11,
        name: 'preferredTestQuery',
        value: 'null',
        description: '定义所有连接测试都执行的测试语句。在使用连接测试的情况下这个一显著提高测试速度。注意： 测试的表必须在初始数据源的时候就存在'
    },
    {
        key: 12,
        name: 'testConnectionOnCheckin',
        value: 'false',
        description: '如果设为true那么在取得连接的同时将校验连接的有效性'
    },
    {
        key: 13,
        name: 'testConnectionOnCheckout',
        value: 'false',
        description: '如果为true，在连接释放的同事将校验连接的有效性。'
    },
    {
        key: 14,
        name: 'maxStatements',
        value: '0',
        description: 'JDBC的标准参数，用以控制数据源内加载d的PreparedStatements数量'
    },
    {
        key: 15,
        name: 'maxStatementsPerConnection',
        value: '0',
        description: 'maxStatementsPerConnection定义了连接池内单个连接所拥有的最大缓存statements数'
    },
    {
        key: 16,
        name: 'statementCacheNumDeferredCloseThreads',
        value: '0',
        description: '如果大于零，则语句池将延迟物理close()缓存语句直到其父连接未被任何客户端使用，或者在其内部（例如在测试中）由池本身使用。'
    },
    {
        key: 17,
        name: 'acquireRetryAttempts',
        value: '30',
        description: '定义在从数据库获取新连接失败后重复尝试的次数'
    },
    {
        key: 18,
        name: 'acquireRetryDelay',
        value: '1000',
        description: '两次连接间隔时间，单位毫秒'
    },
    {
        key: 19,
        name: 'breakAfterAcquireFailure',
        value: 'false',
        description: '获取连接失败将会引起所有等待连接池来获取连接的线程抛出异常。但是数据源仍有效 保留，并在下次调用getConnection()的时候继续尝试获取连接。如果设为true，那么在尝试 获取连接失败后该数据源将申明已断开并永久关闭'
    },
    {
        key: 20,
        name: 'autoCommitOnClose',
        value: 'false',
        description: '连接关闭时默认将所有未提交的操作回滚。如果为true，则未提交设置为待提交而不是回滚。'
    },
    {
        key: 21,
        name: 'forceIgnoreUnresolvedTransactions',
        value: '1000',
        description: '官方文档建议这个不要设置为true'
    },
    {
        key: 22,
        name: 'checkoutTimeout',
        value: '0',
        description: '当连接池用完时客户端调用getConnection()后等待获取新连接的时间，超时后将抛出SQLException,如设为0则无限期等待。单位毫秒。'
    },
    {
        key: 23,
        name: 'factoryClassLocation',
        value: '0',
        description: '指定c3p0 libraries的路径，如果（通常都是这样）在本地即可获得那么无需设置，默认null即可'
    },
    {
        key: 24,
        name: 'numHelperThreads',
        value: '3',
        description: 'c3p0是异步操作的，缓慢的JDBC操作通过帮助进程完成。扩展这些操作可以有效的提升性能通过多线程实现多个操作同时被执行'
    },
]
export const druid = 
    "spring.datasource.initialSize=10\n" + 
    "spring.datasource.maxActive=60\n" + 
    "spring.datasource.minIdle=10\n" + 
    "spring.datasource.maxWait=60\n" + 
    "spring.datasource.minEvictableIdleTimeMillis=300000\n" + 
    "spring.datasource.timeBetweenEvictionRunsMillis=60000\n" + 
    "spring.datasource.testWhileIdle=true\n" + 
    "spring.datasource.validationQuery=select 1\n" + 
    "spring.datasource.testOnBorrow=false\n" + 
    "spring.datasource.testOnReturn=false\n" + 
    "spring.datasource.poolPreparedStatements=true\n" + 
    "spring.datasource.maxOpenPreparedStatements=20\n";

export const druidData: ConnectionPool[] = [
    {
        key: 1,
        name: 'initialSize',
        value: '0',
        description: '初始化时建立物理连接的个数。初始化发生在显示调用init方法，或者第一次getConnection时'
    },
    {
        key: 2,
        name: 'maxActive',
        value: '8',
        description: '最大连接池数量'
    },
    {
        key: 3,
        name: 'maxIdle',
        value: '8',
        description: '已经不再使用，配置了也没效果'
    },
    {
        key: 4,
        name: 'minIdle',
        value: '',
        description: '最小连接池数量'
    },
    {
        key: 5,
        name: 'maxWait',
        value: '',
        description: '获取连接时最大等待时间，单位毫秒。配置了maxWait之后，缺省启用公平锁，并发效率会有所下降，如果需要可以通过配置useUnfairLock属性为true使用非公平锁。'
    },
    {
        key: 6,
        name: 'poolPreparedStatements',
        value: 'false',
        description: '是否缓存preparedStatement，也就是PSCache。PSCache对支持游标的数据库性能提升巨大，比如说oracle。在mysql下建议关闭。'
    },
    {
        key: 7,
        name: 'maxOpenPreparedStatements',
        value: '-1',
        description: '要启用PSCache，必须配置大于0，当大于0时，poolPreparedStatements自动触发修改为true。在Druid中，不会存在Oracle下PSCache占用内存过多的问题，可以把这个数值配置大一些，比如说100'
    },
    {
        key: 8,
        name: 'validationQuery',
        value: '',
        description: '用来检测连接是否有效的sql，要求是一个查询语句。如果validationQuery为null，testOnBorrow、testOnReturn、testWhileIdle都不会其作用。'
    },
    {
        key: 9,
        name: 'testOnBorrow',
        value: 'true',
        description: '申请连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能。'
    },
    {
        key: 10,
        name: 'testOnReturn',
        value: 'false',
        description: '归还连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能'
    },
    {
        key: 11,
        name: 'testWhileIdle',
        value: 'false',
        description: '建议配置为true，不影响性能，并且保证安全性。申请连接的时候检测，如果空闲时间大于timeBetweenEvictionRunsMillis，执行validationQuery检测连接是否有效。'
    },
    {
        key: 12,
        name: 'timeBetweenEvictionRunsMillis',
        value: 'false',
        description: '1) Destroy线程会检测连接的间隔时间2) testWhileIdle的判断依据，详细看testWhileIdle属性的说明'
    },
    {
        key: 13,
        name: 'numTestsPerEvictionRun',
        value: 'false',
        description: '不再使用，一个DruidDataSource只支持一个EvictionRun'
    },
    {
        key: 14,
        name: 'minEvictableIdleTimeMillis',
        value: '',
        description: ''
    },
    {
        key: 15,
        name: 'connectionInitSqls',
        value: '',
        description: '物理连接初始化的时候执行的sql'
    },
    {
        key: 16,
        name: 'exceptionSorter',
        value: '根据dbType自动识别',
        description: '当数据库抛出一些不可恢复的异常时，抛弃连接'
    },
    {
        key: 17,
        name: 'filters',
        value: '',
        description: '属性类型是字符串，通过别名的方式配置扩展插件，常用的插件有： 监控统计用的filter:stat日志用的filter:log4j防御sql注入的filter:wall'
    },
    {
        key: 18,
        name: 'proxyFilters',
        value: '',
        description: '类型是List<com.alibaba.druid.filter.Filter>，如果同时配置了filters和proxyFilters，是组合关系，并非替换关系'
    },
]
export const dbcp = 
    "spring.datasource.initialSize=10\n" + 
    "spring.datasource.maxActive=80\n" + 
    "spring.datasource.minIdle=10\n" + 
    "spring.datasource.maxIdle=60\n" + 
    "spring.datasource.maxWait=3000\n" + 
    "spring.datasource.validationQuery =SELECT 1\n" + 
    "spring.datasource.testWhileIdle=true\n" + 
    "spring.datasource.testOnBorrow=false\n" + 
    "spring.datasource.timeBetweenEvictionRunsMillis=30000\n" + 
    "spring.datasource.minEvictableIdleTimeMillis=180000\n" + 
    "spring.datasource.numTestsPerEvictionRun=3\n" + 
    "spring.datasource.removeAbandoned=true\n" + 
    "spring.datasource.removeAbandonedTimeout=180\n"; 

export const dbcpData: ConnectionPool[] = [
    {
        key: 1,
        name: 'maxWait',
        value: '3000',
        description: '从池中取连接的最大等待时间，单位ms.'
    },
    {
        key: 2,
        name: 'initialSize',
        value: '10',
        description: '初始化连接'
    },
    {
        key: 3,
        name: 'maxIdle',
        value: '60',
        description: '最大空闲连接'
    },
    {
        key: 4,
        name: 'minIdle',
        value: '10',
        description: '最小空闲连接'
    },
    {
        key: 5,
        name: 'maxActive',
        value: '80',
        description: '最大活动连接'
    },
    {
        key: 6,
        name: 'maxActive',
        value: '80',
        description: '最大活动连接'
    },
    {
        key: 7,
        name: 'validationQuery',
        value: 'SELECT 1',
        description: '验证使用的SQL语句'
    },
    {
        key: 8,
        name: 'testWhileIdle',
        value: 'true',
        description: '指明连接是否被空闲连接回收器(如果有)进行检验.如果检测失败,则连接将被从池中去除.'
    },
    {
        key: 9,
        name: 'testOnBorrow',
        value: 'false',
        description: '借出连接时不要测试，否则很影响性能'
    },
    {
        key: 10,
        name: 'timeBetweenEvictionRunsMillis',
        value: '30000',
        description: '每30秒运行一次空闲连接回收器'
    },
    {
        key: 11,
        name: 'minEvictableIdleTimeMillis',
        value: '1800000',
        description: '池中的连接空闲30分钟后被回收'
    },
    {
        key: 12,
        name: 'numTestsPerEvictionRun',
        value: '10',
        description: '在每次空闲连接回收器线程(如果有)运行时检查的连接数量'
    },
    {
        key: 13,
        name: 'removeAbandoned',
        value: 'true',
        description: '连接泄漏回收参数，当可用连接数少于3个时才执行'
    },
    {
        key: 14,
        name: 'removeAbandonedTimeout',
        value: '180',
        description: '连接泄漏回收参数，180秒，泄露的连接可以被删除的超时值'
    },
]