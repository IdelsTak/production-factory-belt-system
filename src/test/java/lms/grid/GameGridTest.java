package lms.grid;

import java.util.Map;
import lms.logistics.belts.Belt;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

public class GameGridTest {

    private GameGrid gameGrid;

    @Before
    public void setup() {
        gameGrid = new GameGrid(1);
    }

    @Test
    public void testGetGrid() {
        for (Map.Entry<Coordinate, GridComponent> entry : gameGrid.getGrid().entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue().getEncoding());
        }

        assertEquals(7L, gameGrid.getGrid().entrySet().size());
    }


    @Test
    public void testGetRange() {
        assertEquals(1L, gameGrid.getRange());
    }

    @Test
    public void testSetCoordinate() {
        gameGrid.setCoordinate(new Coordinate(2, 3), new Belt(1));
        assertEquals(8L, gameGrid.getGrid().entrySet().size());
    }

}
