package com.guitu18.test;

import com.guitu18.service.Consumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;

/**
 * @author zhangkuan
 * @date 2019/4/9
 */
public class Consumer2Test {

    public static void main(String[] args) throws MQClientException {
        // 设置topic和tags
        String[] subscribes = new String[]{"TopicTest, *"};
        // 运行Consumer
        Consumer.pullMessage(subscribes, MessageModel.CLUSTERING, new MessageListener());
    }

}
