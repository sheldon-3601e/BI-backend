package com.sheldon.springbootinit.bizmq;

/**
 * @ClassName BiMqConstant
 * @Author 26483
 * @Date 2024/1/26 17:10
 * @Version 1.0
 * @Description rabbitMQ 的相关常量
 */
public class BiMqConstant {

    /**
     * 交换机名称
     */
    public static final String EXCHANGE_NAME = "Bi_exchange";

    /**
     * 队列名称
     */
    public static final String QUEUE_NAME = "Bi_queue";

    /**
     * 路由key
     */
    public static final String ROUTING_KEY = "Bi_routingKey";

    /**
     * 交换机类型
     */
    public static final String EXCHANGE_TYPE = "direct";


}
