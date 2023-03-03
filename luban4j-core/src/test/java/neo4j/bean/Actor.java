package neo4j.bean;

import org.neo4j.ogm.annotation.*;

import java.util.ArrayList;
import java.util.List;

@NodeEntity

public class Actor {

    @Id
    @GeneratedValue
    private Long id;

    @Property(name = "name")
    private String fullName;

    @Relationship(type = "ACTED_IN", direction = Relationship.OUTGOING)
    private List<Role> filmography=new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public List<Role> getFilmography() {
        return filmography;
    }

    public void setFilmography(List<Role> filmography) {
        this.filmography = filmography;
    }
}
