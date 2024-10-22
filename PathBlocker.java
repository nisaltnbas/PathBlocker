import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;

class PathBlocker {
    private Map map;
    private Player player;
    private int targetX;
    private int targetY;
    private int moveCount = 0;
    private String levelFolder;
    private ArrayList<Map> winningMaps = new ArrayList<>();

    public PathBlocker(Map map, String levelFolder) {
        this.map = map;
        this.levelFolder = levelFolder;
        initializeGame();
        ensureDirectoryExists(levelFolder);
        saveInitialMapState();
    }

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

    public void play() {
        Scanner scanner = new Scanner(System.in);
        boolean gameWon = false;

        // Clear any previous winning moves
        winningMaps.clear();
        winningMaps.add(new Map(deepCopyValues(map.getValues()))); // Save initial state as part of winning moves

        while (!gameWon) {
            displayMap();
            System.out.print("Move (W/A/S/D): ");
            String input = scanner.nextLine().trim().toUpperCase();

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
                    System.out.println("Invalid input! Use W/A/S/D.");
                    continue;
            }

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

                path.add(new int[]{currentX, currentY});
                currentX = nextX;
                currentY = nextY;
                canMove = true;

                if (nextValue == 2)
                    break;
            }

            if (!canMove) {
                System.out.println("You cannot move in that direction!");
                continue;
            }

            for (int i = 0; i < path.size(); i++) {
                int[] pos = path.get(i);
                int x = pos[0];
                int y = pos[1];
                if (x == targetX && y == targetY)
                    continue;
                setCell(x, y, 1);
            }

            movePlayer(currentX, currentY);

            // Save current map state if game is still in progress
            winningMaps.add(new Map(deepCopyValues(map.getValues())));
            moveCount++;

            if (currentX == targetX && currentY == targetY) {
                gameWon = true;
                System.out.println("Congratulations! You reached the target!");
            } else if (!hasValidMoves()) {
                System.out.println("No valid moves left! Resetting level...");
                resetLevel();
            }
        }

        if (gameWon) {
            saveAllMaps();
        } else {
            System.out.println("No solution found.");
        }
    }

    private void resetLevel() {
        moveCount = 0;
        map = new Map(deepCopyValues(winningMaps.get(0).getValues()));
        initializeGame();
        winningMaps.clear();
        winningMaps.add(new Map(deepCopyValues(map.getValues())));
    }

    private void saveInitialMapState() {
        String fileName = String.format("%s/%04d.png", levelFolder, moveCount + 1);
        map.saveAsPng(fileName);
        System.out.println(fileName + " saved.");
        moveCount++;
    }

    private void saveAllMaps() {
        for (int i = 0; i < winningMaps.size(); i++) {
            String fileName = String.format("%s/%04d.png", levelFolder, i + 1);
            winningMaps.get(i).saveAsPng(fileName);
            System.out.println(fileName + " saved.");
        }
    }

    private ArrayList<ArrayList<Integer>> deepCopyValues(ArrayList<ArrayList<Integer>> original) {
        ArrayList<ArrayList<Integer>> copy = new ArrayList<>();
        for (ArrayList<Integer> row : original) {
            ArrayList<Integer> newRow = new ArrayList<>(row);
            copy.add(newRow);
        }
        return copy;
    }

    private boolean hasValidMoves() {
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
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

    private boolean isValidMove(int x, int y) {
        ArrayList<ArrayList<Integer>> values = map.getValues();
        if (y < 0 || y >= values.size() || x < 0 || x >= values.get(y).size()) {
            return false;
        }
        return values.get(y).get(x) != 1;
    }

    private void setCell(int x, int y, int value) {
        map.getValues().get(y).set(x, value);
    }

    private void movePlayer(int x, int y) {
        setCell(x, y, 3);
        player.setPosition(x, y);
    }

    private void displayMap() {
        ArrayList<ArrayList<Integer>> values = map.getValues();
        for (ArrayList<Integer> row : values) {
            for (Integer cell : row) {
                System.out.print(cell + " ");
            }
            System.out.println();
        }
    }

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
