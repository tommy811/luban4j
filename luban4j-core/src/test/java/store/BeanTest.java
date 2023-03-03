package store;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import neo4j.bean.User;
import org.junit.Test;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;

import java.util.Set;

/**
 * @author 鲁班大叔
 * @date 2023
 */
public class BeanTest {
    @Test
    public void test() {
        PropertyUtils propertyUtils = new PropertyUtils();
        propertyUtils.setBeanAccess(BeanAccess.FIELD);
        propertyUtils.getProperties(User.class);
        Set<Property> properties = propertyUtils.getProperties(User.class, BeanAccess.FIELD);
        System.gc();;
        propertyUtils.getProperties(User.class, BeanAccess.PROPERTY);
        User.class.getDeclaredFields();
        User.class.getFields();
    }
}
