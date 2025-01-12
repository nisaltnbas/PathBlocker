package game.model;

import java.util.Random;

/**
 * Represents the scene with elevation data and pyramid generation.
 */
public class Scene {
    private static final int MIN_SIZE = 8;
    private static final int MAX_SIZE = 60;
    private static final int MIN_PYRAMIDS = 1;
    private static final int MAX_PYRAMIDS = 8;
    private static final int BAND_SIZE = 4;
    private static final int TOP_LEVEL = 9;

    public final int size;
    private final int[][] elevations;
    private static final Random rand = new Random();

    public Scene(int size) {
        this.size = validSize(size);
        this.elevations = createElevationGrid(size);
    }

    public Scene(int size, int pyramidCount) {
        this.size = validSize(size);
        this.elevations = createElevationGrid(size);
        generatePyramids(pyramidCount);
    }

    public int getElevation(int x, int y) {
        if (x >= 0 && x < size && y >= 0 && y < size) {
            return elevations[y][x];
        }
        return -1;
    }

    private static int[][] createElevationGrid(int size) {
        int[][] elevations = new int[size][];
        for (int i = 0; i < size; i++) {
            elevations[i] = new int[size];
        }
        return elevations;
    }

    private static int validSize(int size) {
        return Math.min(Math.max(size, MIN_SIZE), MAX_SIZE);
    }

    private void generatePyramids(int pyramidCount) {
        pyramidCount = Math.min(Math.max(pyramidCount, MIN_PYRAMIDS), MAX_PYRAMIDS);

        final int r1 = -BAND_SIZE;
        final int r2 = size + BAND_SIZE;

        for (int i = 0; i < pyramidCount; i++) {
            int x_center = rand.nextInt(r2 - r1) + r1;
            int y_center = rand.nextInt(r2 - r1) + r1;

            for (int y = 0; y < elevations.length; y++) {
                int[] row = elevations[y];
                int delta_y = Math.abs(y_center - y);

                for (int x = 0; x < row.length; x++) {
                    int delta_x = Math.abs(x_center - x);
                    double distance = Math.sqrt(delta_x * delta_x + delta_y * delta_y);
                    int height = Math.max((int) (TOP_LEVEL - (distance * 1.2)), 0);
                    row[x] = Math.max(row[x], height);
                }
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < elevations.length; y++) {
            int[] row = elevations[y];
            for (int x = 0; x < row.length; x++) {
                if (row[x] == 0) {
                    sb.append(".");
                } else if (row[x] > 9) {
                    sb.append("*");
                } else {
                    sb.append((char) ((int) '0' + row[x]));
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}