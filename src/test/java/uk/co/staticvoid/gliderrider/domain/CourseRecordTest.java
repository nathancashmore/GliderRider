package uk.co.staticvoid.gliderrider.domain;

import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class CourseRecordTest {

    private static final String PLAYER_1 = "player";
    private static final String PLAYER_2 = "player2";
    private static final String COURSE = "course";
    private static final Long TIME = 60000L;

    private CourseTime courseTime1 = new CourseTime(PLAYER_1, TIME);
    private CourseTime courseTime2 = new CourseTime(PLAYER_2, TIME * 2);

    private CourseRecord courseRecord;

    @Before
    public void setup() {
        courseRecord = new CourseRecord(COURSE);
    }

    @Test
    public void shouldReturnCourseLeader() {
        courseRecord.addCourseTime(courseTime1);
        courseRecord.addCourseTime(courseTime2);

        assertThat(courseRecord.getLeaderCourseTime().getTime(), is(TIME));
    }

    @Test
    public void shouldReturnPlayersTime() {
        courseRecord.addCourseTime(courseTime1);
        courseRecord.addCourseTime(courseTime2);

        Optional<CourseTime> result = courseRecord.getCourseTime(PLAYER_2);

        assertThat(result.isPresent(), is(true));
        assertThat(result.get().getTime(), is(TIME * 2));
    }

    @Test
    public void shouldReplaceAnEntry() {
        courseRecord.addCourseTime(new CourseTime(PLAYER_1, TIME * 2));
        courseRecord.addCourseTime(new CourseTime(PLAYER_1, TIME));

        assertThat(courseRecord.getCourseTimeList().size(), is(1));
    }
}