package uk.co.staticvoid.gliderrider.business;

import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.co.staticvoid.gliderrider.domain.Attempt;
import uk.co.staticvoid.gliderrider.domain.Checkpoint;
import uk.co.staticvoid.gliderrider.domain.CheckpointType;
import uk.co.staticvoid.gliderrider.domain.Location;
import uk.co.staticvoid.gliderrider.exception.PlayerCheatedException;
import uk.co.staticvoid.gliderrider.helper.CheckpointBuilder;
import uk.co.staticvoid.gliderrider.helper.TimeProvider;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

public class BookkeeperTest {

    private static final String PLAYER_NAME = "TestPlayer";
    private static final String START_CHECKPOINT_NAME = "StartCheckpoint";
    private static final String STAGE_CHECKPOINT_NAME = "StageCheckpoint";
    private static final String FINISH_CHECKPOINT_NAME = "FinishCheckpoint";
    private static final String COURSE = "TestCourse";
    private static final Integer RADIUS = 2;
    private static final Long TIME_NOW = DateTime.now().getMillis();

    private Location location = mock(Location.class);
    private TimeProvider timeProvider = mock(TimeProvider.class);
    private RecordManager recordManager = mock(RecordManager.class);
    private CheckpointManager checkpointManager = mock(CheckpointManager.class);

    private Checkpoint startCheckpoint;
    private Checkpoint stageCheckpoint;
    private Checkpoint finishCheckpoint;

    private Bookkeeper underTest = new Bookkeeper(timeProvider, recordManager, checkpointManager);

    @Before
    public void setup() throws PlayerCheatedException {
        startCheckpoint = getSampleCheckpoint(START_CHECKPOINT_NAME, CheckpointType.START);
        stageCheckpoint = getSampleCheckpoint(STAGE_CHECKPOINT_NAME, CheckpointType.STAGE);
        finishCheckpoint = getSampleCheckpoint(FINISH_CHECKPOINT_NAME, CheckpointType.FINISH);

        when(timeProvider.timeNow()).thenReturn(TIME_NOW);
        when(checkpointManager.getCourse(COURSE)).thenReturn(Arrays.asList(startCheckpoint, stageCheckpoint, finishCheckpoint));
    }

    @Test
    public void shouldRecordAnAttemptWhenPlayerIsSeenAtCheckpoint() throws PlayerCheatedException {

        Attempt expectedAttempt = new Attempt(PLAYER_NAME, COURSE, getSampleTimeEntry(START_CHECKPOINT_NAME));

        underTest.seen(PLAYER_NAME, startCheckpoint);

        Optional<Attempt> result = underTest.getAttempt(PLAYER_NAME, COURSE);

        assertReflectionEquals(result.get(), expectedAttempt);
    }

    @Test
    public void shouldOnlyAllowAttemptsFromTheStart() throws PlayerCheatedException {
        underTest.seen(PLAYER_NAME, stageCheckpoint);
        underTest.seen(PLAYER_NAME, finishCheckpoint);

        assertThat(underTest.getAttempt(PLAYER_NAME, COURSE), Matchers.is(Optional.empty()));
    }

    @Test
    public void shouldAddStageTimeToAnAttemptIfThereIsAnExistingAttempt() throws PlayerCheatedException {
        Map<String, Long> expectedTimeMap = new LinkedHashMap<>();
        expectedTimeMap.put(START_CHECKPOINT_NAME, TIME_NOW);
        expectedTimeMap.put(STAGE_CHECKPOINT_NAME, TIME_NOW);
        Attempt expectedAttempt = new Attempt(PLAYER_NAME, COURSE, expectedTimeMap);

        underTest.seen(PLAYER_NAME, startCheckpoint);
        underTest.seen(PLAYER_NAME, stageCheckpoint);

        assertReflectionEquals(expectedAttempt, underTest.getAttempt(PLAYER_NAME, COURSE).get());
    }

