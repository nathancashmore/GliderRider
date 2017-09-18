package uk.co.staticvoid.gliderrider.domain;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

public class Location implements ConfigurationSerializable {

    private String world;
    private Integer x, y, z;
    private Direction direction;

    public Location(Map<String, Object> locationAsMap) {
        this.x = (Integer)locationAsMap.get("x");
        this.y = (Integer)locationAsMap.get("y");
        this.z = (Integer)locationAsMap.get("z");
        this.world = (String) locationAsMap.get("world");
        this.direction = Direction.valueOf((String)locationAsMap.get("direction"));
    }

    public Location(String world, Integer x, Integer y, Integer z, Direction direction) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.direction = direction;
    }

    public Location clone() {
        return new Location(this.world, this.x, this.y, this.z, this.direction);
    }

    public Location clone(Integer x, Integer y, Integer z) {
        return new Location(this.world, x, y, z, this.direction);
    }

    public String getWorld() {
        return world;
    }

    public Integer getX() {
        return x;
    }

    public Integer getY() {
        return y;
    }

    public Integer getZ() {
        return z;
    }

    public Direction getDirection() {
        return direction;
    }

    public Location addX(int amount) {
        this.x = x + amount;
        return this;
    }

    public Location addY(int amount) {
        this.y = y + amount;
        return this;
    }

    public Location addZ(int amount) {
        this.z = z + amount;
        return this;
    }

    public Location minusX(int amount) {
        this.x = x - amount;
        return this;
    }

    public Location minusY(int amount) {
        this.y = y - amount;
        return this;
    }

    public Location minusZ(int amount) {
        this.z = z - amount;
        return this;
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> locationAsMap = new HashMap<>();

        locationAsMap.put("world", this.world);
        locationAsMap.put("x", this.x);
        locationAsMap.put("y", this.y);
        locationAsMap.put("z", this.z);
        locationAsMap.put("direction", this.direction.name());

        return locationAsMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Location location = (Location) o;

        if (!world.equals(location.world)) return false;
        if (!x.equals(location.x)) return false;
        if (!y.equals(location.y)) return false;
        if (!z.equals(location.z)) return false;
        return direction == location.direction;
    }

    @Override
    public int hashCode() {
        int result = world.hashCode();
        result = 31 * result + x.hashCode();
        result = 31 * result + y.hashCode();
        result = 31 * result + z.hashCode();
        result = 31 * result + direction.hashCode();
        return result;
    }
}
