package org.wechat.service;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */


import org.luban.http.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wechat.bean.PrivateMessage;
import org.wechat.bean.User;
import org.wechat.dao.Db;

import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 消息服务
 *
 * @author 鲁班大叔
 * @date 2023
 */
public class MessageService implements Db {
    static final Map<String, Callback> callbacks = new ConcurrentHashMap<>();
    static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    public void registMessageCall(String userId, Callback callback) {
        callbacks.put(userId, callback);
        // 连接断开时销毁
        callback.onDestroy(() -> callbacks.remove(userId));
    }

    // 私发消息
    public PrivateMessage sendPrivateMsg(HttpSession session, String toId, String message) {
        User user = (User) session.getAttribute("user");
        String fromId = user.getId();
        // 1.添加消息记录
        PrivateMessage msg = new PrivateMessage(fromId, toId, message);
        privateMessages.add(msg);
        // 2.更新聊天列表
       /* Chat chat = chats.stream().filter(c -> c.getUserId().equals(fromId) && c.getFriendId().equals(toId))
                .findFirst().orElse(new Chat());
        chat.setLastMsg(message);
        chat.setLastTime(new Date());
        if (chat.getId() == null) {
            chat.setUserId(fromId);
            chat.setFriendId(toId);
            chats.add(chat);
        }*/
        // 3.实时推送给前端
        if (callbacks.containsKey(toId)) {
            callbacks.get(toId).call(msg);
        }
        return msg;
    }


    // 最近的聊天记录
    public List<PrivateMessage> lastHistory(HttpSession session, String friendId, int maxCount) {
        User user = (User) session.getAttribute("user");
        return privateMessages.stream()
                .filter(m -> m.dialogue(user.getId(),friendId))// 二者的对话
                .limit(maxCount)
                .collect(Collectors.toList());
    }

}