    @Test
    public void shouldMarkAnAttemptedFinishedWhenFinishCheckpointReached() throws PlayerCheatedException {
        underTest.seen(PLAYER_NAME, startCheckpoint);
        underTest.seen(PLAYER_NAME, stageCheckpoint);
        underTest.seen(PLAYER_NAME, finishCheckpoint);

        assertThat(underTest.getAttempt(PLAYER_NAME, COURSE).get().isFinished(), Matchers.is(true));
    }

    @Test(expected = PlayerCheatedException.class)
    public void shouldNotNotifyRecordManagerUnlessAllTheCourseCheckpointsWerePassed() throws PlayerCheatedException {
        underTest.seen(PLAYER_NAME, startCheckpoint);
        underTest.seen(PLAYER_NAME, finishCheckpoint);

        Mockito.verify(recordManager, times(0)).addRecord(underTest.getAttempt(PLAYER_NAME, COURSE).get());
    }

    @Test
    public void shouldNotifyRecordManagerWhenFinalCheckpointReached() throws PlayerCheatedException {
        underTest.seen(PLAYER_NAME, startCheckpoint);
        underTest.seen(PLAYER_NAME, stageCheckpoint);
        underTest.seen(PLAYER_NAME, finishCheckpoint);

        Mockito.verify(recordManager, times(1)).addRecord(underTest.getAttempt(PLAYER_NAME, COURSE).get());
    }

    @Test
    public void shouldOnlyHaveOneAttemptHappeningAtOneTime() throws PlayerCheatedException {
        Map<String, Long> expectedTimeMap = new LinkedHashMap<>();
        expectedTimeMap.put(START_CHECKPOINT_NAME, TIME_NOW);
        expectedTimeMap.put(STAGE_CHECKPOINT_NAME, TIME_NOW);
        expectedTimeMap.put(FINISH_CHECKPOINT_NAME, TIME_NOW);
        Attempt expectedAttempt = new Attempt(PLAYER_NAME, COURSE, expectedTimeMap);
        expectedAttempt.setFinished(true);

        underTest.seen(PLAYER_NAME, startCheckpoint);
        underTest.seen(PLAYER_NAME, startCheckpoint);
        underTest.seen(PLAYER_NAME, startCheckpoint);
        underTest.seen(PLAYER_NAME, startCheckpoint);
        underTest.seen(PLAYER_NAME, startCheckpoint);
        underTest.seen(PLAYER_NAME, startCheckpoint);
        underTest.seen(PLAYER_NAME, startCheckpoint);
        underTest.seen(PLAYER_NAME, startCheckpoint);
        underTest.seen(PLAYER_NAME, stageCheckpoint);
        underTest.seen(PLAYER_NAME, stageCheckpoint);
        underTest.seen(PLAYER_NAME, stageCheckpoint);
        underTest.seen(PLAYER_NAME, stageCheckpoint);
        underTest.seen(PLAYER_NAME, stageCheckpoint);
        underTest.seen(PLAYER_NAME, stageCheckpoint);
        underTest.seen(PLAYER_NAME, stageCheckpoint);
        underTest.seen(PLAYER_NAME, finishCheckpoint);
        underTest.seen(PLAYER_NAME, finishCheckpoint);
        underTest.seen(PLAYER_NAME, finishCheckpoint);
        underTest.seen(PLAYER_NAME, finishCheckpoint);
        underTest.seen(PLAYER_NAME, finishCheckpoint);
        underTest.seen(PLAYER_NAME, finishCheckpoint);
        underTest.seen(PLAYER_NAME, finishCheckpoint);

        assertReflectionEquals(expectedAttempt, underTest.getAttempt(PLAYER_NAME, COURSE).get());
    }

    private Checkpoint getSampleCheckpoint(String name, CheckpointType type) {
        return CheckpointBuilder.aCheckpoint().withName(name).withCourse(COURSE).withType(type).withLocation(location).withRadius(RADIUS).build();
    }

    private Map<String, Long> getSampleTimeEntry(String checkpointName) {
        Map<String, Long> timeMap = new HashMap<>();
        timeMap.put(checkpointName, TIME_NOW);
        return timeMap;
    }
}
