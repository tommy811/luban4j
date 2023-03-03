package org.luban.http;


import java.io.InputStream;

public interface StaticResourceServer {
    Resource findResource(String path);

    interface Resource {
        InputStream getInputStream();

        String getPath();
    }
}
