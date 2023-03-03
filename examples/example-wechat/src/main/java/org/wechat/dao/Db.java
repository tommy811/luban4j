package org.wechat.dao;

import org.luban.store.Trust;
import org.wechat.bean.Chat;
import org.wechat.bean.PrivateMessage;
import org.wechat.bean.User;

import java.util.ArrayList;
import java.util.List;

@Trust
public interface Db{
  List<User> users=new ArrayList<>();
  List<PrivateMessage> privateMessages=new ArrayList<>();// 私发消息
  List<Chat> chats=new ArrayList<>();
  static User getUser(String userId){
    return users.stream().filter(u -> u.getId().equals(userId)).findFirst().orElseThrow(IllegalArgumentException::new);
  }
}
