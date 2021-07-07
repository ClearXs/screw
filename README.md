screw（螺丝钉）是一款基于netty实现的rpc框架，它用于微服务治理。内部包含三个角色，注册中心、服务提供者、服务消费者，实现消费者端的负载均衡，可以基于同步、异步进行rpc请求。

- 注册中心：维护服务注册表、消费者订阅表。心跳机制判断提供者、消费者是否存活。
- 提供者：把自身发布的服务，ip:port发送给注册中心，等待消费者连接。与注册中心断线重连
- 消费者：把需要订阅的服务从注册中心获取，随后与提供者直连进行TCP通信。与注册中心断线重连

技术栈：netty、prostubff。

## 架构

![image-20201214095612429](C:\Users\jiangw\AppData\Roaming\Typora\typora-user-images\image-20201214095612429.png)



## 基本使用

### 引入jar包

```java
<!-- 作为provider -->
<dependency>
    <groupId>com.jw.screw</groupId>
    <artifactId>screw-provider</artifactId>
</dependency>
```

```java
<!-- 作为consumer -->
<dependency>
    <groupId>com.jw.screw</groupId>
    <artifactId>screw-consumer</artifactId>
</dependency>
```

### Provider与Consumer直连通信

创建service，在实现类或者非接口类加上`@ProviderService(publishService = DemoService.class)`注解

```java
interface DemoService {

    String hello(String msg);
}

@ProviderService(publishService = DemoService.class)
class DemoServiceImpl implements DemoService {

    @Override
    public String hello(String msg) {
        System.out.println(msg);
        return msg;
    }
}
```

创建provider，对provider进行配置，使用`nettyProvider.publishServices(new DemoServiceImpl());`发布service

```java
    @Test
    public void provider1() throws InterruptedException {
        NettyProviderConfig providerConfig = new NettyProviderConfig();
        providerConfig.setProviderKey("demo");
        providerConfig.setWeight(3);
        providerConfig.setPort(8081);
        NettyProvider nettyProvider = new NettyProvider(providerConfig);
        nettyProvider.publishServices(new DemoServiceImpl());
        nettyProvider.start();
        TimeUnit.SECONDS.sleep(100);
        nettyProvider.shutdown();
    }
```

创建consumer，使用`nettyConsumer.directService(metadata, "localhost", 8081)`直连provider。通过`ProxyObjectFactory`创建`Proxy`。`ServiceMetadata`是服务元数据，必须要指定服务的`providerKey`

```java
NettyConsumer nettyConsumer = new NettyConsumer();
ServiceMetadata metadata = new ServiceMetadata("demo");
nettyConsumer.directService(metadata, "localhost", 8081);
DemoService o = ProxyObjectFactory
    .factory()
    .consumer(nettyConsumer)
    .metadata(metadata)
    .isAsync(true)
    .newProxyInstance(DemoService.class);
o.hello("2321");
```

### 基于注册中心进行通信

创建registry

```java
DefaultRegistry defaultRegistry = new DefaultRegistry(8080);
defaultRegistry.start();
```

创建provider，使用`nettyProvider.registry(new RemoteAddress("localhost", 8080));`指定注册中心地址

```java
NettyProviderConfig providerConfig = new NettyProviderConfig();
providerConfig.setProviderKey("demo");
providerConfig.setWeight(4);
providerConfig.setPort(8082);
NettyProvider nettyProvider = new NettyProvider(providerConfig);
nettyProvider.publishServices(new DemoServiceImpl());
nettyProvider.registry(new RemoteAddress("localhost", 8080));
nettyProvider.start();

TimeUnit.SECONDS.sleep(100);
nettyProvider.shutdown();
```

创建consumer，`nettyConsumer.register("localhost", 8080);`同provider。注意到`connectWatch = nettyConsumer.watchConnect(metadata);`，这一步是关键，是向注册中心订阅需要的provider，获取之后会进行尝试连接provider，此时将进行阻塞，直到连接成功/失败。

```java
NettyConsumer nettyConsumer = new NettyConsumer();
nettyConsumer.register("localhost", 8080);
try {
    nettyConsumer.start();
} catch (InterruptedException e) {
    e.printStackTrace();
}
ServiceMetadata metadata = new ServiceMetadata("demo");
ConnectWatch connectWatch = null;
try {
    connectWatch = nettyConsumer.watchConnect(metadata);
} catch (InterruptedException | ConnectionException e) {
    e.printStackTrace();
}
DemoService o = ProxyObjectFactory
    .factory()
    .consumer(nettyConsumer)
    .connectWatch(connectWatch)
    .metadata(metadata)
    .newProxyInstance(DemoService.class);
String hello = o.hello("2");
```

### 异步调用

在registry，provider代码不变的情况下，consumer代码改为：

