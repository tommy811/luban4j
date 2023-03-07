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
public class GroupMessage {
    private String id;
    private String fromId;// 发送用户
    public String fromName;// 发送用户名称
    public String fromHead;// 发送用户头像
    private String toId;// 发送到群
    public String message;
    public Date createTime;
    public String type;// 类型 text文本,img图片,tape语音

    public GroupMessage(String fromId, String toId, String message) {
        this.fromId = fromId;
        this.toId = toId;
        this.message = message;
        this.createTime = new Date();
        this.type = "text";
    }

    public GroupMessage() {// 空
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public String getToId() {
        return toId;
    }

    public void setToId(String toId) {
        this.toId = toId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getFromHead() {
        return fromHead;
    }

    public void setFromHead(String fromHead) {
        this.fromHead = fromHead;
    }
}
