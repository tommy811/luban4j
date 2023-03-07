package org.wechat.bean;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import org.luban.Listen;

import java.util.Date;

/**
 * @author 鲁班大叔
 * @date 2023
 */
@Listen
public class GroupUser {
    String id;
    String nickName;
    Date createTime;// 加入时间

    public GroupUser(String id) {
        this.id = id;
        createTime = new Date();
    }

    public GroupUser() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

}