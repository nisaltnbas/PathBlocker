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
    // İki boyutlu Integer değerlerin tutulacağı liste
    private ArrayList<ArrayList<Integer>> values;

    public Map(ArrayList<ArrayList<Integer>> values) {
        this.values = values;
    }

    public ArrayList<ArrayList<Integer>> getValues() {
        return values;
    }

    public static ArrayList<Map> readMaps(String[] filePaths) {
        ArrayList<Map> maps = new ArrayList<>();

        // Her dosya yolu için ayrı bir Map oluşturuyoruz
        for (String filePath : filePaths) {
            ArrayList<ArrayList<Integer>> values = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] tokens = line.split(" ");
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

    // Haritayı bir PNG dosyasına çevirme
    public void saveAsPng(String fileName) {
        int width = values.get(0).size();
        int height = values.size();
        int borderThickness = 1; // Dış sınır kalınlığı

        // Dış sınırları da eklemek için görüntü boyutlarını artır
        BufferedImage image = new BufferedImage(width + 2 * borderThickness, height + 2 * borderThickness,
                BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();

        // Koyu mor duvar rengini tanımla
        Color borderColor = new Color(75, 0, 130); // Koyu mor

        // Tüm haritanın etrafını koyu mor renkle doldur
        g.setColor(borderColor);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());

        // Harita içeriklerini çiz
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int value = values.get(y).get(x);
                Color color;

                // Hücre rengine göre doldurma rengi seç
                switch (value) {
                    case 0:
                        color = new Color(128, 128, 255);
                        break;
                    case 1:
                        color = new Color(75, 0, 130); // Koyu mor - Duvar
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

                // Ana hücre rengi
                g.setColor(color);
                g.fillRect(x + borderThickness, y + borderThickness, 1, 1);
            }
        }
        g.dispose();

        // Görseli kaydetme
        try {
            ImageIO.write(image, "png", new File(fileName));
            System.out.println(fileName + " kaydedildi.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String[] filePaths = {
                "level1.txt", "level2.txt", "level3.txt", "level4.txt", "level5.txt",
                "level6.txt", "level7.txt", "level8.txt", "level9.txt", "level10.txt"
        };

        ArrayList<Map> maps = Map.readMaps(filePaths);

        for (int i = 0; i < maps.size(); i++) {
            Map map = maps.get(i);
            String fileName = "map" + (i + 1) + ".png";
            map.saveAsPng(fileName);
        }
    }
}
