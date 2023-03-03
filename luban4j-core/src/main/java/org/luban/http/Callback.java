package org.luban.http;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

/**
 * @author 鲁班大叔
 * @date 2023
 */
public interface Callback {
    void call(Object... args);// 回调
    // 连接断开
    void onDestroy(Runnable runnable);
}
