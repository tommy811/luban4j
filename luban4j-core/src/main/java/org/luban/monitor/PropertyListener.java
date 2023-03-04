package org.luban.monitor;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

/**
 * @author 鲁班大叔
 * @date 2023
 */
public interface PropertyListener {
   default void get(Object value, Object source, String name){}

    void set(Object oldVla, Object newVal, Object source, String name);

    // 空实现
    PropertyListener EMPTY = new PropertyListener() {
        @Override
        public void get(Object value, Object source, String name) {

        }

        @Override
        public void set(Object oldVla, Object newVal, Object source, String name) {

        }
    };
}
