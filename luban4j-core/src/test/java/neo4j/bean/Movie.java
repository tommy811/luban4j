package neo4j.bean;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

@NodeEntity(label = "Film")

public class Movie {
    @Id
    @GeneratedValue
    public Long id;

    @Property(name = "title")
    private String name;

    public Movie(String name) {
        this.name = name;
    }

    public Movie() {
    }

    public Movie(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}



