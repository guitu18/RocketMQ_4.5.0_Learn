/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.guitu18.service;

import org.apache.rocketmq.client.ClientConfig;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.impl.consumer.DefaultMQPushConsumerImpl;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;

/**
 * 消费者
 *
 * @author zhangkuan
 * @date 2019/4/9
 */
public class Consumer {

    /**
     * 当前例子是 PushConsumer 用法，给用户感觉是消息从 RocketMQ 服务器推到了应用客户端。
     * 而实际 PushConsumer 内部是使用长轮询 Pull(拉取) 方式从 MetaQ 服务器拉消息，然后再回调用户 Listener方法
     *
     * @param subscribes
     * @param messageListener
     * @throws MQClientException
     */
    public static void pullMessage(String[] subscribes, String instanceName, MessageModel messageModel, MessageListenerConcurrently messageListener) throws MQClientException {
        /**
         * 初始化一个Consumer并指定组名ConsumerGroup
         * 多个Consumer如果属于一个应用，订阅同样的消息，且消费逻辑一致，则应该将它们归为同一组
         * 一个应用创建一个Consumer，由应用来维护此对象，可以设置为全局对象或者单例
         * ConsumerGroupName 需要由应用来保证唯一性
         */
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("myCustomerGroupName_1");
        /**
         * 指定NameServer的地址和端口
         */
        consumer.setNamesrvAddr("47.75.222.71:9876");
        /**
         * 这里采坑，记一笔
         * 设置实例名称，有相同实例名的实例在集群模式消费时负载均衡分配到的是相同的消息，不配置时默认为'DEFAULT'
         * 这里在底层做了判断，如果实例名为'DEFAULT'那么会将当前进程PID作为实例名，即不配置默认是不同的实例
         * @see DefaultMQPushConsumerImpl#start() 在这个方法做了判断，如果是CLUSTERING模式会调用
         * @see ClientConfig#changeInstanceNameToPID() 如果实例名为默认的'DEFAULT'则重设实例名为进程PID
         */
        consumer.setInstanceName(instanceName);
        /**
         * 设置 consumer 的消费策略
         * CONSUME_FROM_LAST_OFFSET 默认策略，从该队列最尾开始消费，即跳过历史消息
         * CONSUME_FROM_FIRST_OFFSET 从队列最开始开始消费，即历史消息（还储存在broker的）全部消费一遍
         * CONSUME_FROM_TIMESTAMP 从某个时间点开始消费，和 setConsumeTimestamp() 配合使用，默认是半个小时以前
         */
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        /**
         * 设置消费模式
         * CLUSTERING：集群模式
         * BROADCASTING：广播模式
         */
        consumer.setMessageModel(messageModel);
        /**
         * 消费者订阅消息，如下所示订阅 topic 为'Topic测试'下所有tag类型的消息
         * 订阅指定topic下tags分别等于TagC或TagD，则为：<code>consumer.subscribe("myTopicTest", "TagC||TagD");</code>
         * 一个 consumer 对象可以订阅多个 topic
         */
        try {
            for (String subscribe : subscribes) {
                String[] sub = subscribe.split(",");
                consumer.subscribe(sub[0].trim(), sub[1].trim());
            }
        } catch (Exception e) {
            throw new RuntimeException("参数解析失败，请检查参数是否正确");
        }
        /**
         * 注册消息监听器，如果有订阅的消息就会响应
         */
        consumer.registerMessageListener(messageListener);
        /**
         * Consumer对象在使用之前必须要调用start初始化，初始化一次即可
         */
        consumer.start();
        System.out.printf("Consumer Started.%n");
    }

    /**
     * 默认使用'DEFAULT'作为实例名
     *
     * @param subscribes
     * @param messageListener
     * @throws MQClientException
     */
    public static void pullMessage(String[] subscribes, MessageListenerConcurrently messageListener) throws MQClientException {
        pullMessage(subscribes, "DEFAULT", MessageModel.CLUSTERING, messageListener);
    }

    /**
     * 默认使用集群模式
     *
     * @param subscribes
     * @param messageModel
     * @param messageListener
     * @throws MQClientException
     */
    public static void pullMessage(String[] subscribes, MessageModel messageModel, MessageListenerConcurrently messageListener) throws MQClientException {
        pullMessage(subscribes, "DEFAULT", messageModel, messageListener);
    }
}
