package org.wechat.dao;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import org.junit.Before;
import org.junit.Test;
import org.luban.store.TrustSession;
import org.luban.store.TrustSessionFactory;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.SessionFactory;
import org.wechat.bean.User;

import java.util.Optional;

/**
 * @author 鲁班大叔
 * @date 2023
 */
public class DbTest {

    private TrustSessionFactory dbSessionFactory;

    @Before
    public void init() {
        Configuration dbConfiguration = new Configuration.Builder()
                .uri("http://127.0.0.1:7474/")
                .credentials("neo4j", "123456")
                .withBasePackages("org.wechat.bean")
                .build();
        SessionFactory neo4jSessionFactory = new SessionFactory(dbConfiguration);
        dbSessionFactory = new TrustSessionFactory(neo4jSessionFactory);
        dbSessionFactory.trust(Db.class);// 托管接口

    }

    @Test
    public void crudTest() {
        TrustSession trustSession = dbSessionFactory.openSession();

        User newUser = new User();
        newUser.setName("小明明");
        newUser.setSex("女");
        newUser.setLabels(new String[]{"红孩子", "疯狂","爱神"});
        Db.users.add(newUser);// 增
        // 查询
        User xm=null;
        for (User user : Db.users) {
            if (user.getName().equals("小明")) {
                xm=user;
                break;
            }
        }
        // 改
        xm.setSignDesc("爱我你抱抱我");
        // 删除
        Db.privateMessages.remove(0);

        trustSession.close();
    }
}
