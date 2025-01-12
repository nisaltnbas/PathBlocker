package game.model;

/**
 * Represents the elevation (height) of cells in the game map.
 */
public class Elevation {
    private int[][] heights;
    private final int size;
    private final Scene scene;

    public Elevation(int size) {
        this(size, 3); // Default 3 pyramids
    }

    public Elevation(int size, int pyramidCount) {
        this.size = size;
        this.heights = new int[size][size];
        this.scene = new Scene(size, pyramidCount);
        generateElevations();
    }

    private void generateElevations() {
        // Copy elevations from Scene to our heights array
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                heights[y][x] = scene.getElevation(x, y);
            }
        }
    }

    public int getHeight(int x, int y) {
        if (x >= 0 && x < size && y >= 0 && y < size) {
            return heights[y][x];
        }
        return -1;
    }

    public int getMovementCost(int x, int y) {
        int height = getHeight(x, y);
        return height + 1; // Height + 1 cost
    }

    @Override
    public String toString() {
        return scene.toString();
    }
}