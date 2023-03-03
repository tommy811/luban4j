package store;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import neo4j.bean.Label;
import neo4j.bean.User;
import neo4j.bean.UserAddress;

import java.util.List;
import  static org.luban.common.CollectionUtil.ListOf;
/**
 * @author 鲁班大叔
 * @date 2023
 */
public class BeanMock {
    public static User mockUser(String name) {
        User user = new User();
        user.setAge(18L);
        user.setName(name);
        user.setAddress(new UserAddress("胡同里:" + name, "长沙 "));
        user.setLabels(ListOf(new Label(name + "tag1"), new Label(name + "tag2")));
        user.setSex("男");
        return user;
    }
}
