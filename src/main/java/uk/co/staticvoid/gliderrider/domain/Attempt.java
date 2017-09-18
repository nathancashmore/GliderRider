package uk.co.staticvoid.gliderrider.domain;

import java.util.ArrayList;
import java.util.Map;

public class Attempt {
    private String player;
    private String course;
    private Map<String, Long> timeRecord;
    private boolean finished;

    public Attempt(String player, String course, Map<String, Long> timeRecord) {
        this.player = player;
        this.course = course;
        this.timeRecord = timeRecord;
    }

    public String getPlayer() {
        return player;
    }

    public String getCourse() {
        return course;
    }

    public Map<String, Long> getTimeRecord() {
        return timeRecord;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public Long getCourseTime() {
        Long startTime = new ArrayList<>(timeRecord.values()).get(0);
        Long finishTime = new ArrayList<>(timeRecord.values()).get(timeRecord.size() - 1);

        return finishTime - startTime;
    }

    public Long getCourseTime(String checkpointName) {
        Long startTime = new ArrayList<>(timeRecord.values()).get(0);
        Long finishTime = timeRecord.get(checkpointName);

        return finishTime - startTime;
    }
}


