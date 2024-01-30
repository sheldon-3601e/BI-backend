package com.sheldon.springbootinit.webSocket;
 
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
 
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
 
/**
 * @ServerEndpoint 注解是一个类层次的注解，它的功能主要是将目前的类定义成一个websocket服务器端,
 * 注解的值将被用于监听用户连接的终端访问URL地址,客户端可以通过这个URL来连接到WebSocket服务器端
 */
//@Component
@ServerEndpoint("/websocket/{userId}")
@Slf4j
public class WebSocketService {
 
    private static ConcurrentHashMap<String, CopyOnWriteArraySet<WebSocketService>> userwebSocketMap = new ConcurrentHashMap<String, CopyOnWriteArraySet<WebSocketService>>();
 
    private static ConcurrentHashMap<String, Integer> count = new ConcurrentHashMap<String, Integer>();
 
    private String userId;
 
 
    /*
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    private Session session;
 
    /**
     * 连接建立成功调用的方法
     *
     * @param session 可选的参数。session为与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") final String userId) {
        this.session = session;
        this.userId = userId;
        log.info("websocket:client connected:{}", userId);
        if (!exitUser(userId)) {
            initUserInfo(userId);
        } else {
            CopyOnWriteArraySet<WebSocketService> webSocketServiceSet = getUserSocketSet(userId);
            webSocketServiceSet.add(this);
            userCountIncrease(userId);
        }
    }
 
 
    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        CopyOnWriteArraySet<WebSocketService> webSocketServiceSet = userwebSocketMap.get(userId);
        //从set中删除
        webSocketServiceSet.remove(this);
        //在线数减1
        userCountDecrement(userId);
        System.out.println("有一连接关闭！当前在线人数为" + getCurrUserCount(userId));
    }
 
    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     * @param session 可选的参数
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        CopyOnWriteArraySet<WebSocketService> webSocketSet = userwebSocketMap.get(userId);
        log.info("来自客户端" + userId + "的消息:" + message);
        /*//群发消息
        for (WebSocketTest item : webSocketSet) {
            try {
                item.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
        }*/
    }
 
 
    /**
     * 发生错误时调用
     *
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("发生错误");
        error.printStackTrace();
    }
 
 
 
    /**
     * 这个方法与上面几个方法不一样。没有用注解，是根据自己需要添加的方法。
     *
     * @param message
     * @throws IOException
     */
 
    public void sendMessage(String message) throws IOException {
        log.info("服务端推送" + userId + "的消息:" + message);
        this.session.getAsyncRemote().sendText(message);
    }
 
 
    /**
     * 这个方法与上面几个方法不一样。没有用注解，是根据自己需要添加的方法。   我是在有代办消息时 调用此接口 向指定用户发送消息
     *
     * @param message
     * @throws IOException
     */
    public void sendMessage(String userId,String message) throws IOException {
        log.info("服务端推送" + userId + "的消息:" + message);
        CopyOnWriteArraySet<WebSocketService> webSocketSet = userwebSocketMap.get(userId);
        //群发消息
        for (WebSocketService item : webSocketSet) {
            try {
                item.session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
        }
    }
 
 
    public boolean exitUser(String userId) {
        return userwebSocketMap.containsKey(userId);
    }
 
    public CopyOnWriteArraySet<WebSocketService> getUserSocketSet(String userId) {
        return userwebSocketMap.get(userId);
    }
 
    public void userCountIncrease(String userId) {
        if (count.containsKey(userId)) {
            count.put(userId, count.get(userId) + 1);
        }
    }
 
 
    public void userCountDecrement(String userId) {
        if (count.containsKey(userId)) {
            count.put(userId, count.get(userId) - 1);
        }
    }
 
    public void removeUserConunt(String userId) {
        count.remove(userId);
    }
 
    public Integer getCurrUserCount(String userId) {
        return count.get(userId);
    }
 
    private void initUserInfo(String userId) {
        CopyOnWriteArraySet<WebSocketService> webSocketServiceSet = new CopyOnWriteArraySet<WebSocketService>();
        webSocketServiceSet.add(this);
        userwebSocketMap.put(userId, webSocketServiceSet);
        count.put(userId, 1);
    }
 
}
 
 