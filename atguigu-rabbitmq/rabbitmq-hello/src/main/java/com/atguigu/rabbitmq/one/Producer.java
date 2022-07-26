package com.atguigu.rabbitmq.one;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * hello world！ 入门级，没有交换机 exchange
 * 生产者：发消息
 */
public class Producer {
    // 队列名称
    private final static String QUEUE_NAME = "hello";

    // 发消息
    public static void main(String[] args) throws Exception {
        // 创建一个连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        // 工厂IP连接RabbitMQ队列
        factory.setHost("192.168.68.129");
        // 用户名
        factory.setUsername("admin");
        // 密码
        factory.setPassword("123");

        //默认端口为5672
        // 创建连接
        try(Connection connection = factory.newConnection();
        // 获取信道
        Channel channel = connection.createChannel()){
            /**
             * 生成一个队列
             * 1. 队列名称
             * 2. 队列里面的消息是否持久化 默认消息存储在内存中,不持久化
             * 3. 该队列是否只供一个消费者进行消费 是否进行共享 true 可以多个消费者消费
             * 4. 是否自动删除 最后一个消费者端开连接以后 该队列是否自动删除 true 自动删除
             * 5. 其他参数
             */
            Map<String,Object> arguments = new HashMap<>();
            arguments.put("x-max-priority", 10);
            channel.queueDeclare(QUEUE_NAME, true, false, false, arguments);
            for (int i = 1; i < 11; i++) {
                String message = "info" + i;
                if (i == 5){
                    AMQP.BasicProperties properties = new AMQP.BasicProperties().builder().priority(5).build();
                    channel.basicPublish("", QUEUE_NAME, properties, message.getBytes());
                }else {
                    channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
                }
            }
            /**
             * 发送一个消息
             * 1. 发送到那个交换机
             * 2. 路由的 key 是哪个，本次是队列的名称
             * 3. 其他的参数信息
             * 4. 发送消息的消息体
             */


            System.out.println("消息发送完毕！");
        }
    }
}
