package org.luban.http.ws;

import org.luban.common.Assert;
import org.luban.common.UtilJson;
import org.luban.http.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/api/callback/{token}")
public class WebSocketHand {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketHand.class);
    private static final Map<String, WebSocketHand> connections = new ConcurrentHashMap<>();
    private Session session;
    private String token;
    private Set<Runnable> onDestroys = new HashSet<>();

    @OnOpen
    public void open(Session session) {
        this.session = session;
        String token = session.getPathParameters().get("token");
        this.token = token;
        connections.put(token, this);
    }

    @OnClose
    public void close() {
        connections.remove(token);
        onDestroys.forEach(Runnable::run);// 执行注销操作
    }

    @OnMessage
    public void message(String message) {
        String msg = String.format("%s %s %s", session.getId(), "said:", message);

    }

    public void send(String msg) {
        synchronized (session) {
            try {
                this.session.getBasicRemote().sendText(new String(msg.getBytes()));
            } catch (IOException e) {
                logger.error("消息发送失败", e);
            }
        }
    }

    @OnError
    public void onError(Throwable t) throws Throwable {
        logger.error("websocket异常", t);
        onDestroys.forEach(Runnable::run);// 执行注销操作
    }

    public static Callback buildCall(String token, String id) {
        Assert.isTrue(connections.containsKey(token), "未找到WebSocket连接处理器");
        WebSocketHand webSocketHand = connections.get(token);
        return webSocketHand.new WsCallback(id);
    }

    public class WsCallback implements Callback {
        final String id;

        private WsCallback(String id) {
            this.id = id;
        }

        @Override
        public void call(Object... args) {
            String jsonParam = UtilJson.writeValueAsString(args);
            // #Callback#id json
            send("#Callback#" + id + " " + jsonParam);
        }

        @Override
        public void onDestroy(Runnable runnable) {
            onDestroys.add(runnable);
        }
    }


}