```java
NettyConsumer nettyConsumer = new NettyConsumer();
ServiceMetadata metadata = new ServiceMetadata("demo");
nettyConsumer.register("localhost", 8080);
nettyConsumer.start();
ConnectWatch connectWatch = nettyConsumer.watchConnect(metadata);
DemoService o = ProxyObjectFactory
    .factory()
    .consumer(nettyConsumer)
    .metadata(metadata)
    .isAsync(true)
    .connectWatch(connectWatch)
    .newProxyInstance(DemoService.class);
o.hello("21");
final InvokeFuture<String> future;
try {
    future = InvokeFutureContext.get(String.class);
    future.addListener(new FutureListener<String>() {
        @Override
        public void completed(String result, Throwable throwable) throws Exception {
            if (future.isSuccess()) {
                System.out.println(result);
            } else {
                System.out.println(throwable.getMessage());
            }
        }
    });
} catch (InvokeFutureException e) {
    e.printStackTrace();
}
```

通过`InvokeFutureContext.get(String.class);`获取该rpc调用的futrue对象，参数为目标方法返回值类型。随后通过addListener添加监听，但rpc调用成/失败，都会进行回调。

### provider将某个方法的结果发送给订阅者

```java
NettyProviderConfig providerConfig = new NettyProviderConfig();
providerConfig.setProviderKey("demo");
providerConfig.setWeight(4);
providerConfig.setPort(8082);
DemoServiceImpl demoService = new DemoServiceImpl();
final NettyProvider nettyProvider = new NettyProvider(providerConfig);
nettyProvider.publishServices(demoService);
nettyProvider.registry(new RemoteAddress("localhost", 8080));
ExecutorService executorService = Executors.newFixedThreadPool(1);
executorService.submit(new Runnable() {
    @Override
    public void run() {
        try {
            nettyProvider.start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
});

TimeUnit.SECONDS.sleep(3);

Notifier notifier = new Notifier(nettyProvider);
String hello = demoService.hello("21");
notifier.unicast(hello, DemoServiceImpl.class, "hello", new Class<?>[] {String.class});

TimeUnit.SECONDS.sleep(3);
String msg = demoService.msg();
notifier.unicast(msg, DemoServiceImpl.class, "msg", new Class<?>[] {});
TimeUnit.SECONDS.sleep(2000);
nettyProvider.shutdown();
```

关键是在`notifier.unicast(hello, DemoServiceImpl.class, "hello", new Class<?>[] {String.class});`

```java
public void unicast(Object result, Class<?> targetClass, String methodName, Class<?>... parameterTypes)
```

- result：目标方法调用的结果
- targetClass：目标的类，注意这里是需要带有@ProviderPublish的类
- methodName：目标方法名称
- parameterTypes：方法类型



```java
try {
    RepeatableFuture<String> watch = Listeners.onWatch("demo", DemoService.class, "hello", new Class<?>[]{String.class});
    watch.addListener(new FutureListener<String>() {
        @Override
        public void completed(String result, Throwable throwable) throws Exception {
            System.out.println(Thread.currentThread().getName() + result);
        }
    });
} catch (NoSuchMethodException e) {
    e.printStackTrace();
}
```

在消费者这一边，通过使用`Listeners.onWatch("demo", DemoService.class, "hello", new Class<?>[]{String.class});`监听目标方法的变化

```java
public static <V> RepeatableFuture<V> onWatch(String providerKey, Class<?> targetClass, String targetMethodName,
                                            Class<?>... parameterTypes)
```

- providerKey：服务提供者key
- targetClass：目标类，可以是不带有@ProviderPublish的，也可以是
- targetmethodName：目标方法名称
- parameterTypes：方法类型。



存在一种就是`DemoService` consumer没有引入依赖。此时使用`onWatch()`的重载方法

```java
RepeatableFuture<Object> objectRepeatableFuture = Listeners.onWatch("demo", "DemoService", "msg", null);
objectRepeatableFuture.addListener(new FutureListener<Object>() {
    @Override
    public void completed(Object result, Throwable throwable) throws Exception {
        System.out.println(Thread.currentThread().getName() + result);
    }
});
public static <V> RepeatableFuture<V> onWatch(String providerKey, String serviceName, String targetMethodName,
                                              Class<?>... parameterTypes)
```

第二参数只需要填service的名称



## 基于Spring

引入依赖

```java
<dependency>
    <groupId>com.jw.screw</groupId>
    <artifactId>screw-support-spring</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context</artifactId>
    <version>4.3.0.RELEASE</version>
</dependency>
```


是什么角色就引入什么配置，支持多角色配置。

### 注册中心配置

screw-registry.properties

```properties
# 注册中心端口
registry.port=8081
```

