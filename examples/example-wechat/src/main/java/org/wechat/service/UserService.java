package org.wechat.service;

import java.util.*;

import javax.servlet.http.HttpSession;

import org.luban.common.Assert;
import org.wechat.bean.User;
import org.wechat.dao.Db;

public class UserService {

    // 用户注册
    public User register(HttpSession session, User newUser) {
        System.out.println("用户注册!!!");
        Assert.notNull(newUser, "参数 newUser不能为空");
        Assert.hasText(newUser.name, "参数 newUser.name不能为空");
        Assert.isNull(findUser(newUser.name), "该用户名已存在");
        // 生成id
        newUser.createTime = new Date();// 设置创建时间
        newUser.lastActiveTime=new Date();
        Db.users.add(newUser); // 创建成功
        session.setAttribute("user", newUser);// 保存到会话中
        return newUser;
    }

    // 用户登录
    public User login(String name, String password, HttpSession session) {
        User u = findUser(name);
        Assert.notNull(u, "用户名不存在");
        Assert.isTrue(u.password.equals(password), "密码错误");
        u.lastActiveTime=new Date();
        session.setAttribute("user", u);
        return u;
    }
    // 更新最后活跃时间
    public void heartbeat(HttpSession session){
        User u = (User) session.getAttribute("user");
        u.setLastActiveTime(new Date());
    }
    // 用户退出
    public void logout(HttpSession session) {
        Assert.notNull(session.getAttribute("user"), "用户未登录");
        session.removeAttribute("user");
    }

    //获取当前用户信息
    public User getUserInfo(HttpSession session) {
        return (User) session.getAttribute("user");
    }

    /**
     * 查找用户
     */
    public User findUser(String name) {
        return Db.users.stream()
                .filter(s -> s.name.equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

}
