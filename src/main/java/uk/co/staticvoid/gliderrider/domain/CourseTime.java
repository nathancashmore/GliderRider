package uk.co.staticvoid.gliderrider.domain;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

public class CourseTime implements Comparable<CourseTime>, ConfigurationSerializable {

    private String player;
    private Long time;

    public CourseTime() {}

    public CourseTime(String player, Long time) {
        this.player = player;
        this.time = time;
    }

    public CourseTime(Map<String, Object> courseTimeAsMap) {
        this.player = (String)courseTimeAsMap.get("player");
        Integer timeAsInteger = (Integer)courseTimeAsMap.get("time");
        this.time = timeAsInteger.longValue();
    }

    public String getPlayer() {
        return player;
    }

    public Long getTime() {
        return time;
    }

    @Override
    public String toString() {
        return "Player: " + player + " - " + time + " ms";
    }

    @Override
    public int compareTo(CourseTime o) {
        return Long.compare(this.getTime(), o.getTime());
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> courseTimeAsMap = new HashMap<>();
        courseTimeAsMap.put("player", this.player);
        courseTimeAsMap.put("time", this.time);
        return courseTimeAsMap;
    }
}
