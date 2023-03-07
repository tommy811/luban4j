package org.luban.store;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import org.luban.common.Assert;
import org.luban.common.ReflectionUtils;
import org.luban.monitor.PropertyListener;
import org.luban.monitor.PropertyListenerSuport;
import org.neo4j.ogm.id.UuidStrategy;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.luban.common.CollectionUtil.MapOf;
import static org.luban.store.TypeConverter.CONVERTERS;
/**
 * 数据托管
 *
 * @author 鲁班大叔
 * @date 2023
 */
public class TrustSessionFactory {
    static final Logger logger = LoggerFactory.getLogger(TrustSession.class);
    SessionFactory neo4jSessionFactory;
    // 当前线程暂存空间
    private ThreadLocal<Staging> currentStaging = new ThreadLocal<>();
    private Staging jobStaging = null;
    private Map<Class, Date> trusts = new ConcurrentHashMap<>();

    public TrustSessionFactory(SessionFactory neo4jSessionFactory) {
        this.neo4jSessionFactory = neo4jSessionFactory;
        registerPrimaryIndexField(neo4jSessionFactory);// 注册id主键 ，自动将String id  转为主键
        registerNormalConvert(neo4jSessionFactory);// 注册通用转换器
    }

    //注册通用类型转换器
    private void registerNormalConvert(SessionFactory neo4jSessionFactory) {
        neo4jSessionFactory.metaData().persistentEntities().stream()
                .flatMap(c -> c.fieldsInfo().fields().stream())
                .filter(f -> !f.hasPropertyConverter() && !f.hasCompositeConverter())
                .forEach(f -> {
                    String key = "";
                    key += f.isIterable() ? f.getCollectionClassname() + "/" : "";
                    key += f.getTypeDescriptor();
                    if (CONVERTERS.containsKey(key)) {
                        f.setPropertyConverter(CONVERTERS.get(key));
                    }
                });
    }


  /*  public TrustSessionFactory(SessionFactory neo4jSessionFactory, String labelPrefix) {
        this.neo4jSessionFactory = neo4jSessionFactory;
        this.labelPrefix = labelPrefix;
        *//*
        if (StringUtils.hasText(labelPrefix)) {
            changeLabel(labelPrefix);
        }
        *//*
    }*/

    public Staging getStaging() {
        if (currentStaging.get() != null) {
            return currentStaging.get();
        } else if (jobStaging != null) {
            return jobStaging;
        } else {
            throw new IllegalStateException("不存在 Staging 暂存修改数据");
        }
    }

   /* // 为节点的label 添加前缀
    private void changeLabel(String labelPrefix) {
        MetaData metaData = neo4jSessionFactory.metaData();
        if (metaData.persistentEntities().stream().anyMatch(c -> c.neo4jName().startsWith(labelPrefix))) {
            return; //前缀已添加
        }
        metaData.persistentEntities().stream().forEach(c -> {
            var f = ReflectionUtils.findField(ClassInfo.class, "neo4jName");
            ReflectionUtils.setField(f, c, labelPrefix  + c.neo4jName());
            ReflectionUtils.setValue("staticLabels", null, c);// 置空静态标签
        });
        DomainInfo domainInfo= (DomainInfo) ReflectionUtils.getValue("domainInfo",metaData);
        Schema newSchema = new DomainInfoSchemaBuilder(domainInfo).build();
        ReflectionUtils.setFinalVal("schema",newSchema,metaData);
        System.out.println(metaData);
    }*/

    private void registerPrimaryIndexField(SessionFactory neo4jSessionFactory) {
        for (ClassInfo classInfo : neo4jSessionFactory.metaData().persistentEntities()) {
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
        }
    }

    // 注册暂存空间
    public void registerStaging(Staging stage) {
        if(currentStaging.get()!=null){
          logger.warn("会话未正常关闭：",new IllegalStateException("将造成数据丢失"));
        }
//        Assert.isTrue(currentStaging.get() == null);
        currentStaging.set(stage);
    }

    // 注销暂存空间
    public void unregisterStaging(Staging stage) {
        Assert.isTrue(currentStaging.get() == stage);
        currentStaging.remove();
    }

    public TrustSession openSession() {
        TrustSession storeSession = new TrustSession(newSession(), this);
        return storeSession;
    }


