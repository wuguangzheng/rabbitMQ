package com.atguigu.rabbitmq.springbootrabbitmq.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Date;

import static com.atguigu.rabbitmq.springbootrabbitmq.config.DelayedQueueConfig.DELAYED_QUEUE_NAME;

/**
 * 基于延迟交换机插件的消费者
 */
@Slf4j
@Component
public class DelayQueueConsumer {

    @RabbitListener(queues = DELAYED_QUEUE_NAME)
    public void receiveDelayedQueue(Message message){
        String msg = new String(message.getBody());
        log.info(" 当前时间：{}, 收到延时队列的消息：{}", new Date().toString(), msg);
    }
}
