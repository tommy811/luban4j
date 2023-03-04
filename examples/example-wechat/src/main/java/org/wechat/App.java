package org.wechat;

import org.luban.http.HttpServer;

import java.io.File;
import java.io.IOException;


public class App {
    public static void main(String[] args) throws IOException {
       /*
       集成数据库*/
        String temDir = System.getProperty("java.io.tmpdir");
        File file = new File(temDir, "neo4jDb_Wechat");
        String url=file.toURI().toString();
        System.out.println("数据库："+file);

//       String url="http://127.0.0.1:7474/";
        new HttpServer()
                .api("org.wechat.service")
                .dbConfig(url, "neo4j", "123456", 10,
                        "org.wechat.dao", "org.wechat.bean")
                .start();
    }

}
