package uk.co.staticvoid.gliderrider.helper;

import uk.co.staticvoid.gliderrider.domain.Checkpoint;
import uk.co.staticvoid.gliderrider.domain.CheckpointType;
import uk.co.staticvoid.gliderrider.domain.Location;

public final class CheckpointBuilder {
    private String name;
    private String course;
    private CheckpointType type;
    private Location location;
    private Integer radius;

    private CheckpointBuilder() {
    }

    public static CheckpointBuilder aCheckpoint() {
        return new CheckpointBuilder();
    }

    public CheckpointBuilder from(Checkpoint checkpoint) {
        this.name = checkpoint.getName();
        this.course = checkpoint.getCourse();
        this.type = checkpoint.getType();
        this.location = checkpoint.getLocation();
        this.radius = checkpoint.getRadius();
        return this;
    }

    public CheckpointBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public CheckpointBuilder withCourse(String course) {
        this.course = course;
        return this;
    }

    public CheckpointBuilder withType(CheckpointType type) {
        this.type = type;
        return this;
    }

    public CheckpointBuilder withLocation(Location location) {
        this.location = location;
        return this;
    }

    public CheckpointBuilder withRadius(Integer radius) {
        this.radius = radius;
        return this;
    }

    public Checkpoint build() {
        Checkpoint checkpoint = new Checkpoint(name, course, type, location, radius);
        return checkpoint;
    }
}
