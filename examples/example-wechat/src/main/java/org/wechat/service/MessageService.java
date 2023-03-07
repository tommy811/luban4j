package org.wechat.service;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */


import org.luban.common.Assert;
import org.luban.http.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wechat.bean.Group;
import org.wechat.bean.GroupMessage;
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

        // 2.实时推送给前端
        if (callbacks.containsKey(toId)) {
            callbacks.get(toId).call(msg);
        }
        return msg;
    }

    // 群发消息
    public GroupMessage sendGroupMsg(HttpSession session, String groupId, String message) {
        User user = (User) session.getAttribute("user");
        String fromId = user.getId();
        GroupMessage msg = new GroupMessage(fromId, groupId, message);
        msg.fromName=user.getName();
        msg.fromHead=user.head;
        groupMessages.add(msg);
        Group group = Db.getGroup(groupId);
        group.getUsers().stream()
                .map(s -> callbacks.get(s.getId()))
                .filter(Objects::nonNull)//找出所有在线的用户 并回调
                .forEach(c -> c.call(msg, "group"));
        return msg;
    }


    // 最近的聊天记录
    public List<PrivateMessage> lastHistory(HttpSession session, String friendId, int maxCount) {
        User user = (User) session.getAttribute("user");
        return privateMessages.stream()
                .filter(m -> m.dialogue(user.getId(), friendId))// 二者的对话
                .limit(maxCount)
                .collect(Collectors.toList());
    }
    // 最近的聊天记录
    public List<GroupMessage> lastHistoryByGroup(HttpSession session, String groupId, int maxCount) {
        User user = (User) session.getAttribute("user");
        Group group = Db.getGroup(groupId);
        Assert.isTrue(group.theMember(user.getId()),"用户未加入该群聊");
        return groupMessages.stream()
                .filter(m -> m.getToId().equals(groupId))
                .limit(maxCount)
                .collect(Collectors.toList());
    }

    // 最近的群聊记录
    public List<PrivateMessage> lastGroupHistory(HttpSession session, String friendId, int maxCount) {
        User user = (User) session.getAttribute("user");
        return privateMessages.stream()
                .filter(m -> m.dialogue(user.getId(), friendId))// 二者的对话
                .limit(maxCount)
                .collect(Collectors.toList());
    }


}
