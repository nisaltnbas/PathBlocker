package game.graphics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

import game.model.CellType;
import game.model.Elevation;
import game.util.FileUtil;

/**
 * Handles the graphical representation and file I/O of the game map.
 */
public class ChartMap {
    private static final int BLOCK_SIZE = 40;
    private static final Color FRAME_COLOR = new Color(139, 0, 0); // Dark red
    private static final Color WALL_COLOR = new Color(139, 0, 0);
    private static final Color TARGET_COLOR = new Color(0, 0, 255);
    private static final Color PLAYER_COLOR = new Color(0, 255, 0);
    private static final Color GRID_COLOR = Color.DARK_GRAY;

    private ArrayList<ArrayList<Integer>> values;
    private Elevation elevation;

    public ChartMap(ArrayList<ArrayList<Integer>> values) {
        this.values = values;
    }

    public ArrayList<ArrayList<Integer>> getValues() {
        return values;
    }

    public static ArrayList<ChartMap> readMaps(String[] filePaths) {
        ArrayList<ChartMap> maps = new ArrayList<>();

        for (String filePath : filePaths) {
            String fullPath = FileUtil.getProjectPath(filePath);
            ArrayList<ArrayList<Integer>> values = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(fullPath))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] tokens = line.trim().split("\\s+");
                    ArrayList<Integer> rowValues = new ArrayList<>();
                    for (String token : tokens) {
                        rowValues.add(Integer.parseInt(token));
                    }
                    values.add(rowValues);
                }
            } catch (IOException e) {
                System.err.println("Error reading file: " + fullPath);
                e.printStackTrace();
            }
            maps.add(new ChartMap(values));
        }

        return maps;
    }

    public void saveAsPng(String fileName) {
        int width = values.get(0).size();
        int height = values.size();

        BufferedImage image = createImage(width, height);
        Graphics g = image.getGraphics();

        drawFrame(g, width, height);
        drawMap(g, width, height);

        g.dispose();

        try {
            ImageIO.write(image, "png", new File(fileName));
        } catch (IOException e) {
            System.err.println("Error saving PNG: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private BufferedImage createImage(int width, int height) {
        return new BufferedImage(
                (width + 2) * BLOCK_SIZE,
                (height + 2) * BLOCK_SIZE,
                BufferedImage.TYPE_INT_RGB);
    }

    private void drawFrame(Graphics g, int width, int height) {
        g.setColor(FRAME_COLOR);
        for (int y = 0; y < height + 2; y++) {
            for (int x = 0; x < width + 2; x++) {
                if (y == 0 || y == height + 1 || x == 0 || x == width + 1) {
                    g.fillRect(x * BLOCK_SIZE, y * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
                }
            }
        }
    }

    private void drawMap(Graphics g, int width, int height) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int value = values.get(y).get(x);
                CellType cellType = CellType.fromValue(value);
                Color color = getCellColor(cellType, x, y);

                g.setColor(color);
                g.fillRect((x + 1) * BLOCK_SIZE, (y + 1) * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);

                g.setColor(GRID_COLOR);
                g.drawRect((x + 1) * BLOCK_SIZE, (y + 1) * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
            }
        }
    }

    private Color getCellColor(CellType cellType, int x, int y) {
        switch (cellType) {
            case EMPTY:
                int elevation = getElevationColor(x, y);
                return new Color(elevation, elevation, elevation);
            case WALL:
                return WALL_COLOR;
            case TARGET:
                return TARGET_COLOR;
            case PLAYER:
                return PLAYER_COLOR;
            default:
                return Color.WHITE;
        }
    }

    public void setElevation(Elevation elevation) {
        this.elevation = elevation;
    }

    private int getElevationColor(int x, int y) {
        if (elevation != null) {
            int height = elevation.getHeight(x, y);
            int baseGray = 180;
            int step = 15;
            return Math.max(baseGray - (height * step), 60);
        }
        return 128;
    }
}