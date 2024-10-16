import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

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
                    // Satırdaki integer değerler listeye eklendi
                    values.add(rowValues);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Okunan değerleri içeren yeni bir Map nesnesi oluşturuyor ve listeye ekliyoruz
            maps.add(new Map(values));
        }

        return maps;
    }

    public static void main(String[] args) {
        // 10 farklı dosya yolu
        String[] filePaths = {
                "level1.txt", "level2.txt", "level3.txt", "level4.txt", "level5.txt",
                "level6.txt", "level7.txt", "level8.txt", "level9.txt", "level10.txt"
        };

        ArrayList<Map> maps = Map.readMaps(filePaths);

        // Test etmek için, her Map objesindeki iki boyutlu değerleri yazdırın
        for (int i = 0; i < maps.size(); i++) {
            System.out.println("Map " + (i + 1) + ":");
            for (ArrayList<Integer> row : maps.get(i).getValues()) {
                System.out.println(row);
            }
        }
    }
}
