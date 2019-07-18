package com.guitu18.test;

import com.guitu18.service.Producer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhangkuan
 * @date 2019/4/9
 */
public class ProducerTest {

    public static void main(String[] args) throws UnsupportedEncodingException {
        List<Message> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            // 创建一个消息实例，指定topic、tag和消息主体
            byte[] content = ("RocketMQ测试消息_" + i).getBytes(RemotingHelper.DEFAULT_CHARSET);
            list.add(new Message("TopicTest", "TagA", content));
        }
        try {
            // 发送消息
            Producer.sendMessage(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
