package uk.co.staticvoid.gliderrider.business;

import org.bukkit.Material;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import uk.co.staticvoid.gliderrider.GliderRider;
import uk.co.staticvoid.gliderrider.domain.Checkpoint;
import uk.co.staticvoid.gliderrider.domain.CheckpointType;
import uk.co.staticvoid.gliderrider.domain.Direction;
import uk.co.staticvoid.gliderrider.domain.Location;
import uk.co.staticvoid.gliderrider.helper.CheckpointBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;


/**
 * Checkpoint is based on the following design:
 *
 *    1 2 3 4 5 6 7 8 9 10
 *  1         .
 *  2         .
 *  3     G o o o G
 *  4     o   .   o
 *  5 . . o . x . o . . . .
 *  6     o   .   o
 *  7     G o o o G
 *  8         .
 *  9         .
 *  10        .
 *
 *  Where in this example:
 *  x = (x:5, y:5)
 *  radius = 2
 *  total number of blocks =
 *  - 12 checkpoint blocks (o)
 *  - 4 glowstone blocks (G)
 *  = 16
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({GliderRider.class})
public class CheckpointArtistTest {

    private static final Integer RADIUS = 2;

    private BlockEditor blockEditor = mock(BlockEditor.class);
    private GliderRider plugin = PowerMockito.mock(GliderRider.class);

    private CheckpointArtist underTest = new CheckpointArtist(plugin, blockEditor);

    private Checkpoint sampleCheckpointNorth, sampleCheckpointEast;
    private Location sampleLocationNorth, sampleLocationEast;

    @Before
    public void setup() {
        sampleLocationNorth = new Location("world", 5, 5, 5, Direction.N);
        sampleCheckpointNorth = new Checkpoint("Test-Checkpoint", "TestCourse", CheckpointType.START, sampleLocationNorth, RADIUS);
        sampleLocationEast = new Location("world", 5, 5, 5, Direction.E);
        sampleCheckpointEast = new Checkpoint("Test-Checkpoint", "TestCourse", CheckpointType.START, sampleLocationEast, RADIUS);
    }

    @Test
    public void shouldUseMaterialBasedOnCheckpointType() {
        Checkpoint startCheckpoint = CheckpointBuilder.aCheckpoint().from(sampleCheckpointNorth).withType(CheckpointType.START).build();
        underTest.drawCheckpoint(startCheckpoint);
        verify(blockEditor, times(12)).changeBlockMaterial(anyObject(), Mockito.eq(CheckpointArtist.START_MATERIAL));

        Checkpoint stageCheckpoint = CheckpointBuilder.aCheckpoint().from(sampleCheckpointNorth).withType(CheckpointType.STAGE).build();
        underTest.drawCheckpoint(stageCheckpoint);
        verify(blockEditor, times(12)).changeBlockMaterial(anyObject(), Mockito.eq(CheckpointArtist.STAGE_MATERIAL));

        Checkpoint finishCheckpoint = CheckpointBuilder.aCheckpoint().from(sampleCheckpointNorth).withType(CheckpointType.FINISH).build();
        underTest.drawCheckpoint(finishCheckpoint);
        verify(blockEditor, times(12)).changeBlockMaterial(anyObject(), Mockito.eq(CheckpointArtist.FINISH_MATERIAL));
    }

    @Test
    public void shouldUseLightMaterialWhenBuildingACheckpoint() {
        Checkpoint startCheckpoint = CheckpointBuilder.aCheckpoint().from(sampleCheckpointNorth).withType(CheckpointType.START).build();
        underTest.drawCheckpoint(startCheckpoint);
        verify(blockEditor, times(4)).changeBlockMaterial(anyObject(), Mockito.eq(CheckpointArtist.LIGHT_MATERIAL));
    }

    @Test
    public void shouldReplaceBlocksForAirWhenRemoved() {
        Checkpoint startCheckpoint = CheckpointBuilder.aCheckpoint().from(sampleCheckpointNorth).build();
        underTest.eraseCheckpoint(startCheckpoint);
        verify(blockEditor, times(12 + 4)).changeBlockMaterial(anyObject(), Mockito.eq(Material.AIR));
    }

    @Test
    public void shouldCreateCheckpointWithCorrectBlockLocationsWhenFacingNorthSouth() {
        List<Location> expectedLocationList = new ArrayList<>();

        expectedLocationList.add(sampleLocationNorth.clone(4, 7, 5));
        expectedLocationList.add(sampleLocationNorth.clone(5, 7, 5));
        expectedLocationList.add(sampleLocationNorth.clone(6, 7, 5));

        expectedLocationList.add(sampleLocationNorth.clone(4, 3, 5));
        expectedLocationList.add(sampleLocationNorth.clone(5, 3, 5));
        expectedLocationList.add(sampleLocationNorth.clone(6, 3, 5));

        expectedLocationList.add(sampleLocationNorth.clone(3, 4, 5));
        expectedLocationList.add(sampleLocationNorth.clone(3, 5, 5));
        expectedLocationList.add(sampleLocationNorth.clone(3, 6, 5));

        expectedLocationList.add(sampleLocationNorth.clone(7, 4, 5));
        expectedLocationList.add(sampleLocationNorth.clone(7, 5, 5));
        expectedLocationList.add(sampleLocationNorth.clone(7, 6, 5));

        //Corners
        expectedLocationList.add(sampleLocationNorth.clone(3, 7, 5));
        expectedLocationList.add(sampleLocationNorth.clone(7, 7, 5));
        expectedLocationList.add(sampleLocationNorth.clone(7, 3, 5));
        expectedLocationList.add(sampleLocationNorth.clone(3, 3, 5));

        underTest.drawCheckpoint(sampleCheckpointNorth);

        expectedLocationList.forEach(expLoc -> {
            verify(blockEditor, times(1)).changeBlockMaterial(eq(expLoc), Mockito.anyObject());
        });
    }

    @Test
    public void shouldCreateCheckpointWithCorrectBlockLocationsWhenFacingEastWest() {
        List<Location> expectedLocationList = new ArrayList<>();

        expectedLocationList.add(sampleLocationEast.clone(5, 7, 4));
        expectedLocationList.add(sampleLocationEast.clone(5, 7, 5));
        expectedLocationList.add(sampleLocationEast.clone(5, 7, 6));

        expectedLocationList.add(sampleLocationEast.clone(5, 3, 4));
        expectedLocationList.add(sampleLocationEast.clone(5, 3, 5));
        expectedLocationList.add(sampleLocationEast.clone(5, 3, 6));

        expectedLocationList.add(sampleLocationEast.clone(5, 4, 3));
        expectedLocationList.add(sampleLocationEast.clone(5, 5, 3));
        expectedLocationList.add(sampleLocationEast.clone(5, 6, 3));

        expectedLocationList.add(sampleLocationEast.clone(5, 4, 7));
        expectedLocationList.add(sampleLocationEast.clone(5, 5, 7));
        expectedLocationList.add(sampleLocationEast.clone(5, 6, 7));

        //Corners
        expectedLocationList.add(sampleLocationEast.clone(5, 7, 7));
        expectedLocationList.add(sampleLocationEast.clone(5, 7, 3));
        expectedLocationList.add(sampleLocationEast.clone(5, 3, 7));
        expectedLocationList.add(sampleLocationEast.clone(5, 3, 7));

        underTest.drawCheckpoint(sampleCheckpointEast);

        expectedLocationList.forEach(expLoc -> {
            verify(blockEditor, times(1)).changeBlockMaterial(eq(expLoc), Mockito.anyObject());
        });
    }    

    @Test
    public void shouldDrawBlocksBasedOnRadius() {
        Checkpoint startCheckpoint = CheckpointBuilder.aCheckpoint().from(sampleCheckpointNorth).withRadius(4).build();
        underTest.drawCheckpoint(startCheckpoint);
        verify(blockEditor, times(32)).changeBlockMaterial(anyObject(), anyObject());
    }

}