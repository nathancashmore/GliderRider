package uk.co.staticvoid.gliderrider.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CourseRecord implements ConfigurationSerializable {
    private String course;
    private List<CourseTime> courseTimeList;

    public CourseRecord(String course, List<CourseTime> courseTimeList) {
        this.course = course;
        this.courseTimeList = courseTimeList;
    }

    public CourseRecord(String course) {
        this.course = course;
        this.courseTimeList = new LinkedList<>();
    }

    public CourseRecord() {}

    @SuppressWarnings("unchecked")
    public CourseRecord(Map<String, Object> courseRecordAsMap) {
        this.course = (String)courseRecordAsMap.get("course");
        this.courseTimeList = (List<CourseTime>) courseRecordAsMap.get("courseTimeList");
    }

    public String getCourse() {
        return course;
    }

    public List<CourseTime> getCourseTimeList() {
        return courseTimeList;
    }

    public void addCourseTime(CourseTime courseTime) {
        getCourseTime(courseTime.getPlayer()).ifPresent(ct -> courseTimeList.remove(ct));
        courseTimeList.add(courseTime);
        Collections.sort(courseTimeList);
    }

    public CourseTime getLeaderCourseTime() {
        return courseTimeList.get(0);
    }

    public Optional<CourseTime> getCourseTime(String player) {
        return courseTimeList.stream().filter(ct -> ct.getPlayer().equals(player)).findFirst();
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> courseRecordAsMap = new HashMap<>();
        courseRecordAsMap.put("course", this.course);
        courseRecordAsMap.put("courseTimeList", this.courseTimeList);
        return courseRecordAsMap;
    }
}
