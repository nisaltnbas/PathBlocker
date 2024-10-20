import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;

public class PathBlocker {
    private Map map;
    private Player player;
    private int targetX;
    private int targetY;
    private int moveCount = 0; // Hareket sayısını takip eder
    private String levelFolder; // Level klasörü adı

    public PathBlocker(Map map, String levelFolder) {
        this.map = map;
        this.levelFolder = levelFolder;
        initializeGame();
        ensureDirectoryExists(levelFolder); // Klasörün var olup olmadığını kontrol eder
    }

    // Oyunu başlatırken oyuncu ve hedef pozisyonunu bulur
    private void initializeGame() {
        ArrayList<ArrayList<Integer>> values = map.getValues();
        for (int y = 0; y < values.size(); y++) {
            for (int x = 0; x < values.get(y).size(); x++) {
                int value = values.get(y).get(x);
                if (value == 3) {
                    player = new Player(x, y);
                } else if (value == 2) {
                    targetX = x;
                    targetY = y;
                }
            }
        }
    }

    // Oyun oynanışı
    public void play() {
        Scanner scanner = new Scanner(System.in);
        boolean gameWon = false;

        // İlk olarak haritanın başlangıç halini kaydet
        saveCurrentMapState();

        while (!gameWon) {
            displayMap();
            System.out.print("Move (W/A/S/D): ");
            String input = scanner.nextLine().trim().toUpperCase();

            // Hareket yönlerini belirle
            int dirX = 0, dirY = 0;
            switch (input) {
                case "W":
                    dirY = -1;
                    break;
                case "S":
                    dirY = 1;
                    break;
                case "A":
                    dirX = -1;
                    break;
                case "D":
                    dirX = 1;
                    break;
                default:
                    System.out.println("Geçersiz giriş! W/A/S/D kullanın.");
                    continue;
            }

            // Oyuncunun hareket ettiği yol
            ArrayList<int[]> path = new ArrayList<>();
            int currentX = player.getX();
            int currentY = player.getY();
            boolean canMove = false;

            while (true) {
                int nextX = currentX + dirX;
                int nextY = currentY + dirY;

                if (!isValidMove(nextX, nextY))
                    break;

                int nextValue = map.getValues().get(nextY).get(nextX);

                path.add(new int[] { currentX, currentY });
                currentX = nextX;
                currentY = nextY;
                canMove = true;

                if (nextValue == 2)
                    break; // Hedefe ulaştıysa dur
            }

            if (!canMove) {
                System.out.println("Bu yöne hareket edemezsiniz!");
                continue;
            }

            // Yol boyunca duvarları yerleştir (hedef hariç)
            for (int i = 0; i < path.size(); i++) {
                int[] pos = path.get(i);
                int x = pos[0];
                int y = pos[1];
                if (x == targetX && y == targetY)
                    continue; // Hedefi duvar yapma
                setCell(x, y, 1); // Duvar yap
            }

            // Oyuncuyu yeni pozisyona taşı
            movePlayer(currentX, currentY);

            // Hareketten sonra haritayı PNG olarak kaydet
            saveCurrentMapState();

            // Oyunun bitip bitmediğini kontrol et
            if (currentX == targetX && currentY == targetY) {
                gameWon = true;
                displayMap();
                System.out.println("Tebrikler! Hedefe ulaştınız!");
            } else if (!hasValidMoves()) {
                displayMap();
                System.out.println("Hareket edebileceğiniz geçerli bir yol kalmadı! Oyun bitti.");
                break;
            }
        }

    }

    // Geçerli harita durumunu PNG olarak kaydet
    private void saveCurrentMapState() {
        moveCount++;
        String fileName = String.format("%s/%04d.png", levelFolder, moveCount);
        map.saveAsPng(fileName);
        System.out.println(fileName + " kaydedildi.");
    }

    // Oyuncunun geçerli bir hareket yapıp yapamayacağını kontrol eder
    private boolean hasValidMoves() {
        int[][] directions = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
        for (int[] dir : directions) {
            int x = player.getX();
            int y = player.getY();
            int dirX = dir[0];
            int dirY = dir[1];

            while (true) {
                int nextX = x + dirX;
                int nextY = y + dirY;

                if (!isValidMove(nextX, nextY))
                    break;

                int value = map.getValues().get(nextY).get(nextX);
                if (value != 1)
                    return true;

                x = nextX;
                y = nextY;
            }
        }
        return false;
    }

    // Geçerli bir hareket olup olmadığını kontrol eder
    private boolean isValidMove(int x, int y) {
        ArrayList<ArrayList<Integer>> values = map.getValues();
        if (y < 0 || y >= values.size() || x < 0 || x >= values.get(y).size()) {
            return false;
        }
        return values.get(y).get(x) != 1; // Duvara çarpmaz
    }

    // Bir hücrenin değerini günceller
    private void setCell(int x, int y, int value) {
        map.getValues().get(y).set(x, value);
    }

    // Oyuncuyu yeni pozisyona taşır
    private void movePlayer(int x, int y) {
        setCell(x, y, 3);
        player.setPosition(x, y);
    }

    // Haritayı ekrana yazdırır
    private void displayMap() {
        ArrayList<ArrayList<Integer>> values = map.getValues();
        for (ArrayList<Integer> row : values) {
            for (Integer cell : row) {
                System.out.print(cell + " ");
            }
            System.out.println();
        }
    }

    // Klasörün var olup olmadığını kontrol eder, yoksa oluşturur
    private void ensureDirectoryExists(String dirPath) {
        File directory = new File(dirPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    public static void main(String[] args) {
        String[] filePaths = {
                "level1.txt", "level2.txt", "level3.txt", "level4.txt", "level5.txt",
                "level6.txt", "level7.txt", "level8.txt", "level9.txt", "level10.txt"
        };

        ArrayList<Map> maps = Map.readMaps(filePaths);

        for (int i = 0; i < maps.size(); i++) {
            String levelFolder = String.format("level%02d", i + 1);
            Map map = maps.get(i);
            PathBlocker game = new PathBlocker(map, levelFolder);
            game.play();
        }
    }
}
