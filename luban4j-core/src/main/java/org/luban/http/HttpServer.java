package org.luban.http;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import org.luban.common.Assert;
import org.luban.common.EncryptUtil;
import org.luban.store.Trust;
import org.luban.store.TrustSessionFactory;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.session.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 鲁班大叔
 * @date 2023
 */
public class HttpServer implements ApiServer, StaticResourceServer {
    private static final Logger logger;
    private ClassLoader loader;
    private String contextPath;
    private String homePage = "index.html";
    private boolean onLine;

    static {
        logger = AccessController.doPrivileged((PrivilegedAction<Logger>) () -> LoggerFactory.getLogger(HttpServer.class));
    }

    private String[] apiPackages;
    private Configuration dbConfiguration;
    private TrustSessionFactory dbSessionFactory;

    public HttpServer() {
        onLine = getProperty("user.ident") != null;
    }

    public HttpServer api(final String... packages) {
        Assert.notNull(packages, "必须填写包");
        Assert.isTrue(packages.length > 0, "必须填写包");
        Assert.noNullElements(packages, "存在空包：" + Arrays.toString(packages));
        this.apiPackages = packages;
        return this;
    }

    public HttpServer dbPackages(final String... packages) {
        AccessController.doPrivileged((PrivilegedAction<Logger>) () -> {
            String defaultUri = System.getProperty("db.uri", "http://127.0.0.1:7474");
            String defaultUser = System.getProperty("db.user", "neo4j");
            String defaultPassword = System.getProperty("db.pwd", "123456");
            dbConfig(defaultUri, defaultUser, defaultPassword, -1, packages);
            return null;
        });

        return this;
    }


    public HttpServer dbConfig(String uri, String user, String password, int poolSize, String... packages) {
        Assert.notNull(packages, "必须填写包");
        Assert.isTrue(packages.length > 0, "必须填写包");
        Assert.noNullElements(packages, "存在空包：" + Arrays.toString(packages));
        String ident = getProperty("user.ident");
        if (ident != null) {
            poolSize = 5;// 在线开发环境固定5个连接
        } else if (poolSize < 0) {
            poolSize = 50;//
        }
        this.dbConfiguration = new Configuration.Builder()
                .uri(uri)
                .connectionPoolSize(poolSize)
                .credentials(user, password)
                .withBasePackages(packages)
                .build();

        return this;
    }

    private static String getProperty(String key, String def) {
        return AccessController.doPrivileged((PrivilegedAction<String>) () -> System.getProperty(key, def));
    }

    private static String getProperty(String key) {
        return AccessController.doPrivileged((PrivilegedAction<String>) () -> System.getProperty(key));
    }


    public void start() {
        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            _start();
            return null;
        });
    }

    /**
     * 启动http服务
     *
     * @throws InterruptedException
     */
    private void _start() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        TomcatServer server = TomcatServer.getInstance();
        String context = onLine ? Long.toHexString(System.currentTimeMillis() / 1000) : null;
        server.register(context, this, this);

        if (dbConfiguration != null) {
            logger.info("正在初始化数据库服务....");
            long begin = System.currentTimeMillis();
            initDatabase();
            long end = System.currentTimeMillis();
            logger.info("完成数据库初始化,用时(ms):{}", end - begin);
        }
        // 访问地址
        String address;
        if (onLine) {
            address = "http://stu.coderead.cn/?proxy_address=" + EncryptUtil.URLEncode(String.format("%s:%s/%s", "127.0.0.1", server.getPort(), context));
        } else {
            address = String.format("http://127.0.0.1:%s/", server.getPort());
        }
        logger.info("访问地址：" + address);
        this.loader = classLoader;
        this.contextPath = context;

        waitStop();
    }


    public HttpServer homePage(String page) {
        this.homePage = page;
        return this;
    }

    // TODO 待重构成控制台关闭
    public void waitStop() {
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            logger.error("WEB线程中断", e);
        }
//        System.out.println("输入1停止服务:");
//        Scanner scanner = new Scanner(System.in);
//        while (scanner.hasNextLine()) {
//            if (scanner.nextLine().equals("1")) {
//                stop();
//                break;
//            }
//        }
    }

    private void initDatabase() {
        SessionFactory neo4jSessionFactory = new SessionFactory(dbConfiguration);
        this.dbSessionFactory = new TrustSessionFactory(neo4jSessionFactory);
        // 自动托管 带@Trust注解的接口，该接口必须在dbPackages下
        neo4jSessionFactory.metaData().persistentEntities().stream()
                .filter(c -> c.annotationsInfo().get(Trust.class) != null)
                .filter(c -> c.getUnderlyingClass().isInterface())
                .forEach(c -> dbSessionFactory.trust(c.getUnderlyingClass()));
        logger.info("debug:" + logger.isDebugEnabled());
        if (logger.isDebugEnabled()) {
            logger.debug("neo4j加载的class:");
            for (ClassInfo persistentEntity : neo4jSessionFactory.metaData().persistentEntities()) {
                logger.debug(persistentEntity.toString());
            }
        }
    }


    // 关闭服务
    public void stop() {
        TomcatServer server = TomcatServer.getInstance();
        server.disable(contextPath);
        if (dbSessionFactory != null) {
            dbSessionFactory.close();
        }
        logger.info("WEB服务已注销：" + contextPath);
    }

    // className.
    @Override
    public ApiCallable findApi(String className, String methodName) throws ApiException {
        // 获取包名
        String pkg = className.contains(".") ? className.substring(0, className.lastIndexOf(".")) : "";
        Assert.isTrue(Arrays.asList(apiPackages).contains(pkg), "该API包未对外开放：" + pkg);

        //实例化API对象
        Class<?> aClass;
        final Object instance;
        final Method method;
        try {
            aClass = loader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new ApiException("找不到API类：" + className);
        }
        Assert.isTrue(aClass.getClassLoader() == loader, "无法访问API类：" + className);
        List<Method> methodList = Arrays.stream(aClass.getDeclaredMethods()).filter(m -> m.getName().equals(methodName)).collect(Collectors.toList());
        Assert.isTrue(!methodList.isEmpty(), "找不到API方法：" + className + "#" + methodName);
        Assert.isTrue(methodList.size() == 1, "存在多个同名方法:" + className + "#" + methodName);
        method = methodList.get(0);
        try {
            instance = aClass.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new ApiException("无法访问API类：" + className);
        } catch (NoSuchMethodException e) {
            throw new ApiException("必须存在一个空参构造函数：" + className);
        }
        return new ApiCallable() {
            @Override
            public Object run(Object[] param) throws Throwable {
                try {
                    return method.invoke(instance, param);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    throw e.getTargetException();
                }
            }

            @Override
            public Method getMethod() {
                return method;
            }
        };

    }

    @Override
    public Resource findResource(final String path) {
        Assert.hasText(path, "路径不能为空");
        // 打开首页
        String tPath = path.equals("static/") ? "static/" + homePage : path;
        return new Resource() {
            public InputStream getInputStream() {
                return loader.getResourceAsStream(tPath);
            }

            public String getPath() {
                return tPath;
            }
        };
    }

    @Override
    public TrustSessionFactory getDbSessionFactory() {
        return dbSessionFactory;
    }
}
