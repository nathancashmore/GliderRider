package uk.co.staticvoid.gliderrider.business;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import uk.co.staticvoid.gliderrider.GliderRider;
import uk.co.staticvoid.gliderrider.domain.Attempt;
import uk.co.staticvoid.gliderrider.domain.CourseRecord;
import uk.co.staticvoid.gliderrider.domain.CourseTime;
import uk.co.staticvoid.gliderrider.helper.ConfigHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

@RunWith(PowerMockRunner.class)
@PrepareForTest({GliderRider.class, Bukkit.class})
public class RecordManagerTest {
    private static final String TEST_OUTPUT_FOLDER = "./src/test/output/";

    private static final Long ONE_MINUTE     = 60000L;

    private static final String PLAYER_1 = "TestPlayer1";
    private static final String PLAYER_2 = "TestPlayer2";
    private static final String PLAYER_3 = "TestPlayer3";
    private static final String PLAYER_4 = "TestPlayer4";
    private static final String COURSE_A = "TestCourseA";
    private static final String COURSE_B = "TestCourseB";

    private static final String START_CHECKPOINT_NAME = "StartCheckpoint";
    private static final String FINISH_CHECKPOINT_NAME = "FinishCheckpoint";

    private GliderRider plugin = PowerMockito.mock(GliderRider.class);
    private ConfigHelper configHelper = new ConfigHelper(plugin, RecordManager.CONFIG_FILE);

    private RecordManager underTest = new RecordManager(configHelper);

    private Attempt attempt1, attempt2, attempt3, attempt4;

    private Map<String, Long> timeRecord1 = new LinkedHashMap<>();
    private Map<String, Long> timeRecord2 = new LinkedHashMap<>();
    private Map<String, Long> timeRecord3 = new LinkedHashMap<>();
    private Map<String, Long> timeRecord4 = new LinkedHashMap<>();

    @Before
    public void setup() {
        ConfigurationSerialization.registerClass(CourseRecord.class);
        ConfigurationSerialization.registerClass(CourseTime.class);

        when(plugin.getDataFolder()).thenReturn(new File(TEST_OUTPUT_FOLDER));

        timeRecord1.put(START_CHECKPOINT_NAME, 0L);
        timeRecord1.put(FINISH_CHECKPOINT_NAME, ONE_MINUTE * 3);

        timeRecord2.put(START_CHECKPOINT_NAME, 0L);
        timeRecord2.put(FINISH_CHECKPOINT_NAME, ONE_MINUTE * 2);

        timeRecord3.put(START_CHECKPOINT_NAME, 0L);
        timeRecord3.put(FINISH_CHECKPOINT_NAME, ONE_MINUTE);

        timeRecord4.put(START_CHECKPOINT_NAME, 0L);
        timeRecord4.put(FINISH_CHECKPOINT_NAME, ONE_MINUTE * 5);

        attempt1 = new Attempt(PLAYER_1, COURSE_A, timeRecord1);
        attempt2 = new Attempt(PLAYER_2, COURSE_A, timeRecord2);
        attempt3 = new Attempt(PLAYER_3, COURSE_A, timeRecord3);
        attempt4 = new Attempt(PLAYER_4, COURSE_B, timeRecord4);
    }

    @Test
    public void shouldReturnTheFastestPlayer() {
        underTest.addRecord(attempt1);
        underTest.addRecord(attempt2);
        underTest.addRecord(attempt3);

        Optional<CourseTime> courseTime = underTest.getLeader(COURSE_A);

        assertThat(courseTime.isPresent(), is(true));
        assertThat(courseTime.get().getPlayer(), is(PLAYER_3));
        assertThat(courseTime.get().getTime(), is(ONE_MINUTE));
    }

    @Test
    public void shouldNotReplaceARecordIfPlayerHasAFasterTime() {
        timeRecord1.put(START_CHECKPOINT_NAME, 0L);
        timeRecord1.put(FINISH_CHECKPOINT_NAME, ONE_MINUTE);

        timeRecord2.put(START_CHECKPOINT_NAME, 0L);
        timeRecord2.put(FINISH_CHECKPOINT_NAME, ONE_MINUTE * 2);

        attempt1 = new Attempt(PLAYER_1, COURSE_A, timeRecord1);
        attempt2 = new Attempt(PLAYER_1, COURSE_A, timeRecord2);

        underTest.addRecord(attempt1);
        underTest.addRecord(attempt2);

        Optional<CourseTime> courseTime = underTest.getLeader(COURSE_A);

        assertThat(courseTime.isPresent(), is(true));
        assertThat(courseTime.get().getTime(), is(ONE_MINUTE));
    }

    @Test
    public void shouldReturnCourseTimes() {
        underTest.addRecord(attempt1);
        underTest.addRecord(attempt4);

        List<CourseTime> expectedResult = new LinkedList<>(Collections.singletonList(new CourseTime(PLAYER_4, ONE_MINUTE * 5)));
        List<CourseTime> result = underTest.getCourseTimes(COURSE_B);

        assertReflectionEquals(expectedResult, result);
    }

    @Test
    public void shouldDetermineIfTheFastestTime() {
        underTest.addRecord(attempt1);
        underTest.addRecord(attempt2);
        underTest.addRecord(attempt3);
        underTest.addRecord(attempt4);

        assertThat(underTest.isTheFastestTime(attempt3), is(true));
    }

    @Test
    public void shouldRemoveARecord() {
        underTest.addRecord(attempt1);
        assertThat(underTest.getCourseRecord(COURSE_A).isPresent(), Matchers.is(true));

        underTest.removeRecord(COURSE_A);
        assertThat(underTest.getCourseRecord(COURSE_A).isPresent(), is(false));
    }

    @Test
    public void shouldAddMultipleAttempts() {
        underTest.addRecord(attempt1);
        underTest.addRecord(attempt2);
        underTest.addRecord(attempt3);
        underTest.addRecord(attempt4);

        assertThat(underTest.loadRecords().size(), is(2));
        assertThat(underTest.getCourseTimes(COURSE_A).size(), is(3));
    }

    @Test
    public void shouldOutputRecordsAsJSON() throws IOException {
        List<CourseRecord> expectedResult = new LinkedList<>();
        expectedResult.add(new CourseRecord(attempt1.getCourse(), Collections.singletonList(new CourseTime(attempt1.getPlayer(), attempt1.getCourseTime()))));

        underTest.addRecord(attempt1);
        underTest.outputAsJson();

        List<CourseRecord> result = readCourseRecordListFromJson(TEST_OUTPUT_FOLDER + RecordManager.JSON_FILE);

        assertReflectionEquals(result, expectedResult);
    }

    @After
    public void cleanUp() throws Exception{
        removeConfigFileFromOutputDirectory();
    }

    private void removeConfigFileFromOutputDirectory() throws IOException {
        Files.deleteIfExists(new File(TEST_OUTPUT_FOLDER, RecordManager.CONFIG_FILE).toPath() );
        Files.deleteIfExists(new File(TEST_OUTPUT_FOLDER, RecordManager.JSON_FILE).toPath() );
    }

    private List<CourseRecord> readCourseRecordListFromJson(String filename) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        File expectedOutputFile = new File(filename);
        return objectMapper.readValue(expectedOutputFile, new TypeReference<List<CourseRecord>>() {});
    }

}