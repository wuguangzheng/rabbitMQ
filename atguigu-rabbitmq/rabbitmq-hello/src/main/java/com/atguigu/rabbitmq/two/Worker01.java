package com.atguigu.rabbitmq.two;

import com.atguigu.rabbitmq.utils.RabbitMqUtils;
import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

/**
 * 这是一个工作线程（相当于之前消费者）
 */
public class Worker01 {
    // 队列的名称
    public static final String QUEUE_NAME = "hello";

    // 接受消息
    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMqUtils.getChannel();

        DeliverCallback deliverCallback = (consumerTag, message) -> {
            System.out.println("接收到的消息：" + new String(message.getBody()));
        };

        // 消息接收被取消时，执行下面的内容
        CancelCallback cancelCallback = (consumerTag) -> {
            System.out.println(consumerTag + "消息被消费者取消消费接口回调逻辑");
        };

        // 消息的接收
        System.out.println("C2等待接收消息.....");
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, cancelCallback);
    }
}
