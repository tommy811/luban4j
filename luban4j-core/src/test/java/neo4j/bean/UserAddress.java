package neo4j.bean;

import org.luban.Listen;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.id.UuidStrategy;

@Listen
@NodeEntity
public class UserAddress {
    @Id
    @GeneratedValue(strategy = UuidStrategy.class)
    String id;
    String address;
    String city;

    public UserAddress(String address, String city) {
        this.address = address;
        this.city = city;
    }

    public UserAddress() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
