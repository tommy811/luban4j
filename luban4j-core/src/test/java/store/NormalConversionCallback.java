package store;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import org.neo4j.ogm.typeconversion.ConversionCallback;

import java.math.BigDecimal;

/**
 * @author 鲁班大叔
 * @date 2023
 */
public class NormalConversionCallback implements ConversionCallback {
    @Override
    public <T> T convert(Class<T> targetType, Object value) {
        Object v = value;
        if (value instanceof BigDecimal && targetType.equals(Double.class)) {
            v = ((BigDecimal) value).doubleValue();
        } else if (value instanceof BigDecimal && targetType.equals(Float.class)) {
            v = ((BigDecimal) value).floatValue();
        }
        return (T) v;
    }
}