```java
/**
 * 注册中心配置类
 * @author jiangw
 * @date 2020/12/10 17:37
 * @since 1.0
 */
@Configuration
@PropertySource(name = "registry", value = {
        "classpath:prop/screw-registry.properties"
}, factory = NullablePropertySourceFactory.class)
public class RegistryConfig {

    @Bean
    public ScrewSpringRegistry screwRegistry() {
        return new ScrewSpringRegistry();
    }

    @Bean
    public RefreshListener refreshListener() {
        return new RefreshListener();
    }
}
```



### 提供者配置

screw-provider.properties

```properties
# 注册中心地址
registry.address=localhost
# 注册中心端口
registry.port=8081
# 服务地址
provider.port=8082
# 服务key
provider.providerKey=config_center
# 需要发布服务包所在位置
provider.packageScan=com\\jw\\admin\\ptb\\sys\\configuration\\center\\*
# 服务权重
provider.weight=4
# 处理rpc请求的核心线程数
provider.connCount=10
```

```java
@Configuration
@PropertySource(name = "provider", value = {
        "classpath:prop/screw-provider.properties"
}, factory = NullablePropertySourceFactory.class)
public class ProviderConfig {

    @Bean
    @DependsOn(value = {"screwRegistry"})
    public ScrewSpringProvider screwProvider() {
        return new ScrewSpringProvider();
    }
}
```

### 消费者配置

screw-consumer.properties

```properties
# 注册中心地址
registry.address=localhost
# 注册中心端口
registry.port=8081
# 连接等待时长 unit mills
consumer.waitMills=30000
# 负载均衡策略
consumer.loadbalance=RANDOM_WEIGHT
# 配置中心key
config.key=config_center
# 消费者作为服务提供者的key
consumer.key=3c59dc048e8850243be8079a5c74d079
```

```java
@Configuration
@PropertySource(name = "consumer", value = {
        "classpath:prop/screw-consumer.properties"
}, factory = NullablePropertySourceFactory.class)
public class ConsumerConfig {

    @Bean
    public List<ConsumerWrapper.ServiceWrapper> serviceWrapper() {
        // 如果提供者与消费者引用同一个service，那么可以进行配置
//        List<ConsumerWrapper.ServiceWrapper> wrappers = new ArrayList<>();
//        ConsumerWrapper.ServiceWrapper serviceWrapper = new ConsumerWrapper.ServiceWrapper();
//        serviceWrapper.setServices(Collections.singletonList(DemoService.class));
//        serviceWrapper.setServerKey("provider1");
//        wrappers.add(serviceWrapper);
//        return wrappers;
        return null;
    }

    @Bean
    public ConsumerWrapper consumerWrapper(@Qualifier("serviceWrapper") List<ConsumerWrapper.ServiceWrapper> serviceWrappers) {
        ConsumerWrapper consumerWrapper = new ConsumerWrapper();
        consumerWrapper.setServiceWrappers(serviceWrappers);
        return consumerWrapper;
    }

    @Bean
    public ScrewSpringConsumer screwConsumer(@Qualifier("consumerWrapper") ConsumerWrapper consumerWrapper) {
        ScrewSpringConsumer screwConsumer = new ScrewSpringConsumer();
        screwConsumer.setConsumerWrapper(consumerWrapper);
        return screwConsumer;
    }

    @Bean
    public FactoryBeanRegisterProcessor factory(@Qualifier("screwConsumer") ScrewSpringConsumer screwConsumer) {
        FactoryBeanRegisterProcessor com.jw.screw.monitor.remote.processor = new FactoryBeanRegisterProcessor();
        com.jw.screw.monitor.remote.processor.setProxies(screwConsumer.getProxies());
        return com.jw.screw.monitor.remote.processor;
    }

    @Bean
    public RefreshListener refreshListener() {
        return new RefreshListener();
    }
}
```

测试

```java
public class SpringRegistryTest {

    @Test
    public void registry() {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(RegistryConfigTest.class);

        ScrewSpringRegistry bean = applicationContext.getBean(ScrewSpringRegistry.class);
        int registryPort = bean.getRegistryPort();
        System.out.println(registryPort);
    }
}
```

```java
public class SpringProviderTest {

    @Test
    public void provider() throws InterruptedException {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(ProviderConfigTest.class);
        ScrewSpringProvider provider = applicationContext.getBean(ScrewSpringProvider.class);

        TimeUnit.SECONDS.sleep(100);
    }
}
```

```java
public class SpringConsumerTest {

    @Test
    public void consumer() throws InterruptedException {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(ConsumerConfigTest.class);
        DemoService bean = applicationContext.getBean(DemoService.class);
        String hello = bean.hello("2121");

        System.out.println(hello);

        hello = bean.hello("323232");
        System.out.println(hello);

        applicationContext.destroy();
    }
}
```

若provider与consumer引入同一个service包，我们则可以直接从spring context中获取这个bean，并直接进行rpc调用。

