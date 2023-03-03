package org.wechat.service;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import cn.hutool.core.bean.BeanUtil;
import org.wechat.bean.PrivateMessage;
import org.wechat.bean.User;
import org.wechat.dao.Db;
import org.wechat.service.bean.ChatFriend;
import org.wechat.service.bean.Friend;

import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * 好友服务
 *
 * @author 鲁班大叔
 * @date 2023
 */
public class FriendService implements Db {

    // 所有用户
    public List<Friend> allFriends(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return users.stream()
                .filter(u -> !u.getId().equals(user.getId()))// 过滤自己
                .map(u -> {
                    Friend friend = new Friend();
                    BeanUtil.copyProperties(u, friend);
                    friend.online = isOnline(u);
                    return friend;
                }).collect(Collectors.toList());
    }

    //当前用户 最近正在聊天的好友
    /*public List<ChatFriend> myChats(HttpSession session) {
        // 头像 名称 时间 内容
        User user = (User) session.getAttribute("user");
        //
        List<ChatFriend> results = chats.stream()
                .filter(c -> c.getUserId().equals(user.getId()))
                .map(c -> {
                    ChatFriend chatFriend = new ChatFriend();
                    BeanUtil.copyProperties(c, chatFriend);// 用户基本信息
                    User f = Db.getUser(c.friendId);
                    chatFriend.friendHead = f.head;
                    chatFriend.friendName = f.name;
                    chatFriend.online = isOnline(f);
                    return chatFriend;
                }).collect(Collectors.toList());

        return results;
    }*/
    public Friend getFriend(String friendId) {
        User u = Db.getUser(friendId);
        Friend friend = new Friend();
        BeanUtil.copyProperties(u, friend);
        return friend;
    }

    // 聊天的好友列表
    public List<ChatFriend> myChats(HttpSession session) {
        User user = (User) session.getAttribute("user");
        // 与我有关的消息 ，并基于好友id进行统计
        Map<String, List<PrivateMessage>> collect = privateMessages.stream()
                .filter(m -> m.myMsg(user.getId()))
                .collect(Collectors.groupingBy(m -> m.iSend(user.getId()) ? m.getToId() : m.getFromId()));

        List<ChatFriend> chatFriends = collect.entrySet().stream().map(e -> {
            String friendId = e.getKey();
            List<PrivateMessage> msgs = e.getValue();
            PrivateMessage lastMsg = msgs.get(msgs.size() - 1);
            ChatFriend chatFriend = _newChat(user, friendId);
            chatFriend.lastTime = lastMsg.createTime;
            chatFriend.lastMsg = lastMsg.message;
            chatFriend.unreadCount = (int) msgs.stream().filter(m -> m.sendMe(user.getId())).count();// 未读消息
            return chatFriend;
        }).collect(Collectors.toList());
        return chatFriends;
    }

    public ChatFriend newChat(HttpSession session, String friendId) {
        User user = (User) session.getAttribute("user");
        return _newChat(user, friendId);
    }

    private ChatFriend _newChat(User user, String friendId) {
        ChatFriend chatFriend = new ChatFriend();
        chatFriend.friendId = friendId;
        chatFriend.userId = user.getId();
        chatFriend.lastMsg = "";
        chatFriend.lastTime = new Date();
        User friend = Db.getUser(friendId);
        chatFriend.friendName = friend.name;
        chatFriend.online = isOnline(friend);
        chatFriend.friendHead = friend.head;
        return chatFriend;
    }

    // 用户是否在线
    private boolean isOnline(User u) {
        return u.getLastActiveTime() != null && System.currentTimeMillis() - u.getLastActiveTime().getTime() < 5 * 60 * 1000;
    }
}
