package org.wechat.bean;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import org.luban.Listen;

import java.util.ArrayList;
import java.util.List;

/**
 * 群聊消息
 *
 * @author 鲁班大叔
 * @date 2023
 */
@Listen
public class Group {
    String id;
    List<GroupUser> users = new ArrayList<>();// 所有用户
    String ownerId;  // 群主
    String name; // 群名
    String labels[]; // 标签
    String notice;// 群公告
    String groupHead;// 群头像

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<GroupUser> getUsers() {
        return users;
    }

    public void setUsers(List<GroupUser> users) {
        this.users = users;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }

    public String[] getLabels() {
        return labels;
    }

    public void setLabels(String[] labels) {
        this.labels = labels;
    }

    public String getGroupHead() {
        return groupHead;
    }

    public void setGroupHead(String groupHead) {
        this.groupHead = groupHead;
    }

    public boolean theMember(String userId){
        return users.stream().anyMatch(u -> u.getId().equals(userId));
    }


}
