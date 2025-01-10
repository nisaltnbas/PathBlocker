public class Elevation {
    private int[][] heights;
    private final int size;
    private final Scene scene;
    private final int pyramidCount;

    public Elevation(int size) {
        this(size, 3); // Default 3 piramit
    }

    public Elevation(int size, int pyramidCount) {
        this.size = size;
        this.pyramidCount = pyramidCount;
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

        // Debug için yükseklikleri yazdır
        System.out.println("Generated elevation map with " + pyramidCount + " pyramids:");
        System.out.println(this.toString());
    }

    public int getHeight(int x, int y) {
        if (x >= 0 && x < size && y >= 0 && y < size) {
            return heights[y][x];
        }
        return -1;
    }

    public int getMovementCost(int x, int y) {
        int height = getHeight(x, y);
        return height + 1; // Yükseklik + 1 maliyet
    }

    @Override
    public String toString() {
        return scene.toString();
    }
}