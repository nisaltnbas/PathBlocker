import java.util.*;
import java.io.File;

class PathBlocker {
    private ChartMap map;
    private Player player;
    private int targetX;
    private int targetY;
    private int moveCount = 0;
    private String levelFolder;
    private ArrayList<ChartMap> winningMaps = new ArrayList<>();

    public PathBlocker(ChartMap map, String levelFolder) {
        this.map = map;
        this.levelFolder = levelFolder;
        initializeGame();
        ensureDirectoryExists(levelFolder);
        // Add the initial map state to winningMaps
        winningMaps.add(new ChartMap(deepCopyValues(map.getValues())));
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
        PriorityQueue<State> openSet = new PriorityQueue<>();
        Map<String, Integer> closedSet = new HashMap<>();

        // Initialize the initial state
        ArrayList<String> initialMoves = new ArrayList<>();
        ArrayList<ArrayList<Integer>> initialMapValues = deepCopyValues(map.getValues());
        int initialHeuristic = heuristic(player.getX(), player.getY());
        State initialState = new State(player.getX(), player.getY(), initialMapValues, initialMoves, 0, initialHeuristic);
        openSet.add(initialState);
        closedSet.put(initialState.getUniqueIdentifier(), initialState.g + initialState.h);

        boolean solutionFound = false;
        State finalState = null;

        while (!openSet.isEmpty()) {
            State currentState = openSet.poll();

            if (currentState.playerX == targetX && currentState.playerY == targetY) {
                solutionFound = true;
                finalState = currentState;
                break;
            }

            // Try moving in all directions
            String[] directions = {"W", "A", "S", "D"};
            int[][] dirVectors = {{0, -1}, {-1, 0}, {0, 1}, {1, 0}};

            for (int i = 0; i < directions.length; i++) {
                String move = directions[i];
                int dirX = dirVectors[i][0];
                int dirY = dirVectors[i][1];

                // Simulate the move
                State nextState = simulateMove(currentState, dirX, dirY, move);

                if (nextState != null) {
                    String identifier = nextState.getUniqueIdentifier();
                    int totalCost = nextState.g + nextState.h;

                    if (!closedSet.containsKey(identifier) || closedSet.get(identifier) > totalCost) {
                        openSet.add(nextState);
                        closedSet.put(identifier, totalCost);
                    }
                }
            }
        }

        if (solutionFound) {
            System.out.println("Solution found in " + finalState.moves.size() + " moves.");
            // Replay the moves to save the maps
            replaySolution(finalState.moves);
        } else {
            System.out.println("No solution found.");
        }
    }

    private int heuristic(int x, int y) {
        // Manhattan distance
        return Math.abs(x - targetX) + Math.abs(y - targetY);
    }

    private State simulateMove(State currentState, int dirX, int dirY, String move) {
        int currentX = currentState.playerX;
        int currentY = currentState.playerY;
        ArrayList<ArrayList<Integer>> values = deepCopyValues(currentState.mapValues);
        ArrayList<int[]> path = new ArrayList<>();

        // Başlangıçta, oyuncunun mevcut konumunu duvara dönüştürüyoruz
        values.get(currentY).set(currentX, 1);

        boolean canMove = false;

        while (true) {
            int nextX = currentX + dirX;
            int nextY = currentY + dirY;

            if (!isValidMove(nextX, nextY, values))
                break;

            int nextValue = values.get(nextY).get(nextX);

            currentX = nextX;
            currentY = nextY;
            canMove = true;

            if (nextValue == 2)
                break;

            // Hareket ettiği her hücreyi duvara dönüştür
            values.get(currentY).set(currentX, 1);
        }

        if (!canMove) {
            return null;
        }

        // Oyuncuyu yeni konuma taşı
        values.get(currentY).set(currentX, 3);

        // Hareketleri güncelle
        ArrayList<String> newMoves = new ArrayList<>(currentState.moves);
        newMoves.add(move);

        int gCost = currentState.g + 1;
        int hCost = heuristic(currentX, currentY);

        State nextState = new State(currentX, currentY, values, newMoves, gCost, hCost);
        return nextState;
    }


    private boolean isValidMove(int x, int y, ArrayList<ArrayList<Integer>> values) {
        if (y < 0 || y >= values.size() || x < 0 || x >= values.get(y).size()) {
            return false;
        }
        return values.get(y).get(x) != 1;
    }

