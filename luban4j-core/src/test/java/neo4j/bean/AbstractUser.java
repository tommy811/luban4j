package neo4j.bean;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

/**
 * @author 鲁班大叔
 * @date 2023
 */

public class AbstractUser {
    String sex;
    Long age;
   private String createdTimestamp;

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public Long getAge() {
        return age;
    }

    public void setAge(Long age) {
        this.age = age;
    }
}
