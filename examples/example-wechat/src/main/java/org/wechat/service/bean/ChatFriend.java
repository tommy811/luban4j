package org.wechat.service.bean;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import java.io.Serializable;
import java.util.Date;

/**
 * 正在聊天的好友
 * @author 鲁班大叔
 * @date 2023
 */
public class ChatFriend {
    public String userId;
    public String friendId;
    public String lastMsg;
    public Date lastTime;
    public String friendName;
    public String friendHead;//  头像
    public int unreadCount;// 未读消息数量
    public boolean online;// 该好是否在线
}
