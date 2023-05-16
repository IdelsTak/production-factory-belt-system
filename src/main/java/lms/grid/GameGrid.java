package lms.grid;

import java.util.HashMap;
import java.util.Map;

/**
 * The GameGrid is responsible for managing the state and initialisation of the
 * game's grid. It provides the Map structure to hold the coordinates of each
 * node in the grid. It also maintains the size of the grid using a range
 * variable. The range value donates how many nodes each hexagonal grid node
 * extends to.
 *
 * @ass2
 * @version 1.1
 * <p>
 * Summary: Initializes a grid of the game.
 *
 */
public class GameGrid {

    private final int range;

    /**
     * Create a new GameGrid with the given range, stored in a Map. A private
     * helper method, generate() has been provided to assist you with the logic
     * of the hexagonal coordinate system.
     *
     * @param range The range of the grid.
     */
    public GameGrid(int range) {
        this.range = range;
    }

    /**
     * Get a copy of the grid of the game.
     *
     * @return A copy of the grid of the game.
     */
    public Map<Coordinate, GridComponent> getGrid() {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the range of the grid that was stored when the GameGrid instance was
     * constructed.
     *
     * @return The range of the grid.
     */
    public int getRange() {
        return range;
    }

    /**
     * Set the GridComponent at the given coordinate.
     *
     * @param coordinate The coordinate of the GridComponent.
     * @param component The GridComponent to be set.
     */
    public void setCoordinate(Coordinate coordinate, GridComponent component) {
        throw new UnsupportedOperationException();
    }

    /**
     * Helper method: Generates a grid with the given range, starting from the
     * origin (the centre) and maintaining a balanced shape for the entire
     * mapping structure. This has been provided to support you with the
     * hexagonal coordinate logic.
     *
     * @param range The range of the map.
     * @provided
     */
    private Map<Coordinate, GridComponent> generate(int range) {
        Map<Coordinate, GridComponent> tempGrid = new HashMap<>();
        for (int q = -range; q <= range; q++) { // From negative to positive (inclusive)
            for (int r = -range; r <= range; r++) { // From negative to positive (inclusive)
                for (int s = -range; s <= range; s++) { // From negative to positive (inclusive)
                    if (q + r + s == 0) {
                        // Useful to default to error
                        tempGrid.put(new Coordinate(q, r, s), () -> "ERROR");
                    }
                }
            }
        }
        return tempGrid;
    }
}
