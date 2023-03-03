package http.tomcat;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import org.luban.http.HttpServer;
import org.luban.store.Trust;

/**
 * @author 鲁班大叔
 * @date 2023
 */
public class HttpServerTest {
    public static void main(String[] args) {
        HttpServer server = new HttpServer();
//        System.setProperty("db.key","myDb");
        server.api("http.tomcat")
                .dbPackages("neo4j.bean","store")
                .start();
    }
}
