package neo4j;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import neo4j.bean.*;
import org.junit.Test;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.metadata.schema.Schema;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import store.BeanMock;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

import static org.luban.common.CollectionUtil.ListOf;
import static org.luban.common.CollectionUtil.MapOf;
/**
 * @author 鲁班大叔
 * @date 2023
 */
public class OgmTest {
    @Test
    public void test() {
        //Set up the Session
        Configuration configuration = new Configuration.Builder().uri("file:///Users/tommy/git/code-classroom/luban4j/target/neo4jDb").build();
        SessionFactory sessionFactory = new SessionFactory(configuration, "neo4j.bean");
        Session session = sessionFactory.openSession();

        Actor tom = new Actor();
        tom.setFullName("汤姆汉克思");
        {
            Movie movie = new Movie("阿甘正传");
            Role ro = new Role();
            ro.setActor(tom);
            ro.setMovie(movie);
            ro.setTitle("阿甘");
            tom.getFilmography().add(ro);
            session.save(ro, 2);
        }
        {
            Movie movie2 = new Movie("绿里奇迹");
            Role ro = new Role();
            ro.setActor(tom);
            ro.setMovie(movie2);
            ro.setTitle("绿里");
            tom.getFilmography().add(ro);
            session.save(ro, 2);
        }
        Actor r = session.load(Actor.class, tom.getId());
        System.out.println(r.getFullName());
        for (Role role : r.getFilmography()) {
            System.out.println(role.getMovie().getName());
        }
    }

    protected SessionFactory conn() {
        Configuration configuration = new Configuration.Builder()
                .uri("bolt://127.0.0.1:7687")
                .strictQuerying()
                .credentials("neo4j", "123456")
                .build();
        long l = System.currentTimeMillis();
        SessionFactory sessionFactory = new SessionFactory(configuration, "neo4j.bean");
        System.out.println(System.currentTimeMillis() - l);
        return sessionFactory;
    }

    protected SessionFactory connByHttp() {
        Configuration configuration = new Configuration.Builder()
                .uri("http://127.0.0.1:7474")
                .strictQuerying()
                .credentials("neo4j", "123456")
                .build();
        long l = System.currentTimeMillis();
        SessionFactory sessionFactory = new SessionFactory(configuration, "neo4j.bean");
        System.out.println(System.currentTimeMillis() - l);
        return sessionFactory;
    }
    @Test
    public void testByHttp(){
        SessionFactory factory = connByHttp();
        Session session = factory.openSession();
        Actor tom = new Actor();
        tom.setFullName("汤姆汉克思");
        {
            Movie movie = new Movie("阿甘正传");
            Role ro = new Role();
            ro.setActor(tom);
            ro.setMovie(movie);
            ro.setTitle("阿甘");
            tom.getFilmography().add(ro);
            session.save(ro, 2);
        }
    }

    protected SessionFactory conn2() {
        Configuration configuration = new Configuration.Builder()
                .uri("bolt://127.0.0.1:7687")
                .credentials("neo4j", "123456")
                .strictQuerying()
                .build();
        long l = System.currentTimeMillis();
        SessionFactory sessionFactory = new SessionFactory(configuration, "neo4j.bean") {
        };
        System.out.println(System.currentTimeMillis() - l);
        return sessionFactory;
    }

    @Test
    public void testChangeInfo() throws NoSuchFieldException, IllegalAccessException {
        SessionFactory factor = conn();
        MetaData metaData = factor.metaData();

        ClassInfo classInfo = metaData.classInfo(User.class);
        String s = classInfo.neo4jName();
        metaData.classInfoByLabelOrType("User");

        Field neo4jName = ClassInfo.class.getDeclaredField("neo4jName");
        neo4jName.setAccessible(true);
        neo4jName.set(classInfo, s + "_proxy");
        User user = BeanMock.mockUser("小");
        user.setName("小明-女");
        Schema schema = metaData.getSchema();
        Field nodes = schema.getClass().getDeclaredField("nodes");
        nodes.setAccessible(true);
        Map nodesMap = (Map) nodes.get(schema);
        Object[] keys = nodesMap.keySet().toArray();
        for (Object key : keys) {
            nodesMap.put(key + "_proxy", nodesMap.get(key));
            nodesMap.remove(key);
        }

        Field staticLabels = ClassInfo.class.getDeclaredField("staticLabels");
        staticLabels.setAccessible(true);
        List<String> newLAbels = classInfo.staticLabels().stream().map(lab -> lab + "_proxy").collect(Collectors.toList());
        staticLabels.set(classInfo,  new ArrayList<>(newLAbels));

        Session session = factor.openSession();
        session.save(user);

        User load = session.load(User.class, user.getId());
        System.out.println(load);
    }


