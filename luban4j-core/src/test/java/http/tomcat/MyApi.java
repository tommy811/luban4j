package http.tomcat;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import neo4j.bean.User;
import store.Db;

/**
 * @author 鲁班大叔
 * @date 2023
 */
public class MyApi implements Db {
    public String hello(String name) {
        return "hello " + name;
    }

    public void addUser(User user) {
        users.add(user);
    }

    public User findUser(String name) {
        return users.stream()
                .filter(s -> name.equals(s.getName()))
                .findFirst()
                .orElse(null);
    }

    public void removeUser(String id) {
        User user = users.stream()
                .filter(s -> id.equals(s.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("找不到用户"));
        users.remove(user);
    }
}
