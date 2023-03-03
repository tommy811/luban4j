package store;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import neo4j.bean.Label;
import neo4j.bean.User;
import org.luban.store.Trust;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 鲁班大叔
 * @date 2023
 */
@Trust
public interface Db  {
    List<User> users = new ArrayList<>();// add remove
    List<Label> labels = new ArrayList<>();
}
