package uk.co.staticvoid.gliderrider.business;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import uk.co.staticvoid.gliderrider.GliderRider;
import uk.co.staticvoid.gliderrider.domain.Checkpoint;
import uk.co.staticvoid.gliderrider.domain.CheckpointType;
import uk.co.staticvoid.gliderrider.domain.Location;
import uk.co.staticvoid.gliderrider.exception.CheckpointWithSameNameException;
import uk.co.staticvoid.gliderrider.exception.CourseAlreadyCompleteException;
import uk.co.staticvoid.gliderrider.exception.StartAlreadyExistsException;
import uk.co.staticvoid.gliderrider.exception.StartMustStartException;
import uk.co.staticvoid.gliderrider.helper.CheckpointBuilder;
import uk.co.staticvoid.gliderrider.helper.ConfigHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;
import static uk.co.staticvoid.gliderrider.domain.Direction.N;

@RunWith(PowerMockRunner.class)
@PrepareForTest({GliderRider.class, Bukkit.class})
public class CheckpointManagerTest {
    private static final String TEST_OUTPUT_FOLDER = "./src/test/output/";
    private static final String TEST_FILE_NAME = "checkpoint.yml";

    public static final String TEST_WORLD = "TestWorld";
    public static final String TEST_CHECKPOINT = "Test-Checkpoint";
    public static final String TEST_COURSE = "Test-Course";

    public static final String START_STAGE_NAME = "START";
    public static final String STAGE_STAGE_NAME = "STAGE";
    public static final String FINISH_STAGE_NAME = "FINISH";

    private GliderRider plugin = mock(GliderRider.class);
    private FileConfiguration fileConfiguration = mock(FileConfiguration.class);

    private CheckpointArtist checkpointArtist = mock(CheckpointArtist.class);
    private ConfigHelper configHelper = new ConfigHelper(plugin, TEST_FILE_NAME);
    private CheckpointManager underTest;

    private Integer RADIUS = 2;
    private Location randomLocation;
    private Checkpoint sampleCheckpoint;

    @Before
    public void setup() {
        ConfigurationSerialization.registerClass(Checkpoint.class);
        ConfigurationSerialization.registerClass(Location.class);

        Mockito.when(plugin.getConfig()).thenReturn(fileConfiguration);
        Mockito.when(fileConfiguration.getInt("checkpoint-radius")).thenReturn(RADIUS);

        when(plugin.getDataFolder()).thenReturn(new File(TEST_OUTPUT_FOLDER));

        randomLocation = new Location(TEST_WORLD, 10,10,10,N);
        sampleCheckpoint = new Checkpoint(TEST_CHECKPOINT, TEST_COURSE, CheckpointType.START, randomLocation, RADIUS);
        underTest = new CheckpointManager(plugin, configHelper, checkpointArtist);
    }

    @Test
    public void createCheckpoint() {
        Checkpoint start = CheckpointBuilder.aCheckpoint().from(sampleCheckpoint).withName(START_STAGE_NAME).withType(CheckpointType.START).build();
        Checkpoint stage = CheckpointBuilder.aCheckpoint().from(sampleCheckpoint).withName(STAGE_STAGE_NAME).withType(CheckpointType.STAGE).build();
        Checkpoint finish = CheckpointBuilder.aCheckpoint().from(sampleCheckpoint).withName(FINISH_STAGE_NAME).withType(CheckpointType.FINISH).build();

        underTest.createCheckpoint(start.getName(),start.getCourse(), start.getType(), start.getLocation());
        underTest.createCheckpoint(stage.getName(),stage.getCourse(), stage.getType(), stage.getLocation());
        underTest.createCheckpoint(finish.getName(),finish.getCourse(), finish.getType(), finish.getLocation());

        List<Checkpoint> result = underTest.getCheckpoints();

        assertThat(Arrays.asList(start,stage,finish), is(equalTo(result)));
    }

    @Test(expected = StartAlreadyExistsException.class)
    public void shouldNotBeAbleToCreateStartCheckpointIfCourseAlreadyHasAStart() {
        Checkpoint start = CheckpointBuilder.aCheckpoint().from(sampleCheckpoint).build();
        Checkpoint anotherStart = CheckpointBuilder.aCheckpoint().from(sampleCheckpoint).build();

        underTest.createCheckpoint(start.getName(), start.getCourse(), start.getType(), start.getLocation());
        underTest.createCheckpoint(anotherStart.getName(), anotherStart.getCourse(), anotherStart.getType(), anotherStart.getLocation());
    }

    @Test(expected = CourseAlreadyCompleteException.class)
    public void shouldNotBeAbleToCreateStageAfterFinishCheckpointCreated() {
        Checkpoint start = CheckpointBuilder.aCheckpoint().from(sampleCheckpoint).withName(STAGE_STAGE_NAME).withType(CheckpointType.START).build();
        Checkpoint stage = CheckpointBuilder.aCheckpoint().from(sampleCheckpoint).withName(STAGE_STAGE_NAME).withType(CheckpointType.STAGE).build();
        Checkpoint finish = CheckpointBuilder.aCheckpoint().from(sampleCheckpoint).withName(FINISH_STAGE_NAME).withType(CheckpointType.FINISH).build();

        underTest.createCheckpoint(start.getName(),start.getCourse(), start.getType(), start.getLocation());
        underTest.createCheckpoint(finish.getName(),finish.getCourse(), finish.getType(), finish.getLocation());
        underTest.createCheckpoint(stage.getName(),stage.getCourse(), stage.getType(), stage.getLocation());
    }

