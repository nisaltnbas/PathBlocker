import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class ChartMap {
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
            ArrayList<ArrayList<Integer>> values = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
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
                e.printStackTrace();
            }
            maps.add(new ChartMap(values));
        }

        return maps;
    }

    // Method to save the map as a PNG image
    public void saveAsPng(String fileName) {
        int width = values.get(0).size();
        int height = values.size();
        int blockSize = 40;

        BufferedImage image = new BufferedImage(
                width * blockSize,
                height * blockSize,
                BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();

        // Draw the map
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int value = values.get(y).get(x);
                Color color;

                switch (value) {
                    case 0: // Empty cell - gray gradient based on elevation
                        int elevation = getElevationColor(x, y);
                        color = new Color(elevation, elevation, elevation);
                        break;
                    case 1: // Wall - dark red
                        color = new Color(139, 0, 0); // Dark red
                        break;
                    case 2: // Target - blue
                        color = new Color(0, 0, 255);
                        break;
                    case 3: // Player - green
                        color = new Color(0, 255, 0);
                        break;
                    default:
                        color = Color.WHITE;
                        break;
                }

                g.setColor(color);
                g.fillRect(x * blockSize, y * blockSize, blockSize, blockSize);

                // Draw grid lines
                g.setColor(Color.DARK_GRAY);
                g.drawRect(x * blockSize, y * blockSize, blockSize, blockSize);
            }
        }
        g.dispose();

        try {
            ImageIO.write(image, "png", new File(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setElevation(Elevation elevation) {
        this.elevation = elevation;
    }

    private int getElevationColor(int x, int y) {
        if (elevation != null) {
            int height = elevation.getHeight(x, y);
            // Yüksekliğe göre daha koyu gri tonları (0=en açık, 9=en koyu)
            int baseGray = 180; // Başlangıç gri tonu (daha açık)
            int step = 15; // Her yükseklik için koyulaşma miktarı
            return Math.max(baseGray - (height * step), 60); // En koyu 60 olsun
        }
        return 128; // Elevation yoksa orta gri
    }
}
