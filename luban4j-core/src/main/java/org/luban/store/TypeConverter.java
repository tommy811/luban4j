package org.luban.store;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import org.neo4j.ogm.typeconversion.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 鲁班大叔
 * @date 2023
 */
public class TypeConverter {
    public final static Map<String, AttributeConverter> CONVERTERS = new HashMap<>();
    // 仅支持对象类型，不支持原始类型
    static {
//        CONVERTERS.put("float", new NumberStringConverter(Float.class));
//        CONVERTERS.put("float[]", new NumberArrayStringConverter(float.class));

        CONVERTERS.put("java.lang.Float", new NumberStringConverter(Float.class));
        CONVERTERS.put("java.lang.Float[]", new NumberArrayStringConverter(Float.class));
        CONVERTERS.put("java.util.List/java.lang.Float", new NumberCollectionStringConverter(Float.class, List.class));

//        CONVERTERS.put("double", new NumberStringConverter(Double.class));
//        CONVERTERS.put("double[]", new NumberArrayStringConverter(Double.class));

        CONVERTERS.put("java.lang.Double", new NumberStringConverter(Double.class));
        CONVERTERS.put("java.lang.Double[]", new NumberArrayStringConverter(Double.class));
        CONVERTERS.put("java.util.List/java.lang.Double", new NumberCollectionStringConverter(Double.class, List.class));

    }

}
