package store;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import org.neo4j.ogm.typeconversion.AttributeConverter;

/**
 * @author 鲁班大叔
 * @date 2023
 */
public class NormalAttributeConverter implements AttributeConverter<Object, Object> {
    private Class<?> entityAttributeType;

    public NormalAttributeConverter(Class<?> entityAttributeType) {
        this.entityAttributeType = entityAttributeType;
    }

    @Override
    public Object toGraphProperty(Object value) {
        return value;
    }

    @Override
    public Object toEntityAttribute(Object value) {

        return null;
    }
}
