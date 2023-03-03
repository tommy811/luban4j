package org.luban.http;

import org.luban.store.TrustSessionFactory;

import java.lang.reflect.Method;

public interface ApiServer {
    ApiCallable findApi(String className, String method) throws ApiException;

    TrustSessionFactory getDbSessionFactory();

    interface ApiCallable {

        Object run(Object[] param) throws ApiException, Throwable;
        Method getMethod();
    }
}
