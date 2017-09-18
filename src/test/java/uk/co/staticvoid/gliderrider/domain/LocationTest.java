package uk.co.staticvoid.gliderrider.domain;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class LocationTest {

    private static final String WORLD = "TestWorld";
    private Location underTest = new Location(WORLD, 1, 2, 3, Direction.N);

    @Test
    public void cloneTest() {
        Location result = underTest.clone();

        assertThat(result.getWorld(), is(WORLD));
        assertThat(result.getX(), is(1));
        assertThat(result.getY(), is(2));
        assertThat(result.getZ(), is(3));
    }

    @Test
    public void addX() throws Exception {
        Location result = underTest.addX(1);
        assertThat(result.getX(), is(2));
    }

    @Test
    public void addY() throws Exception {
        Location result = underTest.addY(1);
        assertThat(result.getY(), is(3));
    }

    @Test
    public void addZ() throws Exception {
        Location result = underTest.addZ(1);
        assertThat(result.getZ(), is(4));
    }

    @Test
    public void minusX() throws Exception {
        Location result = underTest.minusX(1);
        assertThat(result.getX(), is(0));
    }

    @Test
    public void minusY() throws Exception {
        Location result = underTest.minusY(1);
        assertThat(result.getY(), is(1));

    }

    @Test
    public void minusZ() throws Exception {
        Location result = underTest.minusZ(1);
        assertThat(result.getZ(), is(2));
    }

}