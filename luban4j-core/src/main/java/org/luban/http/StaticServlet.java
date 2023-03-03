package org.luban.http;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.luban.common.Assert;
import org.luban.common.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * @author 鲁班大叔
 * @date 2023
 */
public class StaticServlet extends HttpServlet {

    final static Logger logger = LoggerFactory.getLogger(StaticServlet.class);


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String proxyContext = (String) req.getAttribute("proxyContext");
        TomcatServer tomcatServer = TomcatServer.getInstance();
        StaticResourceServer loader = tomcatServer.findResourceServer(proxyContext);
        Assert.isTrue(loader != null, "找不到代理服务:" + proxyContext);
        String path = req.getRequestURI().replaceFirst("^/+", "static/");
        StaticResourceServer.Resource resource = loader.findResource(path);
        try (InputStream in = resource.getInputStream()) {
            if (in != null) {
                if ( resource.getPath().contains(".")) {
                    String mime = tomcatServer.findMimeMapping(  resource.getPath().replaceFirst("^(.*)\\.(.*)","$2"));
                    resp.setContentType(mime +";charset=UTF-8");
                }
                // TODO 最后修改时间，支持缓存
                // resp.setDateHeader("Last-Modified","");
                // resp.setContentLength();
                IOUtils.copy(in, resp.getOutputStream());
            } else {
                resp.setStatus(404);
                resp.setContentType("text/plain;charset=utf-8");
                resp.getWriter().println("找不到：" + req.getRequestURI());
            }
        }
    }


}
