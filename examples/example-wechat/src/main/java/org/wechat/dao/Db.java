package org.wechat.dao;

import org.luban.store.Trust;
import org.wechat.bean.*;

import java.util.ArrayList;
import java.util.List;

@Trust
public interface Db{
  List<User> users=new ArrayList<>();
  List<PrivateMessage> privateMessages=new ArrayList<>();// 私发消息
  List<GroupMessage> groupMessages=new ArrayList<>();// 群聊消息
  List<Group> groups=new ArrayList<>();// 群聊

  static User getUser(String userId){
    return users.stream().filter(u -> u.getId().equals(userId)).findFirst().orElseThrow(IllegalArgumentException::new);
  }
  static Group getGroup(String groupId){
    return groups.stream().filter(u -> u.getId().equals(groupId)).findFirst().orElseThrow(IllegalArgumentException::new);
  }
}
