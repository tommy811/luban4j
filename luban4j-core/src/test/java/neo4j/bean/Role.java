package neo4j.bean;

import org.neo4j.ogm.annotation.*;

@RelationshipEntity(type = "ACTED_IN")

public class Role {
    @Id
    @GeneratedValue
    private Long relationshipId;
    @Property
    private String title;
    @StartNode
    private Actor actor;
    @EndNode
    private Movie movie;

    public Long getRelationshipId() {
        return relationshipId;
    }

    public void setRelationshipId(Long relationshipId) {
        this.relationshipId = relationshipId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Actor getActor() {
        return actor;
    }

    public void setActor(Actor actor) {
        this.actor = actor;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }
}