package store;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import neo4j.bean.Fuck;
import neo4j.bean.Label;
import neo4j.bean.User;
import org.junit.Test;
import org.luban.common.ReflectionUtils;
import org.luban.common.UtilJson;
import org.luban.store.TrustSession;
import org.luban.store.TrustSessionFactory;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.id.UuidStrategy;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.typeconversion.ConversionCallback;

import java.util.ArrayList;
import java.util.Date;

import static org.luban.common.CollectionUtil.ListOf;
/**
 * @author 鲁班大叔
 * @date 2023
 */
public class TrustSessionFactoryTest {
    protected SessionFactory connBolt() {
        Configuration configuration = new Configuration.Builder()
//                .uri("bolt://127.0.0.1:7687")
                .uri("http://127.0.0.1:7474")
                .credentials("neo4j", "123456")
                .build();
        SessionFactory sessionFactory = new SessionFactory(configuration, "neo4j.bean");
        return sessionFactory;
    }

    @Test
    public void openSessionTest() {
//        System.setProperty("db.key","abc123");
        TrustSessionFactory sessionFactory = new TrustSessionFactory(connBolt());
        TrustSession trustSession = sessionFactory.openSession();
        User user = trustSession.load(User.class, "8fe1433f-9377-4ea7-8cf7-08de4e76a145");
        System.out.println(UtilJson.writeValueAsString(user));
        trustSession.close();// 关闭会话
    }

    @Test
    public void trustTest() {
        TrustSessionFactory sessionFactory = new TrustSessionFactory(connBolt());
        sessionFactory.trust(Db.class);
        assert !Db.users.isEmpty();
    }

    @Test
    public void testId() {
        SessionFactory factory = connBolt();
        ClassInfo classInfo = factory.metaData().classInfo(Fuck.class);
        if (!classInfo.hasIdentityField() && !classInfo.hasPrimaryIndexField()) {
            // 使用String id
            classInfo.fieldsInfo().fields().stream()
                    .filter(f -> "id".equals(f.getName()))
                    .filter(f -> "java.lang.String".equals(f.getTypeDescriptor()))
                    .findFirst().ifPresent(f -> {
                ReflectionUtils.setValue("primaryIndexField", f, classInfo);
                if (classInfo.idStrategyClass() == null) {
                    ReflectionUtils.setValue("idStrategyClass", UuidStrategy.class, classInfo);
                    classInfo.registerIdGenerationStrategy(new UuidStrategy());
                }
            });
        }
        Session session = factory.openSession();
        Fuck u = session.load(Fuck.class, "a3a43847-7638-49b6-a83c-6b9d358e9a8e");
        classInfo.idStrategyClass();
        Fuck fuck = new Fuck();
        fuck.setName("the ffff");

        session.save(fuck);
    }

    @Test
    public void changeTest() {
        TrustSessionFactory sessionFactory = new TrustSessionFactory(connBolt());
        sessionFactory.trust(Db.class);
        TrustSession session = sessionFactory.openSession();
        User user = Db.users.get(0);
        user.getAddress().setCity("纽约");
        ArrayList<Label> newLables = new ArrayList<>(user.getLabels());
        user.setLabels(newLables);// 不代理
        newLables.add(new Label("皮带"));// 皮带被保存
        Label newLable = new Label("裤子");
        newLables.add(newLable);// 裤子被保存
        session.close();
        System.out.println(user);

    }

    @Test
    public void addRootTest() {
        TrustSessionFactory sessionFactory = new TrustSessionFactory(connBolt());
        sessionFactory.trust(Db.class);
        TrustSession session = sessionFactory.openSession();
        Label label = Db.labels.get(0);
        label.setName("小可爱");
        Db.labels.add(new Label("小明"));
        Db.users.remove(0);
        session.close();
    }

    @Test
    public void addRootTest2() {
        System.setProperty("db.key", "fff_");
        SessionFactory factory = connBolt();
        factory.metaData().registerConversionCallback(new ConversionCallback() {
            @Override
            public <T> T convert(Class<T> targetType, Object value) {
                return (T) value;
            }
        });
        factory.openSession().purgeDatabase();
        SessionFactory neo4jSessionFactory = factory;
        TrustSessionFactory sessionFactory = new TrustSessionFactory(neo4jSessionFactory);
        sessionFactory.trust(Db.class);
        TrustSession session = sessionFactory.openSession();
        User xiaom = BeanMock.mockUser("小明");
        xiaom.setTheFloat(33.12f);
        Db.users.add(xiaom);
        Db.users.add(BeanMock.mockUser("小强"));
        session.close();
        factory.openSession().loadAll(User.class);
    }

    @Test
    public void removeRootTest() {
        TrustSessionFactory sessionFactory = new TrustSessionFactory(connBolt());
        sessionFactory.trust(Db.class);
        TrustSession session = sessionFactory.openSession();
        Label label = Db.labels.get(0);
        label.setName("小可爱");
        Db.labels.add(new Label("小明"));
        Db.users.remove(0);
        session.close();
    }

    @Test
    public void typeTest() {
        SessionFactory neo4jSessionFactory = connBolt();
        /*NormalConversion conversion = new NormalConversion();
        neo4jSessionFactory.metaData().persistentEntities().stream()
                .flatMap(c -> c.fieldsInfo().fields().stream())
                .forEach(f -> {
                    Class<?> entityAttributeType = DescriptorMappings.getType(f.getTypeDescriptor());
                    if (!f.hasPropertyConverter() && !f.hasCompositeConverter() && conversion.isSupported(entityAttributeType)) {
                        f.setPropertyConverter(new ProxyAttributeConverter(entityAttributeType, entityAttributeType,
                                conversion));
                    }
                });*/
        neo4jSessionFactory.openSession().purgeDatabase();
        TrustSessionFactory sessionFactory = new TrustSessionFactory(neo4jSessionFactory);
        sessionFactory.trust(Db.class);
        TrustSession session = sessionFactory.openSession();
        User xiaohua = BeanMock.mockUser("小花");
        xiaohua.setCount(1999);
        xiaohua.setTime(new Date());
        xiaohua.setC('男');
        xiaohua.setTheFloat(3.14f);
        xiaohua.setTheDouble(44.4);
        xiaohua.setTheDoubles(ListOf(1.33, 333.1123));
        xiaohua.setTheBoolean(true);
        xiaohua.setTheShort((short) 11);
        xiaohua.setTheByte((byte) 1);
        xiaohua.setTheFloats(new Float[]{1.1f,2f,3f});
        Db.users.add(xiaohua);
        session.close();
        User load = session.load(User.class, xiaohua.getId());

        Double aDouble = load.getTheDoubles().get(0);
        System.out.println(aDouble);
    }

    @Test
    public void testBeanInfoTest() {
        SessionFactory neo4jSessionFactory = connBolt();
        ClassInfo classInfo = neo4jSessionFactory.metaData().classInfo(User.class);
        classInfo.propertyFields();
    }
}