    @Test(expected = StartMustStartException.class)
    public void mustAddAStartCheckpointFirst() {
        Checkpoint stage = getSampleCheckpoint(CheckpointType.STAGE);

        underTest.createCheckpoint(stage.getName(),stage.getCourse(), stage.getType(), stage.getLocation());
    }

    @Test(expected = CheckpointWithSameNameException.class)
    public void mustNotHaveTheSameNameAsExistingCheckpointOnCourse() {
        Checkpoint start = CheckpointBuilder.aCheckpoint().from(sampleCheckpoint).withType(CheckpointType.START).build();
        Checkpoint stage1 = CheckpointBuilder.aCheckpoint().from(sampleCheckpoint).withType(CheckpointType.STAGE).build();
        Checkpoint stage2 = CheckpointBuilder.aCheckpoint().from(sampleCheckpoint).withType(CheckpointType.STAGE).build();

        underTest.createCheckpoint(start.getName(),start.getCourse(), start.getType(), start.getLocation());
        underTest.createCheckpoint(stage1.getName(),stage1.getCourse(), stage1.getType(), stage1.getLocation());
        underTest.createCheckpoint(stage2.getName(),stage2.getCourse(), stage2.getType(), stage2.getLocation());
    }

    @Test
    public void shouldReturnCheckpointIfNearLocation() {
        int RADIUS = 2;

        FileConfiguration fileConfiguration = mock(FileConfiguration.class);
        when(plugin.getConfig()).thenReturn(fileConfiguration);
        when(fileConfiguration.getInt("checkpoint-radius")).thenReturn(RADIUS);

        Location A = randomLocation;
        Location closeToA = new Location(TEST_WORLD, A.getX() + RADIUS, A.getY() + RADIUS, A.getZ() + RADIUS, N);

        Location B = new Location(TEST_WORLD, 13,50,10,N);

        underTest.createCheckpoint(TEST_CHECKPOINT, TEST_COURSE, CheckpointType.START, A);

        assertThat(underTest.isCheckpoint(A).isPresent(), is(true));
        assertThat(underTest.isCheckpoint(closeToA).isPresent(), is(true));
        assertThat(underTest.isCheckpoint(B).isPresent(), is(false));

        assertReflectionEquals(underTest.isCheckpoint(A).get().getLocation(), A);
    }

    @Test
    public void removeCourse() {
        List<Checkpoint> expectedToBeRemoved = new ArrayList<>();
        List<Checkpoint> expectedToRemain = new ArrayList<>();

        expectedToBeRemoved.add(underTest.createCheckpoint("A-Start", "ACourse", CheckpointType.START, randomLocation));
        expectedToBeRemoved.add(underTest.createCheckpoint("A-Finish", "ACourse", CheckpointType.FINISH, randomLocation));

        expectedToRemain.add(underTest.createCheckpoint("B-Start", "BCourse", CheckpointType.START, randomLocation));
        expectedToRemain.add(underTest.createCheckpoint("B-Finish", "BCourse", CheckpointType.FINISH, randomLocation));

        assertThat(underTest.getCheckpoints().size(), is(4));

        underTest.removeCourse("ACourse");

        List<Checkpoint> result = underTest.getCheckpoints();

        assertReflectionEquals(result, expectedToRemain);

        expectedToBeRemoved.forEach(cp -> {
            Mockito.verify(checkpointArtist, times(1)).eraseCheckpoint(cp);
        });

        expectedToRemain.forEach(cp -> {
            Mockito.verify(checkpointArtist, times(0)).eraseCheckpoint(cp);
        });
    }

    @Test
    public void getCourse() {

        List<Checkpoint> expectedResult = Arrays.asList(
                CheckpointBuilder.aCheckpoint().from(sampleCheckpoint).withName("1").build(),
                CheckpointBuilder.aCheckpoint().from(sampleCheckpoint).withName("2").withType(CheckpointType.FINISH).build()
        );

        underTest.createCheckpoint("1", TEST_COURSE, CheckpointType.START, randomLocation);
        underTest.createCheckpoint("2", TEST_COURSE, CheckpointType.FINISH, randomLocation);

        List<Checkpoint> result = underTest.getCourse(TEST_COURSE);

        assertReflectionEquals(result, expectedResult);
    }

    @Test
    public void getNoOfCheckpoints() {
        underTest.createCheckpoint(START_STAGE_NAME, TEST_COURSE, CheckpointType.START, randomLocation);
        underTest.createCheckpoint(STAGE_STAGE_NAME, TEST_COURSE, CheckpointType.FINISH, randomLocation);

        assertThat(underTest.getNoOfCheckpoints(TEST_COURSE), is(2));
    }

    @After
    public void cleanUp() throws Exception{
        removeConfigFileFromOutputDirectory();
    }

    private void removeConfigFileFromOutputDirectory() throws IOException {
        Files.deleteIfExists(new File(TEST_OUTPUT_FOLDER, TEST_FILE_NAME).toPath() );
    }
    
    private Checkpoint getSampleCheckpoint(CheckpointType checkpointType) {
        return CheckpointBuilder.aCheckpoint().withType(checkpointType).build();
    }

}