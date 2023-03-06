package org.luban.store;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import org.luban.monitor.PropertyListenerSuport;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import static org.luban.common.CollectionUtil.MapOf;
/**
 * @author 鲁班大叔
 * @date 2023
 */
public class TrustSession {
    static final Logger logger = LoggerFactory.getLogger(TrustSession.class);
    Session neo4jSession;
    TrustSessionFactory.Staging staging;
    TrustSessionFactory sessionFactory;

    public TrustSession(Session neo4jSession, TrustSessionFactory sessionFactory) {
        this.neo4jSession = neo4jSession;
        this.sessionFactory = sessionFactory;
        TrustSessionFactory.Staging stage = new TrustSessionFactory.Staging();
        sessionFactory.registerStaging(stage);
        this.staging = stage;
    }


    public void close() {
        this.flush();
        sessionFactory.unregisterStaging(staging);// 取消注册，不在继续监听
        neo4jSession.clear();
    }

    public void flush() {
        // 实时保存 ，并且继续监听
        Transaction transaction = neo4jSession.beginTransaction();
        staging.changes.addAll(staging.rootAdds.values().stream().flatMap(Collection::stream).collect(Collectors.toSet()));// 添加到要保存的节点
        neo4jSession.save(staging.changes);// 修改对象与 新增对象一起保存
        // 刷新监听
        staging.changes.stream().filter(c->c instanceof PropertyListenerSuport).forEach(sessionFactory::listenObject);
        // 添加根节点
        staging.rootAdds.forEach((path, set) -> {
            for (Object object : set) {
                Object id = readId(object);
                // 创建root节点关联
                String uuid = UUID.randomUUID().toString();
                neo4jSession.query("MATCH (n) where n.id = $id \n" +
                                "create (r:Root{id:$uuid,path:$path}), \n" +
                                "(r)-[rs:SOURCE]->(n)  \n" +
                                "return r",
                        MapOf("id", id, "uuid", uuid, "path", path), false);
            }
        });

        // 删除根节点
        staging.rootRemoves.forEach((path, set) -> {
            for (Object object : set) {
                Object id = readId(object);
                String s = "MATCH p=(root:Root)-[r:SOURCE]->(u)" +
                        " where u.id=$id  delete root,r;";
                neo4jSession.query(s, MapOf("id",id), false);// 删除关系
                // 是否存在 被引用关系?
                Long i = neo4jSession.queryForObject(Long.class,
                        "match ()-[]->(n) where n.id=$id return count(n)",
                        MapOf("id",id));
                if (i == 0) {//不被引用即删除
                    neo4jSession.delete(object);
                    logger.debug("删除根节点:" + id);
                }
            }
        });
        transaction.commit();
        transaction.close();
    }

    public <T, ID extends Serializable> T load(Class<T> type, ID id) {
        return neo4jSession.load(type, id);
    }



    private Object readId(Object obj) {
        MetaData metaData = sessionFactory.neo4jSessionFactory.metaData();
        Function<Object, Optional<Object>> idReader = metaData.classInfo(obj).getPrimaryIndexOrIdReader();
        return idReader.apply(obj).orElseThrow(IllegalArgumentException::new);
    }

    private String readMainLabel(Object obj) {
        MetaData metaData = sessionFactory.neo4jSessionFactory.metaData();
        return metaData.classInfo(obj).neo4jName();
    }


}