    @Test
    public void test2() {
        SessionFactory sessionFactory = conn();

        Session session = sessionFactory.openSession();
        // 标签+前缀
        // ID 用UUID

        for (int i = 0; i < 2; i++) {
            User user = new User();
            user.setName("小明-男");
            UserAddress address = new UserAddress();
            address.setAddress("朝阳路");
            address.setCity("北京");
            user.setAddress(address);

            user.setLabels(ListOf(new Label("妈宝"), new Label("正值")));
            user.setSex("男");
            user.setAge(18L);

            long l = System.currentTimeMillis();
            session.save(user);
            System.out.println(System.currentTimeMillis() - l);
            l = System.currentTimeMillis();
            User load = session.load(User.class, user.getId());
            System.out.println(System.currentTimeMillis() - l);
            System.out.println(load);
        }

    }


    @Test
    public void test3() {
        SessionFactory sessionFactory = conn();
        Session session = sessionFactory.openSession();
        User load = session.load(User.class, 22L);
        System.out.println(load);
    }

    @Test
    public void testPurged() {
        SessionFactory sessionFactory = conn();
        Session session = sessionFactory.openSession();
        session.purgeDatabase();
    }
    @Test
    public void test4() {
        SessionFactory sessionFactory = conn();
        Session session = sessionFactory.openSession();
        session.purgeDatabase();
        Label tag1 = new Label("妈宝3");
        Label tag2 = new Label("正值3");
        {
            User user = new User();
            user.setName("小花");
            user.setLabels(ListOf(tag1, tag2));
            user.setNeo4jLabels(ListOf("adf","ffbbb"));
            session.save(user);

        }
        {
            User user = new User();
            user.setName("小花");
            user.setLabels(ListOf(tag1, tag2));
            session.save(user);
        }
        session.loadAll(User.class);
    }

    @Test
    public void removeRel() {
        SessionFactory sessionFactory = conn();
        Session session = sessionFactory.openSession();
        User user = session.load(User.class, 14L);
        user.getLabels().remove(0);
        session.save(user);
    }

    @Test
    public void updateTest() {
        SessionFactory sessionFactory = conn();
        Session session = sessionFactory.openSession();
        User user = session.load(User.class, 14L);
        user.setName(user.getName() + "---1");
        Label label = user.getLabels().get(0);
        label.setName(label.getName() + "----2");
        session.save(user);
        User user2 = session.load(User.class, 14L);
        System.out.println(user.equals(user2));
    }

    @Test
    public void deleteNode() {
        SessionFactory sessionFactory = conn();
        Session session = sessionFactory.openSession();
        Label label = session.load(Label.class, 15l);
        session.delete(label);
    }

    @Test
    public void deleteNode2() {
        SessionFactory sessionFactory = conn();
        Session session = sessionFactory.openSession();
        User label = session.load(User.class, 11l);
        session.delete(label); // 里面包括的对象一并删除
        session.clear();

    }

    @Test
    public void testN() {
        SessionFactory conn = conn();
        Session session = conn.openSession();
        Fuck object = new Fuck();
//        object.setId(1l);

        object.setName("ffff");
        session.save(object);
        System.out.println(object.getId());
    }

    @Test
    public void testLazy() {
        SessionFactory conn = conn();
        Session session = conn.openSession();
        User user = session.load(User.class, 6L, 0);
//        session.loadAll()

        session.loadAll(User.class, new Filter("address", ComparisonOperator.EQUALS, ""));
        System.out.println(user);
    }

