package org.wechat.bean;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import org.luban.Listen;

import java.util.Date;

/**
 * 私发消息
 *
 * @author 鲁班大叔
 * @date 2023
 */
@Listen
public class PrivateMessage {
    private String id;
    public String fromId;
    public String toId;
    public String message;
    public Date createTime;
    public Boolean read; // 是否已读
    public String type;// 类型 text文本,img图片,tape语音

    public PrivateMessage(String fromId, String toId, String message) {
        this.fromId = fromId;
        this.toId = toId;
        this.message = message;
        this.createTime = new Date();
        this.read = false;
        this.type = "text";
    }

    public PrivateMessage() {
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

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    // 发给我的
    public boolean sendMe(String userId){
        return this.toId.equals(userId);
    }
    // 我发送的
    public boolean iSend(String userId){
        return this.fromId.equals(userId);
    }
    // 与我相关的消息
    public boolean myMsg(String userId){
        return  sendMe(userId)||iSend(userId);
    }
   public boolean dialogue(String userId1,String userId2){
       return myMsg(userId1) && myMsg(userId2);
   }
}
