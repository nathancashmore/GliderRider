package uk.co.staticvoid.gliderrider.domain;

import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class AttemptTest {

    private static final String PLAYER = "TestPlayer";
    private static final String COURSE = "TestCourse";
    private static final String START_CHECKPOINT = "StartCheckpoint";
    private static final String STAGE_CHECKPOINT = "StageCheckpoint";
    private static final String FINISH_CHECKPOINT = "FinishCheckpoint";

    @Test
    public void getCourseTime() {
        Map<String, Long> timeMap = new LinkedHashMap<>();

        timeMap.put(START_CHECKPOINT, 10L);
        timeMap.put(FINISH_CHECKPOINT, 15L);

        Attempt underTest = new Attempt(PLAYER, COURSE, timeMap);

        assertThat(underTest.getCourseTime(),is(5L));
    }

    @Test
    public void getCourseTimeForCheckpoint() {
        Map<String, Long> timeMap = new LinkedHashMap<>();

        timeMap.put(START_CHECKPOINT, 10L);
        timeMap.put(STAGE_CHECKPOINT, 20L);
        timeMap.put(FINISH_CHECKPOINT, 30L);

        Attempt underTest = new Attempt(PLAYER, COURSE, timeMap);

        assertThat(underTest.getCourseTime(STAGE_CHECKPOINT),is(10L));
        assertThat(underTest.getCourseTime(FINISH_CHECKPOINT),is(20L));
    }

    @Test
    public void getCourseTimeForEmptyList() {
        Map<String, Long> timeMap = new LinkedHashMap<>();

        Attempt underTest = new Attempt(PLAYER, COURSE, timeMap);

        assertThat(underTest.getCourseTime(START_CHECKPOINT), is(0L));
    }

    @Test
    public void getNoOfCheckpointsPassed() {
        Map<String, Long> timeMap = new LinkedHashMap<>();

        timeMap.put(START_CHECKPOINT, 10L);
        timeMap.put(STAGE_CHECKPOINT, 20L);

        Attempt underTest = new Attempt(PLAYER, COURSE, timeMap);

        assertThat(underTest.getNoOfCheckpointsPassed(),is(2));
    }

}