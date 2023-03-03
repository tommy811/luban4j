package http.tomcat;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.scan.StandardJarScanFilter;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

/**
 * @author 鲁班大叔
 * @date 2023
 */
public class TomcatTest {
    public static void main(String[] args) throws LifecycleException {
        long l = System.currentTimeMillis();
        Tomcat tomcat=new Tomcat();
        tomcat.setAddDefaultWebXmlToWebapp(false);
        Context context = tomcat.addWebapp("", "/Users/tommy/git/code-classroom/luban4j");
        tomcat.getConnector().setPort(8080);
        // 跳过扫描
        StandardJarScanFilter filter = new StandardJarScanFilter();
        filter.setTldSkip("*.jar");
        context.getJarScanner().setJarScanFilter(filter);

        tomcat.start();
        long end = System.currentTimeMillis();
        System.out.println(end-l);
        tomcat.getServer().await();
    }

    @Test
    public void test() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        classLoader.getResource("http.properties");
        Properties properties=new Properties();
        properties.load(classLoader.getResourceAsStream("http.properties"));
        System.out.println(classLoader);
    }
}
