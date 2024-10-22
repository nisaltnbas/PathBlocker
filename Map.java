import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class Map {
    private ArrayList<ArrayList<Integer>> values;

    public Map(ArrayList<ArrayList<Integer>> values) {
        this.values = values;
    }

    public ArrayList<ArrayList<Integer>> getValues() {
        return values;
    }

    public static ArrayList<Map> readMaps(String[] filePaths) {
        ArrayList<Map> maps = new ArrayList<>();

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
            maps.add(new Map(values));
        }

        return maps;
    }

    // Method to save the map as a PNG image
    public void saveAsPng(String fileName) {
        int width = values.get(0).size();
        int height = values.size();
        int blockSize = 50; // Increase block size to create larger images
        int borderThickness = blockSize;

        BufferedImage image = new BufferedImage(
                (width * blockSize) + 2 * borderThickness, 
                (height * blockSize) + 2 * borderThickness,
                BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();

        Color borderColor = new Color(75, 0, 130);

        // Draw the outer border
        g.setColor(borderColor);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());

        // Draw the inner map with an additional wall around it
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int value = values.get(y).get(x);
                Color color;

                switch (value) {
                    case 0:
                        color = new Color(128, 128, 255);
                        break;
                    case 1:
                        color = new Color(75, 0, 130);
                        break;
                    case 2:
                        color = new Color(128, 128, 128);
                        break;
                    case 3:
                        color = Color.YELLOW;
                        break;
                    default:
                        color = Color.WHITE;
                        break;
                }

                g.setColor(color);
                g.fillRect(x * blockSize + borderThickness, y * blockSize + borderThickness, blockSize, blockSize);
            }
        }
        g.dispose();

        try {
            ImageIO.write(image, "png", new File(fileName));
            System.out.println(fileName + " has been saved.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
