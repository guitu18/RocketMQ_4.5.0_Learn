package com.guitu18.aliyun;

import com.aliyun.openservices.ons.api.*;

import java.util.Properties;

/**
 * 阿里云消费者测试
 *
 * @author zhangkuan
 * @date 2019/4/9
 */
public class AliConsumer {

    public static void main(String[] args) {
        Properties properties = new Properties();
        // 您在控制台创建的 Group ID
        properties.put(PropertyKeyConst.GROUP_ID, "GID_TEST");
        // AccessKey 阿里云身份验证，在阿里云服务器管理控制台创建
        properties.put(PropertyKeyConst.AccessKey, "LTAITKpsO5XWFi4w");
        // SecretKey 阿里云身份验证，在阿里云服务器管理控制台创建
        properties.put(PropertyKeyConst.SecretKey, "ilce3n7kAYn0RWrw3COUSKcqWLzfqe");
        // 设置 TCP 接入域名，到控制台的实例基本信息中查看
        properties.put(PropertyKeyConst.NAMESRV_ADDR,
                "http://MQ_INST_1442213751203263_Bam90Mxw.mq-internet-access.mq-internet.aliyuncs.com:80");
        // 集群订阅方式 (默认)
        properties.put(PropertyKeyConst.MessageModel, PropertyValueConst.CLUSTERING);
        // 广播订阅方式
        // properties.put(PropertyKeyConst.MessageModel, PropertyValueConst.BROADCASTING);
        Consumer consumer = ONSFactory.createConsumer(properties);
        //订阅多个 Tag
        consumer.subscribe("MyTopicTest", "TagA||TagB", new MessageListener() {
            @Override
            public Action consume(Message message, ConsumeContext context) {
                System.out.println("Receive: " + message);
                System.out.println(new String(message.getBody()));
                return Action.CommitMessage;
            }
        });
        //订阅另外一个 Topic 全部 Tag
        consumer.subscribe("TopicTestMQ-Other", "*", new MessageListener() {
            @Override
            public Action consume(Message message, ConsumeContext context) {
                System.out.println("Receive: " + message);
                System.out.println(new String(message.getBody()));
                return Action.CommitMessage;
            }
        });
        consumer.start();
        System.out.println("Consumer Started");
    }

}