    // 托管类
    public void trust(Class trustClass) {
        Assert.isTrue(trustClass.isInterface(), "trust(托管)类只能是接口(interface)：请检查：" + trustClass.toString());
        logger.info("trust: " + trustClass);
        trusts.put(trustClass, new Date());
        Field[] declaredFields = trustClass.getDeclaredFields();
        Session session = newSession();
        // 代理所有List静态属性
        Arrays.stream(declaredFields)
                .filter(f -> Modifier.isStatic(f.getModifiers()))
                .filter(f -> Modifier.isFinal(f.getModifiers()))
                .filter(f -> f.getType().isAssignableFrom(List.class))
                .peek(f -> load(f, session))// 加载数据  //TODO 懒加载实现
                .map(f -> proxyList(null, f,true))// 代理list
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .filter(o -> o instanceof PropertyListenerSuport)//监听list中元素
                .forEach(this::listenObject);
    }

    private Session newSession() {
        Session session = neo4jSessionFactory.openSession();
        // 创建对象后监听
       /* Class<? extends Session> aClass = session.getClass();
        try {
            Field insFiled = aClass.getDeclaredField("entityInstantiator");
            insFiled.setAccessible(true);
            EntityInstantiator entityInstantiator = (EntityInstantiator) insFiled.get(session);
            EntityInstantiator proxyIns = new EntityInstantiator() {
                @Override
                public <T> T createInstance(Class<T> clazz, Map<String, Object> propertyValues) {
                    T instance = entityInstantiator.createInstance(clazz, propertyValues);
                    if (instance instanceof PropertyListenerSuport) {
                        listenObject(instance);// 添加监听
                    }
                    return instance;
                }
            };
            insFiled.set(session, proxyIns);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }*/

        return session;
    }

