# RocketMQ 分布式消息中间件-Java端入门代码示例

## 一、新建Maven工程，引入依赖

- Maven地址：[**https://mvnrepository.com/artifact/org.apache.rocketmq/rocketmq-client**](https://mvnrepository.com/artifact/org.apache.rocketmq/rocketmq-client) 请根据安装的版本引入对应的依赖

- 我这里安装的是当前最新版4.5.0，所以我对应的依赖是：

  ```
  <!-- https://mvnrepository.com/artifact/org.apache.rocketmq/rocketmq-client -->
  <dependency>
      <groupId>org.apache.rocketmq</groupId>
      <artifactId>rocketmq-client</artifactId>
      <version>4.5.0</version>
  </dependency>  
  ```

- 这里我还引入了一个 ```rocketmq-example```，这里面的示例代码和官方文档是一直的，可以直接参考

  ```
  <!-- https://mvnrepository.com/artifact/org.apache.rocketmq/rocketmq-example -->
  <dependency>
      <groupId>org.apache.rocketmq</groupId>
      <artifactId>rocketmq-example</artifactId>
      <version>4.5.0</version>
  </dependency>
  ```

- 前一步的RocketMQ服务搭建并运行无误就可以进入Java编码了

## 二、编码

- **这里重点说一下，防火墙请放行9876端口，如果可以请把防火墙彻底关掉。**
- 贴上CentOS7中 ```firewalld``` 的基本使用命令：
  - 启动：```systemctl start firewalld```
  - 关闭：```systemctl stop firewalld```
  - 查看状态：```systemctl status firewalld ```
  - 开机禁用  ：```systemctl disable firewalld```
  - 开机启用  ：```systemctl enable firewalld```
  - 放行9876端口：```firewall-cmd --zone=public --add-port=9876/tcp --permanent```（--permanent永久生效，没有此参数重启后失效）
  - 重新载入：```firewall-cmd --reload```（修改端口后需要重新载入）
  - 查看9876端口：```firewall-cmd --zone= public --query-port=9876/tcp```
  - 删除9876端口：```firewall-cmd --zone= public --remove-port=9876/tcp --permanent```

### 文档

- **本篇代码参照自官方文档及 ```rocketmq-example``` 中的示例代码**

- 官方文档及快速上手示例地址：[**https://rocketmq.apache.org/docs/simple-example/**](https://rocketmq.apache.org/docs/simple-example/)

- Producer示例代码路径：```org.apache.rocketmq.example.quickstart.Producer``` (需导入 ```rocketmq-example``` 依赖)
- Customer示例代码路径：```org.apache.rocketmq.example.quickstart.Customer``` (需导入 ```rocketmq-example``` 依赖)

### Producer

- 代码参考，每个步骤写有注释

  ```
    package com.rocketmq.demo;

    import org.apache.rocketmq.client.exception.MQClientException;
    import org.apache.rocketmq.client.producer.DefaultMQProducer;
    import org.apache.rocketmq.client.producer.SendResult;
    import org.apache.rocketmq.common.message.Message;
    import org.apache.rocketmq.remoting.common.RemotingHelper;

    /**
     * 生产者测试
     *
     * @author zhangkuan
     * @date 2019/4/8
     */
    public class ProducerTest {

        public static void main(String[] args) throws MQClientException, InterruptedException {

            /**
             * 声明并初始化一个 Producer，同时指定 Producer Group 的名称
             * 一个应用创建一个 Producer，由应用来维护此对象，可以设置为全局对象或者单例
             * ProducerGroup 的名称 "producerGroupName" 需要由应用来保证唯一性
             * ProducerGroup 这个概念发送普通的消息时，作用不大，但是发送分布式事务消息时，比较关键，
             * 因为服务器会回查这个 Group 下的任意一个 Producer
             */
            DefaultMQProducer producer = new DefaultMQProducer("myProducerGroupName_1");
            /**
             * 指定 Producer 连接的 nameServer 服务器所在地址以及端口
             * 如果是分布式部署的多个，则用分号隔开，如：
             * setNamesrvAddr("172.16.235.77:9876;172.16.235.78:9876");
             * 这里只是为了方便才将地址与端口写死，实际中应该至少放在配置文件中去
             */
            producer.setNamesrvAddr("47.75.222.71:9876");
            /**
             * 指定自己的在 Producer Group 中的名称
             */
            producer.setInstanceName("myProducer_1");
            /**
             * Producer 对象在使用之前必须要调用 start 进行启动初始化
             * 初始化一次即可，切忌不可每次发送消息时，都调用start方法
             */
            producer.start();

            /**
             * 一个 Producer 对象可以发送多个 topic（主题），多个 tag 的消息
             * 本实例 send 方法采用同步调用，只要不抛异常就标识成功
             */
            boolean flag1 = true;
            boolean flag2 = true;
            for (int i = 0; i < 8; i++) {
                try {
                    Message msg;
                    if (i % 2 == 1) {
                        /*
                         * 创建一个消息实例，指定topic、tag和消息主体。
                         */
                        msg = new Message("myTopicTest_1", flag1 ? "TagA" : "TagB", ("Hello RocketMQ " + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
                        flag1 = !flag1;
                    } else {
                        /*
                         * 创建一个消息实例，指定topic、tag和消息主体。
                         */
                        msg = new Message("myTopicTest_2", flag2 ? "TagC" : "TagD", ("Hello RocketMQ " + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
                        flag2 = !flag2;
                    }
                    /*
                     * 调用send message将消息传递给其中的一个节点
                     */
                    SendResult sendResult = producer.send(msg);
                    System.out.printf("%s%n", sendResult);
                } catch (Exception e) {
                    e.printStackTrace();
                    Thread.sleep(500);
                }
            }

            /**
             * 应用退出时，调用 shutdown 关闭网络连接，清理资源，从 MocketMQ 服务器上注销自己
             * 建议应用在 JBOSS、Tomcat 等容器的退出钩子里调用 shutdown 方法
             */
            producer.shutdown();
        }
    }
  ```

### Customer

- 消费者代码，同样在注释中说明

  ```
    package com.rocketmq.demo;

    import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
    import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
    import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
    import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
    import org.apache.rocketmq.client.exception.MQClientException;
    import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
    import org.apache.rocketmq.common.message.MessageExt;

    import java.util.List;

    /**
     * 消费者测试
     *
     * @author zhangkuan
     * @date 2019/4/8
     */
    public class CustomerTest {

        /**
         * 当前例子是 PushConsumer 用法，给用户感觉是消息从 RocketMQ 服务器推到了应用客户端。
         * 而实际 PushConsumer 内部是使用长轮询 Pull(拉取) 方式从 MetaQ 服务器拉消息，然后再回调用户 Listener方法
         *
         * @param args
         * @throws InterruptedException
         * @throws MQClientException
         */
        public static void main(String[] args) throws MQClientException {
            /**
             * 声明并初始化 一个 consumer
             * Consumer Group 组名，多个 Consumer 如果属于一个应用，订阅同样的消息，且消费逻辑一致，则应该将它们归为同一组
             * 一个应用创建一个 Consumer，由应用来维护此对象，可以设置为全局对象或者单例
             * ConsumerGroupName 需要由应用来保证唯一
             */
            DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("myCustomerGroupName_1");
            /**
             * 指定 NameServer 的地址 与端口
             * 指定自己在 Consumer Group 组中的名称
             */
            consumer.setNamesrvAddr("47.75.222.71:9876");
            consumer.setInstanceName("myConsumer_1");
            /**
             * 设置 consumer 的消费策略
             * CONSUME_FROM_LAST_OFFSET 默认策略，从该队列最尾开始消费，即跳过历史消息
             * CONSUME_FROM_FIRST_OFFSET 从队列最开始开始消费，即历史消息（还储存在broker的）全部消费一遍
             * CONSUME_FROM_TIMESTAMP 从某个时间点开始消费，和 setConsumeTimestamp() 配合使用，默认是半个小时以前
             */
            consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
            /**
             * 消费者订阅消息，如下所示订阅 topic 为 myTopicTest_1 下tag为 TagA 类型的消息
             * 订阅指定 topic 下 tags 分别等于TagC或TagD，则为：<code>consumer.subscribe("myTopicTest_2", "TagC||TagD");</code>
             * 一个 consumer 对象可以订阅多个 topic，如下
             */
            consumer.subscribe("myTopicTest_1", "TagA");
            consumer.subscribe("myTopicTest_2", "TagC||TagD");

            /**
             * 注册消息监听器，如果有订阅的消息就会响应
             */
            consumer.registerMessageListener(new MessageListenerConcurrently() {
                /**
                 * 默认 msgs 里只有一条消息，可以通过设置 consumeMessageBatchMaxSize 参数来批量接收消息
                 * consumeThreadMin:消费线程池数量 默认最小值10
                 * consumeThreadMax:消费线程池数量 默认最大值20
                 */
                @Override
                public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                    System.out.println(Thread.currentThread().getName() + " Receive New Messages: " + msgs.size());
                    MessageExt msg = msgs.get(0);
                    System.out.printf("%s Receive New Messages: %s %n", new Object[]{Thread.currentThread().getName(), msgs});
                    System.out.println("----topic: " + msg.getTopic() + ", tag: " + msg.getTags() + ", body: " + new String(msg.getBody()));
                    /**
                     * 返回消费状态
                     * CONSUME_SUCCESS 消费成功
                     * RECONSUME_LATER 消费失败，需要稍后重新消费
                     */
                    //return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
            });

            /**
             * Consumer对象在使用之前必须要调用start初始化，初始化一次即可
             */
            consumer.start();
            System.out.printf("Consumer Started.%n");
        }
    }

  ```

## 三、可能遇到的坑

### RocketMQ服务外网不能访问

- 如果你按照上一篇的方式运行，那么你可能会遇到下面这种错误（在本地运行RocketMQ不会遇到这个问题）

  ```
  org.apache.rocketmq.remoting.exception.RemotingTooMuchRequestException: sendDefaultImpl call timeout
  at org.apache.rocketmq.client.impl.producer.DefaultMQProducerImpl.sendDefaultImpl(DefaultMQProducerImpl.java:634)
  at org.apache.rocketmq.client.impl.producer.DefaultMQProducerImpl.send(DefaultMQProducerImpl.java:1279)
  at org.apache.rocketmq.client.impl.producer.DefaultMQProducerImpl.send(DefaultMQProducerImpl.java:1225)
  at org.apache.rocketmq.client.producer.DefaultMQProducer.send(DefaultMQProducer.java:283)
  at com.rocketmq.demo.ProducerTest.main(ProducerTest.java:69)
  09:19:42.792 [NettyClientSelector_1] INFO  RocketmqRemoting - closeChannel: close the connection to remote address[] result: true
  ```

- 那么你需要修改一些配置，在编译后的目录中的 ```conf``` 文件夹下有一个 ```broker.conf``` 配置文件，在其末尾添加如下配置：

  - ```vim /root/rocketmq-4.5.0/conf/broker.conf```

    ```
    brokerClusterName = DefaultCluster
    brokerName = broker-a
    brokerId = 0
    deleteWhen = 04
    fileReservedTime = 48
    brokerRole = ASYNC_MASTER
    flushDiskType = ASYNC_FLUSH
    
    # 添加如下两行内容，'47.**.***.71'为你的服务器公网IP
    namesrvAddr=47.**.***.71:9876
    # 需要指定broker外网IP否则外网不能访问
    brokerIP1=47.**.***.71
    ```

- 然后将broker的运行方式改为： ```nohup sh bin/mqbroker -c conf/broker.conf &```

