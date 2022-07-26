package com.atguigu.rabbitmq.four;

import com.atguigu.rabbitmq.utils.RabbitMqUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmCallback;

import java.util.UUID;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * 发布确认模式
 * 用使用的时间来比较哪种方式是最好的
 * 1、单个确认模式
 * 2、批量确认
 * 3、异步批量确认
 */
public class ConfirmMessage {

    // 批量发消息的个数
    public static final int MESSAGE_COUNT = 1000;

    public static void main(String[] args) throws Exception {
        // 1、单个确认
      //  ConfirmMessage.publishMessageIndividually();   // 发布1000个单独确认消息，耗时3576ms
        // 2、批量确认
      //  ConfirmMessage.publishMessageBatch();   // 发布1000个批量确认消息，耗时136ms
        // 3、异步批量确认
        ConfirmMessage.publishMessageAsync();   // 发布1000个异步发布确认消息，耗时93ms
    }


    // 单个确认
    public static void publishMessageIndividually() throws Exception {
        Channel channel = RabbitMqUtils.getChannel();
        // 队列的声明
        String queueName = UUID.randomUUID().toString();
        channel.queueDeclare(queueName, true, false, false, null);
        // 开启发布确认
        channel.confirmSelect();
        // 开始时间
        long begin = System.currentTimeMillis();

        // 批量发消息
        for (int i = 0; i < MESSAGE_COUNT; i++) {
            String message = i + "";
            channel.basicPublish("", queueName, null, message.getBytes());
            // 单个消息就马上进行发布确认
            boolean flag = channel.waitForConfirms();
            if (flag) {
                System.out.println("消息发送成功！");
            }
        }
        // 结束时间
        long end = System.currentTimeMillis();
        System.out.println("发布" + MESSAGE_COUNT + "个单独确认消息，耗时" + (end - begin) + "ms");
    }


    // 批量发布确认
    public static void publishMessageBatch() throws Exception {
        Channel channel = RabbitMqUtils.getChannel();
        // 队列的声明
        String queueName = UUID.randomUUID().toString();
        channel.queueDeclare(queueName, true, false, false, null);
        // 开启发布确认
        channel.confirmSelect();
        // 开始时间
        long begin = System.currentTimeMillis();

        // 批量确认消息大小
        int batchSize = 100;

        // 未确认消息个数
        // 批量发消息，批量确认
        for (int i = 0; i < MESSAGE_COUNT; i++) {
            String message = i + "";
            channel.basicPublish("", queueName, null, message.getBytes());

            // 判断达到100条消息时，批量确认一次
            if ((i + 1) % batchSize == 0) {
                // 确认
                channel.waitForConfirms();
            }
        }


        // 结束时间
        long end = System.currentTimeMillis();
        System.out.println("发布" + MESSAGE_COUNT + "个批量确认消息，耗时" + (end - begin) + "ms");
    }

    // 异步确认
    public static void publishMessageAsync() throws Exception{
        Channel channel = RabbitMqUtils.getChannel();
        // 队列的声明
        String queueName = UUID.randomUUID().toString();
        channel.queueDeclare(queueName, true, false, false, null);
        // 开启发布确认
        channel.confirmSelect();

        /**
         * 线程安全有序的一个哈希表，适用于高并发的情况下
         * 1、轻松的将序号与消息进行关联
         * 2、只要给到序号，轻松的批量删除条目
         * 3、支持高并发（多线程）
         */
        ConcurrentSkipListMap<Long,String> outstandingConfirms = new ConcurrentSkipListMap<>();
        // 消息确认成功回调函数
        ConfirmCallback ackCallback =  (deliveryTag, multiple) ->{
            if(multiple){
                // 2、删除掉已经确认的消息，剩下的就是未确认的消息
                ConcurrentNavigableMap<Long, String> confirmed = outstandingConfirms.headMap(deliveryTag);
                // 批量的话都确认过之后就全部清理掉
                confirmed.clear();
            }else {// 不是批量就单个删除
                outstandingConfirms.remove(deliveryTag);
            }
            System.out.println("确认的消息：" + deliveryTag);
        };

        // 消息确认失败回调函数
        /**
         * 1、deliveryTag消息的标记
         * 2、multiple 是否批量确认
         */
        ConfirmCallback nackCallback =  (deliveryTag, multiple) ->{
            // 3、打印一下未确认的消息都有哪些
            String message = outstandingConfirms.get(deliveryTag);
            System.out.println("未确认的消息是：" + message + "::::未确认的消息tag：" + deliveryTag);
        };
        // 准备消息的监听器，监听哪些消息成功了，哪些消息失败了
        /**
         * 1、监听哪些消息成功了
         * 2、监听哪些消息失败了
         */
        channel.addConfirmListener(ackCallback, nackCallback);  // 异步通知
        // 开始时间
        long begin = System.currentTimeMillis();
        // 批量发送消息
        for (int i = 0; i < MESSAGE_COUNT; i++) {
            String message = "消息" + i;
            channel.basicPublish("", queueName,null,message.getBytes());
            // 1、此处记录所有要发送的消息  的总和
            outstandingConfirms.put(channel.getNextPublishSeqNo(), message);
        }

        // 结束时间
        long end = System.currentTimeMillis();
        System.out.println("发布" + MESSAGE_COUNT + "个异步发布确认消息，耗时" + (end - begin) + "ms");
    }

}
