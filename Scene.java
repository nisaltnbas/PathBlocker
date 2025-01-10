import java.util.Random;

public class Scene {
    public final int size;
    private final int[][] elevations;
    private static Random rand = new Random();

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
        if (x >= 0 && x < size && y >= 0 && y < size)
            return elevations[y][x];
        else
            return -1;
    }

    public static int[][] createElevationGrid(int size) {
        int[][] elevations = new int[size][];

        for (int i = 0; i < size; i++) {
            elevations[i] = new int[size];
        }

        return elevations;
    }

    public static int validSize(int size) {
        return Math.min(Math.max(size, 8), 60);
    }

    public void generatePyramids(int pyramidCount) {
        pyramidCount = Math.min(Math.max(pyramidCount, 1), 8);

        final int band = 4;
        final int r1 = -band;
        final int r2 = size + band;

        // Daha yumuşak geçişler için top_level'i ayarlayalım
        int top_level = 9;

        for (int i = 0; i < pyramidCount; i++) {
            int x_center = rand.nextInt(r2 - r1) + r1;
            int y_center = rand.nextInt(r2 - r1) + r1;

            for (int y = 0; y < elevations.length; y++) {
                int[] row = elevations[y];
                int delta_y = Math.abs(y_center - y);

                for (int x = 0; x < row.length; x++) {
                    int delta_x = Math.abs(x_center - x);
                    // Daha yumuşak geçişler için mesafe hesabını değiştirelim
                    double distance = Math.sqrt(delta_x * delta_x + delta_y * delta_y);
                    // Daha yumuşak eğim için distance çarpanını azaltalım
                    int height = Math.max((int) (top_level - (distance * 1.2)), 0);

                    // Mevcut yükseklikle karşılaştır ve en yükseği al
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
                if (row[x] == 0)
                    sb.append(".");
                else if (row[x] > 9)
                    sb.append("*");
                else
                    sb.append((char) ((int) '0' + row[x]));
            }

            sb.append("\n");
        }

        return sb.toString();
    }
}