    private void replaySolution(ArrayList<String> moves) {
        // Haritayı başlangıç durumuna sıfırla
        map = new ChartMap(deepCopyValues(winningMaps.get(0).getValues()));
        initializeGame();

        // Önceki kazanma hareketlerini temizle
        winningMaps.clear();
        winningMaps.add(new ChartMap(deepCopyValues(map.getValues()))); // Başlangıç durumunu kaydet

        moveCount = 0;

        for (String move : moves) {
            int dirX = 0, dirY = 0;
            switch (move) {
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
            }

            int currentX = player.getX();
            int currentY = player.getY();

            // Başlangıç konumunu duvara dönüştür
            setCell(currentX, currentY, 1);

            boolean canMove = false;

            while (true) {
                int nextX = currentX + dirX;
                int nextY = currentY + dirY;

                if (!isValidMove(nextX, nextY, map.getValues()))
                    break;

                int nextValue = map.getValues().get(nextY).get(nextX);

                currentX = nextX;
                currentY = nextY;
                canMove = true;

                if (nextValue == 2)
                    break;

                // Hareket ettiği her hücreyi duvara dönüştür
                setCell(currentX, currentY, 1);
            }

            if (!canMove) {
                System.out.println("Hareket edilemiyor!");
                break;
            }

            // Oyuncuyu yeni konuma taşı
            movePlayer(currentX, currentY);

            // Geçerli harita durumunu kaydet
            winningMaps.add(new ChartMap(deepCopyValues(map.getValues())));
            moveCount++;
        }

        // Tüm haritaları kaydet
        saveAllMaps();
    }

    private void resetLevel() {
        moveCount = 0;
        map = new ChartMap(deepCopyValues(winningMaps.get(0).getValues()));
        initializeGame();
        winningMaps.clear();
        winningMaps.add(new ChartMap(deepCopyValues(map.getValues())));
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

    private void setCell(int x, int y, int value) {
        map.getValues().get(y).set(x, value);
    }

    private void movePlayer(int x, int y) {
        // Oyuncunun önceki konumu zaten duvara dönüştürüldü, bu yüzden burada sadece yeni konumu güncelliyoruz
        setCell(x, y, 3);
        player.setPosition(x, y);
    }

    private void ensureDirectoryExists(String dirPath) {
        File directory = new File(dirPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    // State class for A* Algorithm
    class State implements Comparable<State> {
        int playerX;
        int playerY;
        ArrayList<ArrayList<Integer>> mapValues;
        ArrayList<String> moves;
        int g; // Cost from start to current node
        int h; // Heuristic estimate from current node to goal

        public State(int playerX, int playerY, ArrayList<ArrayList<Integer>> mapValues, ArrayList<String> moves, int g, int h) {
            this.playerX = playerX;
            this.playerY = playerY;
            this.mapValues = mapValues;
            this.moves = moves;
            this.g = g;
            this.h = h;
        }

        public String getUniqueIdentifier() {
            // Unique identifier combining player position and blocked cells
            StringBuilder sb = new StringBuilder();
            sb.append(playerX).append(',').append(playerY).append(';');
            // Serialize only the blocked cells
            for (int y = 0; y < mapValues.size(); y++) {
                for (int x = 0; x < mapValues.get(y).size(); x++) {
                    int cell = mapValues.get(y).get(x);
                    if (cell == 1) {
                        sb.append(x).append(',').append(y).append(';');
                    }
                }
            }
            return sb.toString();
        }

        @Override
        public int compareTo(State other) {
            return Integer.compare(this.g + this.h, other.g + other.h);
        }
    }

    public static void main(String[] args) {
        String[] filePaths = {
                "level1.txt", "level2.txt", "level3.txt", "level4.txt", "level5.txt",
                "level6.txt", "level7.txt", "level8.txt", "level9.txt", "level10.txt"
        };

        ArrayList<ChartMap> maps = ChartMap.readMaps(filePaths);

        for (int i = 0; i < maps.size(); i++) {
            String levelFolder = String.format("level%02d", i + 1);
            ChartMap map = maps.get(i);
            PathBlocker game = new PathBlocker(map, levelFolder);
            game.play();
        }
    }
}
