package uk.co.staticvoid.gliderrider.business;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.co.staticvoid.gliderrider.domain.Attempt;
import uk.co.staticvoid.gliderrider.domain.CourseRecord;
import uk.co.staticvoid.gliderrider.domain.CourseTime;
import uk.co.staticvoid.gliderrider.helper.ConfigHelper;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RecordManager {

    public static final String CONFIG_FILE = "record.yml";
    public static final String JSON_FILE = "record.json";

    private final ConfigHelper configHelper;

    public RecordManager(ConfigHelper configHelper) {
        this.configHelper = configHelper;
    }

    public void addRecord(Attempt attempt) {
        Optional<CourseRecord> opt = getCourseRecord(attempt.getCourse());

        if (opt.isPresent()) {
            addNewCourseTime(opt.get(), attempt);
        } else {
            addNewCourseRecord(attempt);
        }
    }

    public Optional<CourseTime> getLeader(String course) {
        if(getCourseRecord(course).isPresent()) {
            return Optional.of(getCourseRecord(course).get().getLeaderCourseTime());
        } else {
            return Optional.empty();
        }
    }

    public List<CourseTime> getCourseTimes(String course) {
        Optional<CourseRecord> courseRecord = getCourseRecord(course);

        if(courseRecord.isPresent()) {
            return courseRecord.get().getCourseTimeList();
        } else {
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    public void removeRecord(String course) {
        List<CourseRecord> courseRecordList = (List<CourseRecord>)configHelper.getConfig().getList("record");
        List<CourseRecord> revisedRecordList = courseRecordList.stream().filter(cr -> !cr.getCourse().equals(course)).collect(Collectors.toList());

        saveRecords(revisedRecordList);
    }


    @SuppressWarnings("unchecked")
    public Optional<CourseRecord> getCourseRecord(String course) {
        List<CourseRecord> courseRecordList =  loadRecords();
        return courseRecordList.stream().filter(cr -> cr.getCourse().equals(course)).findFirst();
    }

    @SuppressWarnings("unchecked")
    private void addNewCourseRecord(Attempt attempt) {

        CourseRecord newCourseRecord = new CourseRecord(attempt.getCourse(),
                        new LinkedList<>(Collections.singletonList(
                                new CourseTime(attempt.getPlayer(), attempt.getCourseTime()))));

        List<CourseRecord> courseRecordList =  loadRecords();
        courseRecordList.add(newCourseRecord);
        saveRecords(courseRecordList);
    }


    @SuppressWarnings("unchecked")
    private void addNewCourseTime(CourseRecord courseRecord, Attempt attempt) {
        Optional<CourseTime> playerCourseTime = courseRecord.getCourseTime(attempt.getPlayer());

        if(playerCourseTime.isPresent()) {
            if(playerCourseTime.get().getTime() > attempt.getCourseTime()) {
                courseRecord.addCourseTime(new CourseTime(attempt.getPlayer(), attempt.getCourseTime()));
            }
        } else {
            courseRecord.addCourseTime(new CourseTime(attempt.getPlayer(), attempt.getCourseTime()));
        }

        removeRecord(courseRecord.getCourse());
        List<CourseRecord> courseRecordList =  loadRecords();
        courseRecordList.add(courseRecord);
        saveRecords(courseRecordList);
    }

    public void outputAsJson() throws IOException {
        List<CourseRecord> courseRecordList =  loadRecords();

        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.writeValue(new File(configHelper.getDataFolder(), JSON_FILE), courseRecordList);
    }

    @SuppressWarnings("unchecked")
    public synchronized List<CourseRecord> loadRecords() {
        return  (List<CourseRecord>)configHelper.getConfig().getList("record");
    }

    private synchronized void saveRecords(List<CourseRecord> courseRecordList) {
        configHelper.getConfig().set("record", courseRecordList);
        configHelper.saveConfig();
    }
}
