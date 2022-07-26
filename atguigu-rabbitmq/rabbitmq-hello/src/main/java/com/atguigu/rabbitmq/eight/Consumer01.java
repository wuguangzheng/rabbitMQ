package com.atguigu.rabbitmq.eight;

import com.atguigu.rabbitmq.utils.RabbitMqUtils;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

import java.util.HashMap;
import java.util.Map;

/**
 * 死信队列实战
 * 消费者1
 */
public class Consumer01 {
    // 普通交换机的名称
    public static final String NORMAL_EXCHANGE = "normal_exchange";
    // 死信交换机的名称
    public static final String DEAD_EXCHANGE = "dead_exchange";
    // 普通队列的名称
    public static final String NORMAL_QUEUE = "normal_queue";
    // 死信队列的名称
    public static final String DEAD_QUEUE = "dead_queue";

    public static void main(String[] args) throws Exception{
        Channel channel = RabbitMqUtils.getChannel();
        // 声明死信和普通交换机类型为direct
        channel.exchangeDeclare(NORMAL_EXCHANGE, BuiltinExchangeType.DIRECT);
        channel.exchangeDeclare(DEAD_EXCHANGE, BuiltinExchangeType.DIRECT);

        ////////////////////////////////////////
        // 声明普通队列
        Map<String, Object> arguments = new HashMap<>();
        // 过期时间
       // arguments.put("x-message-ttl", 10000);
        // 正常队列设置死信交换机
        arguments.put("x-dead-letter-exchange", DEAD_EXCHANGE);
        // 设置死信的RoutingKey
        arguments.put("x-dead-letter-routing-key", "lisi");
        // 设置正常队列的长度的限制
       // arguments.put("x-max-length", 6);
        channel.queueDeclare(NORMAL_QUEUE,false,false,false,arguments);

        ////////////////////////////////////////
        // 声明死信队列
        channel.queueDeclare(DEAD_QUEUE,false,false,false,null);

        // 绑定
        channel.queueBind(NORMAL_QUEUE,NORMAL_EXCHANGE,"zhangsan");
        channel.queueBind(DEAD_QUEUE,DEAD_EXCHANGE,"lisi");
        System.out.println("等待接收消息......");
        DeliverCallback deliverCallback = (consumerTag, message) ->{
            String msg = new String(message.getBody(), "UTF-8");
            if(msg.equals("info5")){
                System.out.println("Consumer01接收的消息是：" + msg + " :此消息是被拒绝的");
                channel.basicReject(message.getEnvelope().getDeliveryTag(),false);  // false 表示不放回原来队列，自然就会跑到死信队列
            }else {
                System.out.println("Consumer01接收的消息是：" + msg);
                channel.basicAck(message.getEnvelope().getDeliveryTag(),false);
            }
        };
        // 开启手动应答
        channel.basicConsume(NORMAL_QUEUE,false,deliverCallback,consumerTag ->{});
    }
}
