package com.guitu18.test;

import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;

/**
 * 消息监听器，收到消息会调用consumeMessage方法
 *
 * @author zhangkuan
 * @date 2019/4/10
 */
public class MessageListener implements MessageListenerConcurrently {
    /**
     * 默认msgs里只有一条消息，可以通过设置 consumeMessageBatchMaxSize 参数来批量接收消息
     * consumeThreadMin:消费线程池数量 默认最小值10
     * consumeThreadMax:消费线程池数量 默认最大值20
     */
    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        System.out.println(Thread.currentThread().getName() + " 消息数量: " + msgs.size());
        for (MessageExt msg : msgs) {
            System.out.println(Thread.currentThread().getName() + " ----topic: " + msg.getTopic() +
                    ", tag: " + msg.getTags() + ", body: " + new String(msg.getBody()));
        }
        /**
         * 返回消费状态
         * CONSUME_SUCCESS 消费成功
         * RECONSUME_LATER 消费失败，需要稍后重新消费
         */
        //return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }
}
