# RocketMQ 分布式消息中间件 理论介绍

## RocketMQ 简介
- RocketMQ 是阿里巴巴在 2012 年开源的第三代分布式消息中间件

- 2018年9月，阿里巴巴将 RocketMQ 捐赠给 Apache 软件基金会作为开源项目

- 历年双11，RocketMQ 都承载着阿里巴巴生产系统100%的消息流转，以2017年双11为例， RocketMQ 完成了1.2万亿消息精准低延迟投递，交易峰值高达17万笔/秒。

- 目前有 100 多家公司和科研机构正在使用RocketMQ

- Apache 上开源官方地址：https://rocketmq.apache.org/

- GitHub 托管地址：https://github.com/apache/rocketmq

- 阿里官方的介绍文档：http://jm.taobao.org/2017/01/12/rocketmq-quick-start-in-10-minutes/


![](https://img-blog.csdn.net/20180808163938680?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dhbmdteDE5OTMzMjg=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

## RocketMQ 特性
- RockatMQ 是一款分布式消息引擎
- 低延迟、高并发：99.6%以上的响应延迟在1毫秒以内
- 面向金融：满足跟踪和审计的高可用性

- 工业级适用：可确保万亿量级的消息发送

- 中立性：支持多种消息传递协议，如 JMS 和 OpenMessaging

- 性能可靠：给予足够的磁盘空间，消息可以累积存放而没有性能损失。

- 支持发布/订阅（Pub/Sub）和点对点（P2P）消息模型
- 在一个队列中可靠的先进先出（FIFO）和严格的顺序传递
- 支持拉（pull）和推（push）两种消息模式
- 单一队列百万消息的堆积能力
- 分布式高可用的部署架构，满足至少一次消息传递语义
- 提供 docker 镜像用于隔离测试和云集群部署
- 提供配置、指标和监控等功能丰富的 Dashboard

## 专业术语
- RocketMQ 涉及的专有名词比较多，需要在实际开发中逐渐加深了解。

### Producer
- Producer 消息生产者，生产者的作用就是将消息发送到 MQ（Message Queue）
- 消息生产者，负责产生消息，一般由业务系统负责产生消息。

### Producer Group
- 生产者组，简单来说就是多个发送同一类消息的生产者称之为一个生产者组，大家发送逻辑一致。

### Consumer
- Consumer 消息消费者，简单来说，消费 MQ 上的消息的应用程序就是消费者，至于消息是否进行逻辑处理，还是直接存储到数据库等取决于业务需要。
- 消息消费者，负责消费消息，一般是后台系统负责异步消费

### Consumer Group
- 消费者组，和生产者类似，消费同一类消息的多个 consumer 实例组成一个消费者组，大家消费逻辑一致。

![](https://img-blog.csdn.net/20180808170041151?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dhbmdteDE5OTMzMjg=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

- 通过 Group 机制，让 RocketMQ 天然的支持消息负载均衡！比如某个Topic(主题)有9条消息，其中一个Consumer Group有3个实例（3个进程 OR 3台机器），那么每个实例将均摊3条消息！

### Topic
- **Topic(主题) 是一种消息的逻辑分类**，比如有订单类的消息，也有库存类的消息，那么就需要进行分类，一个是订单 Topic 存放订单相关的消息，一个是库存 Topic 存储库存相关的消息。

### Message
- **Message 是消息的载体**，一个 Message 必须指定 topic，相当于寄信的地址。
- Message 还有一个可选的 tag 设置，以便消费端可以基于 tag 进行过滤消息。也可以添加额外的键值对，例如你需要一个业务 key 来查找 broker（代理人/中间人） 上的消息，方便在开发过程中诊断问题。

### Tag
- Tag (标签)可以被认为是对 Topic 进一步细化。一般在相同业务模块中通过引入标签来标记不同用途的消息。

### Broker
- Broker（代理人/中间人） 是 RocketMQ 系统的主要角色，其实就是前面一直说的 MQ。
- Broker 接收来自生产者的消息，储存以及为消费者拉取消息的请求做好准备。
- **Broker  作为消息中转角色，负责存储消息，转发消息，一般也称为 Server**

### Name Server
- Name Server 为 producer 和 consumer 提供路由信息。

## RocketMQ 物理部署结构

![](https://img-blog.csdn.net/20180810115535795?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dhbmdteDE5OTMzMjg=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

- 从这架构图中可以看到它主要由四部分组成：Producer（生产者）、NameServer、Broker、Consumer（消费者）。

- 如上图所示， RocketMQ 的部署结构有以下特点：

    > 1. Name Server 是一个几乎无状态节点，可集群部署，节点之间无任何信息同步。
    > 2. Broker 部署相对复杂，Broker 分为 Master 与 Slave，一个 Master（主人） 可以对应多个 Slave(奴隶)，但是一个Slave只能对应一个Master，Master与Slave的对应关系通过指定相同的 BrokerName，不同的BrokerId来定义，BrokerId为0表示Master，非0表示Slave。Master也可以部署多个，每个Broker与Name Server集群中的所有节点建立长连接，定时注册Topic 信息到所有 Name Server。
    > 3. Producer 与 Name Server 集群中的其中一个节点（随机选择）建立长连接，定期从Name Server取Topic路由信息，并向提供 Topic 服务的 Master 建立长连接，且定时向 Master 发送心跳。Producer完全无状态，可集群部署。
    > 4. Consumer 与 Name Server 集群中的其中一个节点（随机选择）建立长连接，定期从Name Server取Topic路由信息，并向提供 Topic 服务的 Master、Slave 建立长连接，且定时向 Master、Slave发送心跳。Consumer既可以从Master订阅消息，也可以从Slave订阅消息，订阅规则由Broker配置决定。

### Producer
-   生产者支持分布式部署。分布式生产者通过多种负载均衡模式向 Broker 集群发送消息。发送过程支持快速失败并具有低延迟。

### NameServer
- 提供轻量级服务和路由，每个 Name Server 记录完整的路由信息，提供相应的读写服务，支持快速存储扩展。主要包括两个功能：

    > 1. 代理管理，NameServer 接受来自 Broker 集群的注册，并提供检测代理是否存在的心跳机制。
    > 2. 路由管理，NameServer 将保存有关代理集群的全部路由信息以及客户端查询的队列信息。
- RocketMQ 客户端(生产者/消费者)会从 NameServer 查询队列路由信息。客户端通过如下方式之一找到NameServer地址：

    > 1. 编程方式，如：producer.setNamesrvAddr("ip:port")
    > 2. Java 选项，如：rocketmq.namesrv.addr
    > 3. 环境变量，如：NAMESRV_ADDR
    > 4. HTTP 端点

### Broker
  Broker 通过提供轻量级的 Topic 和 Queue 机制来进行消息存储。
  Broker 支持 Push 和 Pull 模式，包含容错机制，并且提供了强大的峰值填充和以原始时间顺序累计数千亿条消息的能力。
  Broker 还提供灾难恢复，丰富的指标统计数据和警报机制，而传统的消息传递系统都缺乏这些机制

## RocketMQ 逻辑部署结构

![](https://img-blog.csdn.net/20180808171317377?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dhbmdteDE5OTMzMjg=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

如上图所示，RocketMQ 的逻辑部署结构有 Producer 和 Consumer 两个特点。

- Producer Group 用来表示一个发送消息应用，一个Producer Group下包含多个Producer实例，可以是多台机器，也可以是一台机器的多个进程，或者一个进程的多个Producer对象。一个 Producer Group 可以发送多个Topic消息。Producer Group作用如下：

    > 1. 标识一类 Producer
    > 2. 可以通过运维工具查询这个发送消息应用下有多个 Producer 实例
    > 3. 发送分布式事务消息时，如果 Producer 中途意外宕机，Broker 会主动回调 Producer Group 内的任意一台机器来确认事务状态。
- Consumer Group 用来表示一个消费消息应用，一个Consumer Group下包含多个Consumer实例，可以是多台机器，也可以是多个进程，或者是一个进程的多个Consumer对象。一个Consumer Group下的多个Consumer以均摊方式消费消息，如果设置为广播方式，那么这个Consumer Group下的每个实例都消费全量数据。

## RocketMQ 集群部署模式

![](https://img-blog.csdn.net/20180808171335698?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dhbmdteDE5OTMzMjg=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

- 如上图所示， RocketMQ的部署结构有以下特点：
    >
    > 1. Name Server是一个几乎无状态节点，可集群部署，节点之间无任何信息同步。
    > 2. Broker 部署相对复杂，Broker 分为 Master 与 Slave，一个 Master（主人） 可以对应多个 Slave(奴隶)，但是一个Slave只能对应一个Master，Master与Slave的对应关系通过指定相同的 BrokerName，不同的BrokerId来定义，BrokerId为0表示Master，非0表示Slave。Master也可以部署多个，每个Broker与Name Server集群中的所有节点建立长连接，定时注册Topic 信息到所有 Name Server。
    > 3. Producer 与 Name Server 集群中的其中一个节点（随机选择）建立长连接，定期从Name Server取Topic路由信息，并向提供 Topic 服务的 Master 建立长连接，且定时向 Master 发送心跳。Producer完全无状态，可集群部署。
    > 4. Consumer 与 Name Server 集群中的其中一个节点（随机选择）建立长连接，定期从Name Server取Topic路由信息，并向提供 Topic 服务的 Master、Slave 建立长连接，且定时向 Master、Slave发送心跳。Consumer既可以从Master订阅消息，也可以从Slave订阅消息，订阅规则由Broker配置决定。
    >

### 单 master 模式

  - 也就是只有一个 master 节点，称不上是集群，一旦这个 master 节点宕机，那么整个服务就不可用，适合个人学习使用。

### 多 master 模式
  - 多个 master 节点组成集群，单个 master 节点宕机或者重启对应用没有影响。
  - 优点：所有模式中性能最高
  - 缺点：单个 master 节点宕机期间，未被消费的消息在节点恢复之前不可用，消息的实时性就受到影响。
  - 注意：使用同步刷盘可以保证消息不丢失，同时 Topic 相对应的 queue 应该分布在集群中各个节点，而不是只在某各节点上，否则，该节点宕机会对订阅该 topic 的应用造成影响。

### 多 master 多 slave 异步复制模式
  - 在多 master 模式的基础上，每个 master 节点都有至少一个对应的 slave。
  - master 节点可读可写，但是 slave 只能读不能写，类似于 mysql 的主从模式。
  - 优点： 在 master 宕机时，消费者可以从 slave 读取消息，消息的实时性不会受影响，性能几乎和多 master 一样。
  - 缺点：使用异步复制的同步方式有可能会有消息丢失的问题。

### 多 master 多 slave 同步双写模式
  - 同多 master 多 slave 异步复制模式类似，区别在于 master 和 slave 之间的数据同步方式。
  - 优点：同步双写的同步模式能保证数据不丢失。
  - 缺点：发送单个消息 RT 会略长，性能相比异步复制低10%左右。
  - 刷盘策略：同步刷盘和异步刷盘（指的是节点自身数据是同步还是异步存储）
  - 同步方式：同步双写和异步复制（指的一组 master 和 slave 之间数据的同步）
  - 注意：要保证数据可靠，需采用同步刷盘和同步双写的方式，但性能会较其他方式低。