package org.luban.http;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.luban.common.Assert;
import org.luban.common.StringUtils;
import org.luban.common.UtilJson;
import org.luban.http.ws.WebSocketHand;
import org.luban.store.TrustSession;
import org.luban.store.TrustSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static org.luban.common.CollectionUtil.MapOf;

/**
 * @author 鲁班大叔
 * @date 2023
 */
public class ApiServlet extends HttpServlet {
    final static Logger logger = LoggerFactory.getLogger(ApiServlet.class);


    @Override
    protected void service(HttpServletRequest req, HttpServletResponse response) {
        Object result;
        try {
            result = handle(req, response);
        } catch (ApiException | IllegalArgumentException e) {
            response.setStatus(400);// 封装异常并返回
            result = MapOf("code", 400, "time", new Date(), "error", e.getMessage());
            logger.error("业务异常：", e);
        } catch (Throwable e) {
            response.setStatus(500);// 封装业务异常并返回
            String message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            result = MapOf("code", 500, "time", new Date(), "error", message);
            logger.error("系统内部异常：", e);
        }
        // 构造参数
        returnResult(result, response);
    }

    private Object handle(HttpServletRequest req, HttpServletResponse resp) throws Throwable {
        String[] urls = req.getRequestURI().split("/");
        Assert.isTrue(urls.length >= 4, "url格式错误：" + req.getRequestURI());
        String proxyContext = (String) req.getAttribute("proxyContext");

        String className = urls[2];
        String methodName = urls[3];
        String invokerTarget = className + "#" + methodName;
        TomcatServer tomcatServer = TomcatServer.getInstance();
        ApiServer apiServer = tomcatServer.getApiServer(proxyContext);
        Assert.notNull(apiServer, "找不到代理服务:" + proxyContext);
        ApiServer.ApiCallable apiCall = apiServer.findApi(className, methodName);
        //实例化API对象
        Method method = apiCall.getMethod();

        String paramJson = req.getParameter("params");
        paramJson = paramJson != null ? URLDecoder.decode(paramJson, "UTF-8") : null;
        if (paramJson == null) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            IOUtils.copy(req.getInputStream(), output);
            byte[] bytes = output.toByteArray();
            paramJson = new String(bytes, StandardCharsets.UTF_8);
        }
        if (!StringUtils.hasText(paramJson)) {
            paramJson = "{}";
        }
        TrustSessionFactory dbFactory = apiServer.getDbSessionFactory();
        TrustSession trustSession = null;
        if (dbFactory != null) {
            trustSession = dbFactory.openSession();
        }
        Object[] params = buildParams(method, paramJson, req, resp, trustSession);

        try {
            return apiCall.run(params);
        } finally {
            if (trustSession != null)
                trustSession.close();
        }

    }

    private Object handleError(Throwable throwable) {
        String message = throwable.getMessage();
        Map<String, Object> result = new HashMap<>();
        result.put("time", new Date());
        if (throwable instanceof IllegalArgumentException || throwable instanceof ApiException) {
            result.put("error", message);//错误消息
        } else {
            result.put("error", "内部错误:" + throwable.getClass().getName() + ":" + throwable.getMessage());//错误消息
        }
        return result;
    }


    /***
     * 验证业务参数，和构建业务参数对象
     * @param paramJson
     * @param request
     * @param response
     * @param trustSession
     * @return
     * @throws ApiException
     */
    private Object[] buildParams(Method method, String paramJson, HttpServletRequest request,
                                 HttpServletResponse response, TrustSession trustSession)
            throws ApiException {
        Map<String, Object> map = null;
        try {
            map = UtilJson.toMap(paramJson);
        } catch (IllegalArgumentException e) {
            throw new ApiException("调用失败：json字符串格式异常，请检查params参数 ");
        }
        if (map == null) {
            map = new HashMap<>();
        }

        List<String> paramNames = Arrays.stream(method.getParameters()).map(Parameter::getName).collect(Collectors.toList());
        // goods ,id
        Class<?>[] paramTypes = method.getParameterTypes(); //反射

        for (Map.Entry<String, Object> m : map.entrySet()) {
            if (!paramNames.contains(m.getKey())) {
                throw new ApiException("调用失败：接口不存在‘" + m.getKey() + "’参数");
            }
        }
        Object[] args = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            if (paramTypes[i].isAssignableFrom(TrustSession.class)) {
                Assert.notNull(trustSession, "未配置数据库！");
                args[i] = trustSession;
            } else if (paramTypes[i].isAssignableFrom(HttpSession.class)) {
                args[i] = request.getSession();
            } else if (paramTypes[i].isAssignableFrom(HttpServletRequest.class)) {
                args[i] = request;
            } else if (paramTypes[i].isAssignableFrom(HttpServletResponse.class)) {
                args[i] = response;
            } else if (paramTypes[i].isAssignableFrom(Callback.class)) {
                args[i] = buildCallbackParam(map.get(paramNames.get(i)));
            } else if (map.containsKey(paramNames.get(i))) {
                try {
                    args[i] = convertJsonToBean(map.get(paramNames.get(i)), paramTypes[i]);
                } catch (Exception e) {
                    throw new ApiException("调用失败：指定参数格式错误或值错误‘" + paramNames.get(i) + "’"
                            + e.getMessage());
                }
            } else {
                args[i] = null;
            }
        }
        return args;
    }

    private Callback buildCallbackParam(Object path) {
        Assert.isTrue(path instanceof String, "参数必须是回调函数");
        String token = path.toString().split("/")[0];
        String id = path.toString().split("/")[1];
        return WebSocketHand.buildCall(token, id);
    }

    // 将MAP转换成具体的目标方方法参数对象
    private <T> Object convertJsonToBean(Object val, Class<T> targetClass) throws Exception {
        Object result = null;
        if (val == null) {
            return null;
        } else if (Integer.class.equals(targetClass)) {
            result = Integer.parseInt(val.toString());
        } else if (Long.class.equals(targetClass)) {
            result = Long.parseLong(val.toString());
        } else if (Date.class.equals(targetClass)) {
            if (val.toString().matches("[0-9]+")) {
                result = new Date(Long.parseLong(val.toString()));
            } else {
                throw new IllegalArgumentException("日期必须是长整型的时间戳");
            }
        } else if (String.class.equals(targetClass)) {
            if (val instanceof String) {
                result = val;
            } else {
                throw new IllegalArgumentException("转换目标类型为字符串");
            }
        } else {
            result = UtilJson.convertValue(val, targetClass);
        }
        return result;
    }

    private void returnResult(Object result, HttpServletResponse response) {
        try {
            UtilJson.JSON_MAPPER.configure(
                    SerializationFeature.WRITE_NULL_MAP_VALUES, true);
            String json = result == null ? "null" : UtilJson.writeValueAsString(result);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/html/json;charset=utf-8");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
            if (json != null)
                response.getWriter().write(json);
        } catch (IOException e) {
            logger.error("服务中心响应异常", e);
            throw new RuntimeException(e);
        }
    }


}
