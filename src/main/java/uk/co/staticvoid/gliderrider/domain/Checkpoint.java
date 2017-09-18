package uk.co.staticvoid.gliderrider.domain;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

public class Checkpoint implements ConfigurationSerializable {

    private String name;
    private String course;
    private CheckpointType type;
    private Location location;
    private Integer radius;

    public Checkpoint(Map<String, Object> checkpointAsMap) {
        this.name = (String)checkpointAsMap.get("name");
        this.course = (String)checkpointAsMap.get("course");
        this.type = CheckpointType.valueOf((String)checkpointAsMap.get("type"));
        this.location = (Location)checkpointAsMap.get("location");
        this.radius = (Integer)checkpointAsMap.get("radius");
    }

    public Checkpoint(String name, String course, CheckpointType type, Location location, Integer radius) {
        this.name = name;
        this.course = course;
        this.type = type;
        this.location = location;
        this.radius = radius;
    }

    public String getName() {
        return name;
    }

    public String getCourse() {
        return course;
    }

    public CheckpointType getType() {
        return type;
    }

    public Location getLocation() {
        return location;
    }

    public Integer getRadius() {
        return radius;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> checkpointAsMap = new HashMap<>();

        checkpointAsMap.put("name", this.name);
        checkpointAsMap.put("course", this.course);
        checkpointAsMap.put("type", this.type.name());
        checkpointAsMap.put("location", this.location);
        checkpointAsMap.put("radius", this.radius);

        return checkpointAsMap;
    }

    @Override
    public String toString() {
        return this.name +
                " - " + this.getCourse() +
                " ( " + this.getType() + " ) " +
                " @ X: " + this.getLocation().getX().toString() +
                " @ Y: " + this.getLocation().getY().toString() +
                " @ Z: " + this.getLocation().getZ().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Checkpoint that = (Checkpoint) o;

        if (!name.equals(that.name)) return false;
        if (!course.equals(that.course)) return false;
        if (type != that.type) return false;
        if (!location.equals(that.location)) return false;
        return radius.equals(that.radius);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + course.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + location.hashCode();
        result = 31 * result + radius.hashCode();
        return result;
    }
}
