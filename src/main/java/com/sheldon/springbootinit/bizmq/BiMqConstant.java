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
     * 等待任务交换机名称
     */
    public static final String EXCHANGE_WAITING_NAME = "Bi_exchange_waiting";

    /**
     * 失败任务交换机名称
     */
    public static final String EXCHANGE_FAILED_NAME = "Bi_exchange_failed";

    /**
     * 等待任务队列名称
     */
    public static final String QUEUE_WAITING_NAME = "Bi_queue_waiting";

    /**
     * 成功任务队列名称
     */
    public static final String QUEUE_SUCCEED_NAME = "Bi_queue_succeed";

    /**
     * 失败任务队列名称
     */
    public static final String QUEUE_FAILED_NAME = "Bi_queue_failed";

    /**
     * 路由key
     */
    public static final String ROUTING_KEY_WAITING = "Bi_waiting_routingKey";
    /**
     * 路由key
     */
    public static final String ROUTING_KEY_FAILED = "Bi_failed_routingKey";

    /**
     * 交换机类型
     */
    public static final String EXCHANGE_TYPE = "direct";


}
