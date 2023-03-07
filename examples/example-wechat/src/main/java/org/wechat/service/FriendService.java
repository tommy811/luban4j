package org.wechat.service;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import cn.hutool.core.bean.BeanUtil;
import org.luban.common.Assert;
import org.wechat.bean.Group;
import org.wechat.bean.GroupMessage;
import org.wechat.bean.PrivateMessage;
import org.wechat.bean.User;
import org.wechat.dao.Db;
import org.wechat.service.bean.ChatItem;
import org.wechat.service.bean.Friend;

import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;
import java.util.Map;
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


    public Friend getFriend(String friendId) {
        User u = Db.getUser(friendId);
        Friend friend = new Friend();
        BeanUtil.copyProperties(u, friend);
        return friend;
    }

    // 获取群成员
    public List<Friend> getGroupMembers(HttpSession session ,String groupId){
        User user = (User) session.getAttribute("user");
        Group group = Db.getGroup(groupId);
        Assert.isTrue(group.theMember(user.getId()),"用户未加入该群聊");
        List<Friend> result = group.getUsers().stream().map(gu -> {
            Friend friend = new Friend();
            User u = Db.getUser(gu.getId());
            BeanUtil.copyProperties(u, friend);
            if (gu.getNickName()!=null) {
                friend.name = gu.getNickName();
            }
            friend.online=isOnline(u);
            return friend;
        }).collect(Collectors.toList());
        return result;
    }

    // 聊天的好友列表
    public List<ChatItem> myChats(HttpSession session) {
        User user = (User) session.getAttribute("user");
        // 与我有关的私发消息 ，并基于好友id进行统计
        Map<String, List<PrivateMessage>> collect = privateMessages.stream()
                .filter(m -> m.myMsg(user.getId()))
                .collect(Collectors.groupingBy(m -> m.iSend(user.getId()) ? m.getToId() : m.getFromId()));

        List<ChatItem> chatList = collect.entrySet().stream().map(e -> {
            String friendId = e.getKey();
            List<PrivateMessage> msgs = e.getValue();
            PrivateMessage lastMsg = msgs.get(msgs.size() - 1);
            ChatItem chatFriend = _newChat(user, friendId);
            chatFriend.lastTime = lastMsg.createTime;
            chatFriend.lastMsg = lastMsg.message;
            chatFriend.unreadCount = (int) msgs.stream().filter(m -> m.sendMe(user.getId())).count();// 未读消息
            return chatFriend;
        }).collect(Collectors.toList());

        // 与我有关的群消息
        Map<String, Group> myGroup = groups.stream().filter(g -> g.theMember(user.getId())).collect(Collectors.toMap(Group::getId, v -> v));
        Map<String, List<GroupMessage>> collect1 = groupMessages.stream().filter(m -> myGroup.containsKey(m.getToId())).collect(Collectors.groupingBy(GroupMessage::getToId));
        List<ChatItem> chatList2 = collect1.entrySet().stream().map(e -> {
            ChatItem chatItem = new ChatItem();
            chatItem.groupId = e.getKey();
            Group group = myGroup.get(e.getKey());
            chatItem.groupName = group.getName();
            chatItem.groupHead = group.getGroupHead();
            List<GroupMessage> msgs = e.getValue();
            GroupMessage lastMsg = msgs.get(msgs.size() - 1);
            chatItem.lastTime = lastMsg.createTime;
            chatItem.lastMsg = lastMsg.message;
            return chatItem;
        }).collect(Collectors.toList());

        chatList.addAll(chatList2);
        chatList.sort((o1, o2) -> o1.lastTime.before(o2.lastTime) ? 1 : -1);
        return chatList;
    }


    public ChatItem newChat(HttpSession session, String friendId) {
        User user = (User) session.getAttribute("user");
        return _newChat(user, friendId);
    }

    public ChatItem newGroupChat(HttpSession session, String groupId) {
        User user = (User) session.getAttribute("user");
        ChatItem chatFriend = new ChatItem();
        chatFriend.userId = user.getId();
        chatFriend.lastTime = new Date();
        chatFriend.groupId = groupId;
        Group group = Db.getGroup(groupId);
        chatFriend.groupName = group.getName();
        chatFriend.groupHead = group.getGroupHead();
        return chatFriend;
    }

    private ChatItem _newChat(User user, String friendId) {
        ChatItem chatFriend = new ChatItem();
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
