package com.atguigu.rabbitmq.three;

import com.atguigu.rabbitmq.utils.RabbitMqUtils;
import com.atguigu.rabbitmq.utils.SleepUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

/**
 * 消息在手动应答时是不丢失的，放回队列中重新消费
 */
public class Work03 {
    // 队列名称
    public static final String TASK_QUEUE_NAME = "ack_queue";

    // 接收消息
    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMqUtils.getChannel();
        System.out.println("C1等待接收消息处理时间较短");

        DeliverCallback deliverCallback = (consumerTag, message) -> {
            // 沉睡1S
            SleepUtils.sleep(1);
            System.out.println("接收的消息：" + new String(message.getBody(), "UTF-8"));
            // 手动应答
            /**
             * 1、消息的标记 tag
             * 2、是否批量应答
             */
            channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
        };

        // 值为1的预取值间接将队列改为了不公平分发
       // int prefetchCount = 1;
        // 预取值为2
        int prefetchCount = 2;
        channel.basicQos(prefetchCount);

        // 手动应答
        boolean autoAck = false;
        channel.basicConsume(TASK_QUEUE_NAME, autoAck, deliverCallback, (consumerTag -> {
            System.out.println(consumerTag + "消费者取消消费接口回调逻辑");
        }));
    }
}
