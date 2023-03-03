package neo4j.bean;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

/**
 * @author 鲁班大叔
 * @date 2023
 */
public class Fuck {
    /*@Id
    @GeneratedValue(strategy = UuidStrategy.class)*/
    String id;
    String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
