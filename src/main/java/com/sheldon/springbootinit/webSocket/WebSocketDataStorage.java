package com.sheldon.springbootinit.webSocket;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class WebSocketDataStorage {

    private ConcurrentHashMap<String, CopyOnWriteArraySet<WebSocketService>> userwebSocketMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Integer> count = new ConcurrentHashMap<>();

    // 添加其他方法来操作数据

    public ConcurrentHashMap<String, CopyOnWriteArraySet<WebSocketService>> getUserwebSocketMap() {
        return userwebSocketMap;
    }

    public ConcurrentHashMap<String, Integer> getCount() {
        return count;
    }
}
