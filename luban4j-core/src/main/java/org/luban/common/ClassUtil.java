package org.luban.common;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 鲁班大叔
 * @date 2023
 */
public class ClassUtil {
    static
    public List<Field> getAllFields(Class<?> type) {
        List<Field> fieldList = new ArrayList<>();
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                int modifiers = field.getModifiers();
                if (!Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers)) {
                    fieldList.add(field);
                }
            }
        }
        return fieldList;
    }
}
