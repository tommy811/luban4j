package org.luban.http;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 鲁班大叔
 * @date 2023
 */
// 访问方式 1.集成开发 2.集成部署 3.自定义部署
public class TomcatServer {
    private static final String DEFAULT_CONTEXT = "DEFAULT_CONTEXT";
    static volatile TomcatServer instance;
    int port;// 当前端口
    //注册的http服务
    Map<String, ApiServer> apiServers = new ConcurrentHashMap<>();
    Map<String, StaticResourceServer> resourceServers = new ConcurrentHashMap<>();
    Tomcat tomcat = null;
    private Context context;

    public static TomcatServer getInstance() {
        if (instance == null) {
            synchronized (TomcatServer.class) {
                if (instance == null) {
                    instance = new TomcatServer();
                    // TODO 优化启动时机
                    // 初始化并启动
                    try {
                        instance.startTomcatServer(new Properties());
                    } catch (LifecycleException e) {
                        throw new RuntimeException("启动Tomcat失败", e);
                    }
                }
            }
        }
        return instance;
    }

    private void startTomcatServer(Properties config) throws LifecycleException {
//        System.setProperty("tomcat.util.http.parser.HttpParser.requestTargetAllow","|:{}");
//        System.setProperty("org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH","true");
        Tomcat tomcat = new Tomcat();
        tomcat.setAddDefaultWebXmlToWebapp(false);// 禁止加载jsp
        String base = config.getProperty("baseDir", System.getProperty("java.io.tmpdir"));
        String contextPath = config.getProperty("context", "");
        String port = config.getProperty("port", buildNotUsedPort() + "");
//        base = "/Users/tommy/git/code-classroom/luban4j/target/test-classes/static";
        Context context = tomcat.addWebapp(contextPath, base);
        // TODO 支持在url中输入json字符
//        context.addParameter("tomcat.util.http.parser.HttpParser.requestTargetAllow","|{}");
//        context.addParameter("org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH","true");

        context.setSessionTimeout(30);

        // MIME type mappings
        Tomcat.addDefaultMimeTypeMappings(context);

        // Welcome files
        context.addWelcomeFile("index.html");
        context.addWelcomeFile("index.htm");


        tomcat.getConnector().setPort(Integer.parseInt(port));
        tomcat.start();
        this.context = context;
        this.tomcat = tomcat;
    }

    public String findMimeMapping(String extension) {
        return context.findMimeMapping(extension);
    }

    public int getPort() {
        return tomcat.getConnector().getPort();
    }

    // 注册标识检测
    public String register(String context, ApiServer httpLoader, StaticResourceServer staticResourceServer) {
        context = context == null ? DEFAULT_CONTEXT : context;
        apiServers.put(context, httpLoader);
        resourceServers.put(context, staticResourceServer);
        return context;
    }



    public void disable(String context) {
        apiServers.remove(context);
        resourceServers.remove(context);
    }

    private static int buildNotUsedPort() {
        for (int i = 9000; i < 9999; i++) {
            if (isUsableLocalPort(i)) {
                return i;
            }
        }
        throw new IllegalStateException("找不到可用端口");
    }

    // 检测端口是否可用
    private static boolean isUsableLocalPort(int port) {
        try (ServerSocket ss = new ServerSocket(port)) {
            ss.setReuseAddress(true);
        } catch (IOException ignored) {
            return false;
        }
        try (DatagramSocket ds = new DatagramSocket(port)) {
            ds.setReuseAddress(true);
        } catch (IOException ignored) {
            return false;
        }
        return true;
    }

    public ApiServer getApiServer(String proxyContext) {
        proxyContext=proxyContext==null?DEFAULT_CONTEXT:proxyContext;
        return apiServers.get(proxyContext);
    }

    //TODO 处理proxyContext 为null
    public StaticResourceServer findResourceServer(String proxyContext) {
        proxyContext=proxyContext==null?DEFAULT_CONTEXT:proxyContext;
        return resourceServers.get(proxyContext);
    }
}
