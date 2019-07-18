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

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.exception.RemotingException;

import java.util.List;

/**
 * 生产者
 *
 * @author zhangkuan
 * @date 2019/4/9
 */
public class ProducerSeletor {

    public static void sendMessage(List<Message> list) throws MQClientException, InterruptedException, RemotingException, MQBrokerException {
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
         * 指定实例名称
         */
        producer.setInstanceName("myProducer_1");
        /**
         * Producer 对象在使用之前必须要调用 start 进行启动初始化
         * 初始化一次即可，切忌不可每次发送消息时，都调用start方法
         */
        producer.start();

        for (Message message : list) {
            /**
             * 一个 Producer 对象可以发送多个 topic（主题），多个 tag 的消息
             * 本实例 send 方法采用同步调用，只要不抛异常就表示成功
             */
            SendResult sendResult = producer.send(message, new MessageQueueSelector() {
                @Override
                public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                    Integer id = (Integer) arg;
                    int index = id % mqs.size();
                    return mqs.get(index);
                }
            }, 10);
            System.out.printf("%s%n", sendResult);
        }

        /**
         * 应用退出时，调用 shutdown 关闭网络连接，清理资源，从 MocketMQ 服务器上注销自己
         * 建议应用在 JBOSS、Tomcat 等容器的退出钩子里调用 shutdown 方法
         */
        producer.shutdown();
    }
}
