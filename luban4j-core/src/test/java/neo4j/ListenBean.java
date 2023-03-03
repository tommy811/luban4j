package neo4j;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import org.luban.Listen;
import org.luban.common.UtilJson;
import org.luban.monitor.PropertyListener;
import org.luban.monitor.PropertyListenerSuport;

import java.io.Serializable;
/**
 * @author 鲁班大叔
 * @date 2023
 */
public class ListenBean  implements Serializable , PropertyListenerSuport {
    private PropertyListener $p_c_l;
    String name;
    int age;
    public void saHEllo() {
        System.out.println("hello22");
    }


    public void insertPropertyListener(PropertyListener listener) {
        this.$p_c_l=listener;
    }
    public void removePropertyListener(PropertyListener listener) {
        this.$p_c_l=null;
    }

    @Override
    public PropertyListener currentPropertyListener() {
        return this.$p_c_l;
    }

    public String getName() {
        this.$p_c_l.get(this.name,this,"name");
        return name;
    }

    public void setName(String name) {
        this.$p_c_l.set(name,this.name,this,"name");
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
