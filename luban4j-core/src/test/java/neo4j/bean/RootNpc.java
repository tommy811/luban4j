package neo4j.bean;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import java.util.List;

/**
 * @author 鲁班大叔
 * @date 2023
 */
public class RootNpc {
//    @Id
//    @GeneratedValue(strategy = UuidStrategy.class)
    Long id;
    String path;
    List<Label> source;

    public RootNpc(String path, List<Label> source) {
        this.path = path;
        this.source = source;
    }

    public RootNpc() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<Label> getSource() {
        return source;
    }

    public void setSource(List<Label> source) {
        this.source = source;
    }
}