    private void load(Field file, Session session) {
        Assert.isTrue(file.getGenericType() instanceof ParameterizedType, "必须指定泛型：" + file.getName());
        Class<?> itemType = (Class) ((ParameterizedType)
                file.getGenericType()).getActualTypeArguments()[0];
        String label = neo4jSessionFactory.metaData().classInfo(itemType).neo4jName();
        long begin = System.currentTimeMillis();
        final String path = file.getDeclaringClass().getName() + "." + file.getName();

        Iterable<String> result = session.query(String.class,
                String.format("MATCH p=(ro:Root)-[r:SOURCE]->(u:%s) where ro.path=$path RETURN u.id;", label),
                MapOf("path", path));

        Iterator<String> iterator = result.iterator();
        ArrayList<String> ids = new ArrayList<>();
        while (iterator.hasNext()) {
            ids.add(iterator.next());
        }
        List<?> list = new ArrayList<>(session.loadAll(itemType, ids));// 基于ID加载全部数据
        long end = System.currentTimeMillis();
        logger.info(String.format("加载:%s 总数:%s 用时(ms):%s", path, list.size(), end - begin));
        file.setAccessible(true);
        try {
            writeFinalVal(null, file, list);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }


    // 代理托管对象
    private ListenList<?> proxyList(final Object source, Field f,final boolean isRoot) {
        Assert.isTrue(f.getType().isAssignableFrom(List.class));
        Class trustClass = f.getDeclaringClass();
        f.setAccessible(true);
        List<?> oldVal;
        try {
            oldVal = (List) f.get(source);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        if (oldVal == null) {
            return null;
        }
        final String path = trustClass.getName() + "." + f.getName();
        ListenList<?> newList = new ListenList<>(oldVal, new ListChangeListener() {
            @Override
            public void addItem(Object item) {
                if (item != null) {
                    if (isRoot) {
                        getStaging().rootAdd(path, item);
                    }else{
                        getStaging().addChange(source);// 改变源
                    }
//                  listenObject(item);
                }
            }

            @Override
            public void removeItem(Object item) {
                getStaging().rootRemove(path, item);
            }
        });
        try {
//            f.set(trustClass, newList);
            writeFinalVal(source, f, newList);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        return newList;
    }

    // 监听对象变化
    public void listenObject(Object obj) {
        Assert.isTrue(obj instanceof PropertyListenerSuport, "托管对象必须添加@Listen 注解");
        PropertyListenerSuport l = (PropertyListenerSuport) obj;
        ClassInfo classInfo = neo4jSessionFactory.metaData().classInfo(obj);
        if (l.currentPropertyListener() == PropertyListener.EMPTY) {
            //TODO oldVal 未实现
            l.insertPropertyListener((oldVla, newVal, source, name) -> {
                // 添加到暂存空间
                getStaging().addChange(source);
                // 只有保存之后才会添加监听
                /*
                if (newVal instanceof PropertyListenerSuport) {
                    listenObject(newVal);
                } else if (newVal instanceof List && !(newVal instanceof ListenList)) { // 新添加的List
                    // 设置代理
                    classInfo.propertyField(name);
                    classInfo.findIterableFields()
                            .stream()
                            .filter(f -> f.isTypeOf(List.class) && !ListenList.class.isAssignableFrom(f.getField().getType()))
                            .filter(f -> f.getName().equals(name))
                            .flatMap(f -> proxyList(source, f.getField()).stream())//代理list
                            .filter(o -> o instanceof PropertyListenerSuport)//继续监听list中元素
                            .forEach(this::listenObject);
                }
                */
            });
        }

        // 递归监听子对象 包括list中的对象
        classInfo.fieldsInfo()
                .fields()
                .stream()
                .filter(f -> PropertyListenerSuport.class.isAssignableFrom(f.type()))
                .map(f -> f.read(obj))
                .filter(Objects::nonNull)
                .forEach(this::listenObject);


        // 监听 所有list字段
        classInfo.findIterableFields()
                .stream()
                .filter(f -> f.isTypeOf(List.class) && !ListenList.class.isAssignableFrom(f.getField().getType()))
                .map(FieldInfo::getField)
                .map(f -> proxyList(obj, f,false))
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .filter(o -> o instanceof PropertyListenerSuport)
                .forEach(this::listenObject);//

        /*
         * 监听 所有list字段
         * TODO 缓存字段Field
         */
       /* Set<Property> properties = propertyUtils.getProperties(obj.getClass());
        Predicate<Field> isBeanField = f -> properties.stream()
                .anyMatch(s -> s.getName().equals(f.getName())
                        && s.isReadable()
                        && s.isWritable());
        ClassUtil.getAllFields(obj.getClass())
                .stream()
                .filter(f -> f.getType().isAssignableFrom(List.class))
                .filter(f -> !f.getType().isAssignableFrom(ListenList.class))
                .filter(f -> !Modifier.isTransient(f.getModifiers()))
                .filter(f -> !Modifier.isStatic(f.getModifiers()))
                .filter(isBeanField)
                .forEach(f -> proxyList(obj, f));*/
    }


    /**
     * 当前线程 区间内的变化。
     * 由当前会话获取。并消费
     * 如果当前没有会话，则由定时任务消费
     */
    public static class Staging {
        final Set<Object> changes = new HashSet<>();
        final Map<String, Set<Object>> rootAdds = new HashMap<>();
        final Map<String, Set<Object>> rootRemoves = new HashMap<>();

        // 添加对象
        private void addChange(Object obj) {
            if (changes.stream().anyMatch(c -> c == obj)) {
                return;
            }
            changes.add(obj);
        }

        private void rootAdd(String path, Object obj) {
            Set sets = rootAdds.computeIfAbsent(path, k -> new HashSet<>());
            sets.add(obj);
            if (rootRemoves.computeIfAbsent(path, k -> new HashSet<>()).remove(obj)) {
                logger.warn("对象的增加与删除重复存在！");
            }
        }

        private void rootRemove(String path, Object obj) {
            Set sets = rootRemoves.computeIfAbsent(path, k -> new HashSet<>());
            sets.add(obj);
            if (rootAdds.computeIfAbsent(path, k -> new HashSet<>()).remove(obj)) {
                logger.warn("对象的增加与删除重复存在！");
            }
        }
    }

    public void close() {
        if (currentStaging.get() != null) {
            Staging staging = currentStaging.get();
            if (!staging.changes.isEmpty() ||
                    !staging.rootRemoves.isEmpty()
                    || !staging.rootAdds.isEmpty()) {
                logger.error("会话工厂关闭时存在未保存的数据");
            }
        }
        logger.info("数据会话工厂已关闭");
        this.neo4jSessionFactory.close();
    }

    static void writeFinalVal(Object source, Field field, Object newValue) throws NoSuchFieldException, IllegalAccessException {
        field.setAccessible(true);
        // 修改常量修饰
        if (Modifier.isFinal(field.getModifiers())) {
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            // wrapping setAccessible
            AccessController.doPrivileged((PrivilegedAction) () -> {
                modifiersField.setAccessible(true);
                return null;
            });
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        }
        field.set(source, newValue);
    }
}
