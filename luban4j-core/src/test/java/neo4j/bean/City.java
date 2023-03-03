package neo4j.bean;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.id.UuidStrategy;

/**
 * @author 鲁班大叔
 * @date 2023
 */
public class City {
    @Id
    @GeneratedValue(strategy = UuidStrategy.class)
    String id;
    String name;
    long coord;

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

    public long getCoord() {
        return coord;
    }

    public void setCoord(long coord) {
        this.coord = coord;
    }
}
