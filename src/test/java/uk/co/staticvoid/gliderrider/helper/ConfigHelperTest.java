package uk.co.staticvoid.gliderrider.helper;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.unitils.reflectionassert.ReflectionComparatorMode;
import uk.co.staticvoid.gliderrider.GliderRider;
import uk.co.staticvoid.gliderrider.domain.Checkpoint;
import uk.co.staticvoid.gliderrider.domain.CheckpointType;
import uk.co.staticvoid.gliderrider.domain.Direction;
import uk.co.staticvoid.gliderrider.domain.Location;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

@RunWith(PowerMockRunner.class)
@PrepareForTest({GliderRider.class})
    public class ConfigHelperTest {

    private static final String TEST_OUTPUT_FOLDER = "./src/test/output/";
    private static final String TEST_FILE_NAME = "checkpoint.yml";

    private GliderRider plugin = mock(GliderRider.class);

    private ConfigHelper underTest = new ConfigHelper(plugin, TEST_FILE_NAME);

    @Before
    public void setup() {
        ConfigurationSerialization.registerClass(Checkpoint.class);
        ConfigurationSerialization.registerClass(Location.class);

        when(plugin.getDataFolder()).thenReturn(new File(TEST_OUTPUT_FOLDER));
    }

    @Test
    public void shouldSaveDefaultConfigWhenNoConfigPresent() {
        underTest = new ConfigHelper(plugin, TEST_FILE_NAME);

        underTest.saveDefaultConfig();
        verify(plugin, times(1)).saveResource(TEST_FILE_NAME, false);
    }

    @Test
    public void shouldNotSaveDefaultIfOnePresent() throws IOException {
        addConfigFileToOutputDirectory();

        underTest.saveDefaultConfig();
        verify(plugin, times(0)).saveResource(TEST_FILE_NAME, false);
    }

    @Test
    public void shouldReturnConfig() {
        FileConfiguration result = underTest.getConfig();
        assertThat(result.getDefaults(), is(notNullValue()));
    }

    @Test
    public void shouldSaveConfigChanges() {
        List<Checkpoint> sampleCheckpoints = createSampleCheckpoints();

        underTest.getConfig().set("test.checkpoint", sampleCheckpoints);

        underTest.saveConfig();

        @SuppressWarnings("unchecked")
        List<Checkpoint> resultCheckpointList = (List<Checkpoint>)
                underTest.getConfig().get("test.checkpoint");

        assertThat(resultCheckpointList, is(equalTo(sampleCheckpoints)));
    }

    @Test
    public void shouldLoadSavedConfig() {
        List<Checkpoint> sampleCheckpoints = createSampleCheckpoints();
        underTest.getConfig().set("test.checkpoint", sampleCheckpoints);
        underTest.saveConfig();

        underTest.reloadConfig();

        List<Checkpoint> expectedResult = createSampleCheckpoints();

        @SuppressWarnings("unchecked")
        List<Checkpoint> resultList = (List<Checkpoint>) underTest.getConfig().get("test.checkpoint");

        assertReflectionEquals(resultList, expectedResult, ReflectionComparatorMode.LENIENT_ORDER);
    }

    @After
    public void cleanUp() throws Exception{
        removeConfigFileFromOutputDirectory();
    }


    private void addConfigFileToOutputDirectory() throws IOException {
        Files.createFile(new File(TEST_OUTPUT_FOLDER, TEST_FILE_NAME).toPath());
    }

    private void removeConfigFileFromOutputDirectory() throws IOException {
        Files.deleteIfExists(new File(TEST_OUTPUT_FOLDER, TEST_FILE_NAME).toPath() );
    }

    private List<Checkpoint> createSampleCheckpoints() {
        Location randomLocation = new Location("world", 10, 10, 10, Direction.N);
        Checkpoint testCheckpoint_1 =  CheckpointBuilder.aCheckpoint().withName("A-Start").withCourse("x").withType(CheckpointType.START).withLocation(randomLocation).build();
        Checkpoint testCheckpoint_2 =  CheckpointBuilder.aCheckpoint().withName("B-Start").withCourse("y").withType(CheckpointType.START).withLocation(randomLocation).build();

        return Arrays.asList(testCheckpoint_1, testCheckpoint_2);
    }
}