若没有引入同一个包，那么

```java
@GetMapping("/syncRpc")
public Object syncRpc(String msg) throws ConnectionException, InterruptedException {
    ServiceMetadata serviceMetadata = new ServiceMetadata("provider1");
    NettyConsumer consumer = screwSpringConsumer.getConsumer();
    ConnectWatch connectWatch = consumer.watchConnect(serviceMetadata);
    Object o = ProxyObjectFactory
        .factory()
        .consumer(consumer)
        .metadata(serviceMetadata)
        .isAsync(false)
        .connectWatch(connectWatch)
        .remoteInvoke("DemoService", "hello", new Object[]{"232321"});
    System.out.println(o);
    return o;
}
```

或者异步调用

```java
@GetMapping("/asyncRpc")
public Object asyncRpc(String msg) throws ConnectionException, InterruptedException, InvokeFutureException {
    ServiceMetadata serviceMetadata = new ServiceMetadata("provider1");
    NettyConsumer consumer = screwSpringConsumer.getConsumer();
    ConnectWatch connectWatch = consumer.watchConnect(serviceMetadata);
    ProxyObjectFactory
        .factory()
        .consumer(consumer)
        .metadata(serviceMetadata)
        .isAsync(true)
        .connectWatch(connectWatch)
        .remoteInvoke("DemoService", "hello", new Object[]{"232321"});
    InvokeFuture<String> stringInvokeFuture = InvokeFutureContext.get(String.class);
    stringInvokeFuture.addListener(new FutureListener<String>() {
        @Override
        public void completed(String s, Throwable throwable) throws Exception {
            System.out.println(s);
        }
    });
    return null;
}
```



## 配置中心

### 架构

![image-20201214110628474](C:\Users\jiangw\AppData\Roaming\Typora\typora-user-images\image-20201214110628474.png)

在引入配置服务后，可以说所有的消费者、提供者都要作为配置服务的消费者，这样我们可以在服务启动时动态刷新配置。注意的是基于spring进行配置。

### 使用

配置作为配置服务的消费者

```properties
# 注册中心地址
registry.address=localhost
# 注册中心端口
registry.port=8081
# 连接等待时长 unit mills
consumer.waitMills=30000
# 负载均衡策略
consumer.loadbalance=RANDOM_WEIGHT
# 配置中心key
config.key=config_center
# 消费者做为服务的key
consumer.key=3c59dc048e8850243be8079a5c74d079
```

即在最后面加上config.key与consumer.key。配置中心的具体使用看：

### 配置的获取

#### @ScrewValue

`@ScrewValue`注解与Spring`@Value`注解类似，只要被该注解的字段可以实现动态刷新，因为配置是以json形式创建，所有给出以下约定：

1. 如果配置是：{config: {subConfig: value}}形式，解析出来的配置结果是：config.subConfig
2. 如果配置是：{config: [{subConfig: value}]}形式，解析出来的配置结果是：config[0].subConfig
3. 作为array的数据一定是key-value形式

如果一个配置不满足上面约定，那么这个配置无法成功解析。

@ScrewValue关于配置的获取的规则为：**配置文件名-具体配置名称**

比如，文件名为address，里面的配置为：

```json
{
    "address":{
        "defaultCfg":{
            "cityName":"苏232州",
            "transType":"svr"
        },
        "types":[
            {
                "name":"百度",
                "type":"baidu"
            },
            {
                "name":"本地",
                "type":"local"
            }
        ],
        "mapCfg":"extend:mapcfg",
        "polygon":{
            "fillcolor":"0,0,0,1",
            "linecolor":"0,0,0,1"
        },
        "polyline":{
            "linecolor":"0,0,0,1",
            "width":2
        }
    }

```

那么解析address-address.defaultCfg.cityName的值为苏232州

```java
@ScrewValue("address-address.defaultCfg.cityName")
private String address;

@ScrewValue("address-address.types[0].name")
private String name;
```

#### Property

`Property`是一个获取配置上下文的类，可以直接通过它获取到相应的配置。使用为：

```java
Property.get(String name);
// 如
Property.get("address-address.defaultCfg.cityName");
// 也可以根据基本类型获取
Property.getString(String name);
Property.getInteger(String name);
...
```

## 监控中心

监控使得我们可以知道每一个服务（消费者、注册中心、提供者）的性能指标如内存、gc、堆和健康状况，并可以实现阈值报警。还可以通过链路追踪快速定位某个接口、某个rpc请求、某个流程流转的调用链路，通过这个链路能够快速跟踪到问题的所在。

技术栈：Java Agent、Byte Buddy、Micrometer、OpenTracing、Screw...。

### 架构

![image-20201214114449182](C:\Users\jiangw\AppData\Roaming\Typora\typora-user-images\image-20201214114449182.png)

