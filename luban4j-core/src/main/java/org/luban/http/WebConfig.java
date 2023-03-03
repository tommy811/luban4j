package org.luban.http;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import org.luban.common.EncryptUtil;
import org.luban.common.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.Set;

/**
 * @author 鲁班大叔
 * @date 2023
 */
public class WebConfig implements ServletContainerInitializer {
    static final Logger logger = LoggerFactory.getLogger(WebConfig.class);

    @Override
    public void onStartup(Set<Class<?>> c, ServletContext ctx) {
        {
            ApiServlet servlet = new ApiServlet();
            ServletRegistration.Dynamic registration = ctx.addServlet("api", servlet);
            registration.setLoadOnStartup(1);
            registration.addMapping("/api/*");
        }

        {   // 静态文件
            StaticServlet servlet = new StaticServlet();
            ServletRegistration.Dynamic registration = ctx.addServlet("static", servlet);
            registration.setLoadOnStartup(2);
            registration.addMapping("/*");
        }
        {// 上下文过滤器
            FilterRegistration.Dynamic contextFilter = ctx.addFilter("ContextFilter", new ContextFilter());
            contextFilter.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD),true,"/*");
        }
        {
            // 添加会话管理监听
            ctx.addListener(SessionManager.getInstance());
        }


    }

    static class ContextFilter implements Filter {

        @Override
        public void doFilter(ServletRequest req, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            String proxy_address = req.getParameter("proxy_address");// 基于参数获取
            HttpServletRequest request= (HttpServletRequest) req;
            logger.info("请求："+request.getRequestURI());
//            if (!StringUtils.hasText(proxy_address)) {// 基于cookie 获取
//                HttpServletRequest request = (HttpServletRequest) req;
//                for (Cookie cookie : request.getCookies()) {
//                    if (cookie.getName().equals("proxy_address")) {
//                        proxy_address = cookie.getValue();
//                        break;
//                    }
//                }
//            }

            if (StringUtils.hasText(proxy_address)) {
                String proxyContext = proxy_address.substring(proxy_address.lastIndexOf("/") + 1);
                // TODO 验证地址有效性后 在设置cookie 域名为 .coderead.cn
                Cookie proxy_address1 = new Cookie("proxy_address", EncryptUtil.URLEncode(proxy_address));
                proxy_address1.setPath("/");
                ((HttpServletResponse) response).addCookie(proxy_address1);
                req.setAttribute("proxyContext", proxyContext);
            }
            chain.doFilter(req, response);
        }
    }
}
