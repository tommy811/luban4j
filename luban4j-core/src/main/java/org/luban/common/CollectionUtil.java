package org.luban.common;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import java.util.*;

/**
 * @author 鲁班大叔
 * @date 2023
 */
public class CollectionUtil {

    public static Map MapOf(Object ... input){
        if ((input.length & 1) != 0) { // implicit nullcheck of input
            throw new InternalError("length is odd");
        }

        HashMap map = new HashMap<>();
        for (int i = 0; i < input.length; i++) {
            // 0 1   2 3   4 5
            map.put(input[i],input[++i]);
        }
        return map;
    }
    public static List ListOf(Object ... input){
        return Arrays.asList(input);
    }

    public static void main(String[] args) {
        Map map = MapOf("a", "1", "b", "2", "c", "3");
        System.out.println(map);
        System.out.println(ListOf(1, 2, 3, 4));
    }
}