    @Test
    public void testAddRoot() {
        SessionFactory conn = conn();
        Session session = conn.openSession();
        session.purgeDatabase();// 清空数据库
        Label tag1 = new Label("妈宝3");
        Label tag2 = new Label("正值3");
        Label tag3 = new Label("正值4");
        User user = new User();
        user.setName("小花");
        user.setLabels(ListOf(tag1, tag2));
        session.save(user);
        User user2 = new User();
        user2.setName("小花2");
        user2.setLabels(new ArrayList<>(ListOf(tag1, tag2, tag3)));
        session.save(user2);

        for (Object id : ListOf(user.getId(), user2.getId())) {
            String uuid = UUID.randomUUID().toString();
            Result r = session.query("MATCH (n:User) where n.id = $id\n" +
                            "create (r:Root{id:$uuid,path:$path})  ,\n" +
                            "(r)-[rs:SOURCE]->(n)  \n" +
                            "return r",
                    MapOf("id", id, "uuid", uuid, "path", "store.Db.users"),
                    false);
            System.out.println(r.queryStatistics());
        }
        for (Object id : ListOf(tag1.getId(), tag2.getId(), tag3.getId())) {
            String uuid = UUID.randomUUID().toString();
            Result r = session.query("MATCH (n:Label) where n.id = $id\n" +
                            "create (r:Root{id:$uuid,path:$path})  ,\n" +
                            "(r)-[rs:SOURCE]->(n)  \n" +
                            "return r",
                    MapOf("id", id, "uuid", uuid, "path", "store.Db.labels"),
                    false);
            System.out.println(r.queryStatistics());
        }
        user2 = session.load(User.class, user2.getId());
        user2.getLabels().remove(tag1);
        user2.getLabels().remove(tag3);// 被引用无法删除
        session.save(user2);
    }

    @Test
    public void testRemoveRoot() {
        SessionFactory conn = conn();
        Session session = conn.openSession();
        Collection<User> users = session.loadAll(User.class);
        List<User> list = new ArrayList<>(users);
        User user = list.get(0);
        String s = "MATCH p=(root:Root)-[r:SOURCE]->(u:User)" +
                " where u.id=$id  delete root,r;";
        session.query(s, MapOf("id", user.getId()), false);// 删除关系
        //
        Long i = session.queryForObject(Long.class, "match ()-[]->(n:User) where n.id=$id return count(n)", MapOf("id", user.getId()));
        if (i == 0) {
            session.delete( session.load(User.class,user.getId()));
        }
    }

    @Test
    public void testLoad() {
        SessionFactory conn = conn();
        Session session = conn.openSession();
        Class<? extends Object> itemType = User.class;
        Iterable<?> label = session.query(itemType, String.format("MATCH p=(ro:Root)-[r:SOURCE]->(u:%s) RETURN u;", itemType.getSimpleName()), MapOf());
        System.out.println(label.iterator().hasNext());

    }

    @Test
    public void testUuid() {
        SessionFactory conn = conn();
        Session session = conn.openSession();
        session.purgeDatabase();
        City city = new City();
        city.setName("北京");
        city.setCoord(111L);
        session.save(city);
        City load = session.load(City.class, city.getId());
//        session.delete(city);
    }

    // id批量加载
    @Test
    public void testLoadBatch() {
        SessionFactory conn = conn();
        Session session = conn.openSession();
        Class<? extends Object> itemType = User.class;
        Iterable<?> label = session.query(itemType,
                "MATCH (n:User) where ID(n) IN $ids return n;", MapOf("ids", ListOf(53L, 45L, 15L)));
        System.out.println(label.iterator().hasNext());
    }


    static class fffff {
        private List<String> list;
    }

    public static void main(String[] args) throws NoSuchFieldException {
        Field f = fffff.class.getDeclaredField("list");
        f.getDeclaringClass();
        f.getGenericType();// 获取泛型信息
        Type actualTypeArgument = ((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0];

    }
}





