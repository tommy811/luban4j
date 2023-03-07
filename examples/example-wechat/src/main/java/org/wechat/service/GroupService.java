package org.wechat.service;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import org.luban.common.Assert;
import org.wechat.bean.Group;
import org.wechat.bean.GroupUser;
import org.wechat.bean.User;
import org.wechat.dao.Db;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 鲁班大叔
 * @date 2023
 */
public class GroupService implements Db {
    // 获取所有群聊
    public List<Group> allGroups() {
        return new ArrayList<>(groups); //构建新数据库
    }

    // 创建群聊
    public Group createGroup(HttpSession session,Group group) {
        User user = (User) session.getAttribute("user");
        group.setOwnerId(user.getId());
        groups.add(group);// 新增群聊
        return group;
    }

    // 加入群聊
    public Group join(HttpSession session, String groupId) {
        User user = (User) session.getAttribute("user");
        Group group = Db.getGroup(groupId);
        Assert.isTrue(group.getUsers().stream().noneMatch(s -> s.getId().equals(user.getId())), "用户已加入该群");
        group.getUsers().add(new GroupUser(user.getId()));// 加入新用户
        return group;
    }

    // 退出群聊
    public void quit(HttpSession session, String groupId) {
        User user = (User) session.getAttribute("user");
        Group group = Db.getGroup(groupId);
        GroupUser groupUser = group.getUsers().stream().filter(s -> s.getId().equals(user.getId())).findFirst().orElseThrow(() -> new IllegalArgumentException("用户未加入该群"));
        group.getUsers().remove(groupUser);// 删除用户
    }

    // 修改在本群中昵称
    public void editNickName(HttpSession session, String groupId, String newNickName) {
        User user = (User) session.getAttribute("user");
        Group group = Db.getGroup(groupId);
        GroupUser groupUser = group.getUsers().stream().filter(s -> s.getId().equals(user.getId())).findFirst().orElseThrow(() -> new IllegalArgumentException("用户未加入该群"));
        groupUser.setNickName(newNickName);
    }
    public List<Group> myGroups(HttpSession session){
        User user = (User) session.getAttribute("user");
        return groups.stream().filter(s -> s.theMember(user.getId())).collect(Collectors.toList());
    }
}
