import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;

public class PathBlocker {
    private Map map;
    private Player player;
    private int targetX;
    private int targetY;
    private int moveCount = 0; // Hareket sayısını takip eder

    public PathBlocker(Map map) {
        this.map = map;
        initializeGame();
    }

    // Initialize the game by finding the player's and target's positions
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

    // Main game loop
    public void play() {
        Scanner scanner = new Scanner(System.in);
        boolean gameWon = false;

        while (!gameWon) {
            displayMap();
            System.out.print("Move (W/A/S/D): ");
            String input = scanner.nextLine().trim().toUpperCase();

            int dirX = 0;
            int dirY = 0;

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
                    System.out.println("Invalid input! Use W/A/S/D to move.");
                    continue;
            }

            // Remember the path the player will pass through
            ArrayList<int[]> path = new ArrayList<>();
            int currentX = player.getX();
            int currentY = player.getY();
            boolean canMove = false;

            while (true) {
                int nextX = currentX + dirX;
                int nextY = currentY + dirY;

                if (!isValidMove(nextX, nextY)) {
                    break; // Can't move further
                }

                int nextValue = map.getValues().get(nextY).get(nextX);

                // Add the current position to the path
                path.add(new int[] { currentX, currentY });

                // Move to the next position
                currentX = nextX;
                currentY = nextY;
                canMove = true;

                // If we've reached the target, we need to stop moving
                if (nextValue == 2) {
                    break;
                }
            }

            // If the player didn't move
            if (!canMove) {
                System.out.println("You can't move in that direction!");
                continue;
            }

            // Turn all cells in the path into walls, except for the last one (the player's
            // new position)
            for (int i = 0; i < path.size(); i++) {
                int[] pos = path.get(i);
                int x = pos[0];
                int y = pos[1];

                // If the cell is the target, we don't turn it into a wall
                if (x == targetX && y == targetY) {
                    continue;
                }

                // Set the cell to a wallD
                setCell(x, y, 1);
            }

            // Move the player to the new position
            movePlayer(currentX, currentY);

            // Her hareketten sonra PNG dosyasını kaydet
            moveCount++; // Hareket sayısını artır
            String fileName = String.format("level01/%04d.png", moveCount);

            // Dosya kaydetmeden önce klasör oluştur
            ensureDirectoryExists("level01");

            map.saveAsPng(fileName); // PNG olarak kaydet
            System.out.println(fileName + " has been saved.");

            // Check if the player has reached the target
            if (currentX == targetX && currentY == targetY) {
                gameWon = true;
                displayMap();
                System.out.println("Congratulations! You've reached the target!");
            } else if (!hasValidMoves()) {
                // Check if the player has any valid moves left
                displayMap();
                System.out.println("No more valid moves! Game over.");
                break;
            }
        }

        scanner.close();
    }

    // Check if there are any valid moves left for the player
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

                if (!isValidMove(nextX, nextY)) {
                    break;
                }

                int value = map.getValues().get(nextY).get(nextX);

                if (value != 1) {
                    return true;
                }

                x = nextX;
                y = nextY;
            }
        }
        return false;
    }

    // Set a cell's value on the map
    private void setCell(int x, int y, int value) {
        map.getValues().get(y).set(x, value);
    }

    // Check if the move is valid (not moving into a wall)
    private boolean isValidMove(int x, int y) {
        ArrayList<ArrayList<Integer>> values = map.getValues();
        if (y < 0 || y >= values.size() || x < 0 || x >= values.get(y).size()) {
            return false;
        }
        int value = values.get(y).get(x);
        return value != 1; // Can't move into walls
    }

    // Update the player's position on the map
    private void movePlayer(int x, int y) {
        // Set the new position to the player (3)
        setCell(x, y, 3);
        player.setPosition(x, y);
    }

    // Display the current state of the map using numbers
    private void displayMap() {
        ArrayList<ArrayList<Integer>> values = map.getValues();
        for (ArrayList<Integer> row : values) {
            for (Integer cell : row) {
                System.out.print(cell + " ");
            }
            System.out.println();
        }
    }

    // Ensure that the directory exists, if not, create it
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

        // Start the game with the first map
        Map map = maps.get(0); // Change the index to select a different level
        PathBlocker game = new PathBlocker(map);
        game.play();
    }
}
