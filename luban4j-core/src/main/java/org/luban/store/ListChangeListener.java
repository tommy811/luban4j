package org.luban.store;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

/**
 * @author 鲁班大叔
 * @date 2023
 */
public interface ListChangeListener {
    void addItem(Object item);

    void removeItem(Object item);

    default void get(String name) {

    }

    ListChangeListener EMPTY = new ListChangeListener() {
        @Override
        public void addItem(Object item) {

        }

        @Override
        public void removeItem(Object item) {

        }
    };
}
