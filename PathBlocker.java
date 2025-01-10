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
    private Elevation elevation;

    public PathBlocker(ChartMap map, String levelFolder) {
        this.map = map;
        this.levelFolder = levelFolder;
        this.elevation = new Elevation(map.getValues().size(), 5);
        initializeGame();
        ensureDirectoryExists(levelFolder);

        // İlk haritayı elevation ile birlikte kaydet
        map.setElevation(elevation);
        ChartMap initialMap = new ChartMap(deepCopyValues(map.getValues()));
        initialMap.setElevation(elevation);
        winningMaps.add(initialMap);

        System.out.println("Elevation Map:");
        System.out.println(elevation.toString());

        saveInitialMapState();
    }

    private void initializeGame() {
        ArrayList<ArrayList<Integer>> values = map.getValues();
        for (int y = 0; y < values.size(); y++) {
            for (int x = 0; x < values.get(y).size(); x++) {
                int value = values.get(y).get(x);
                if (value == 3) { // 3 -> player
                    player = new Player(x, y);
                } else if (value == 2) { // 2 -> target
                    targetX = x;
                    targetY = y;
                }
            }
        }
    }

    public void play() {
        // Priority queue ordered by f(n) = g(n) + h(n)
        PriorityQueue<State> openSet = new PriorityQueue<>((a, b) -> {
            int f1 = a.getTotalCost() + manhattanDistance(a.getPlayerX(), a.getPlayerY(), targetX, targetY);
            int f2 = b.getTotalCost() + manhattanDistance(b.getPlayerX(), b.getPlayerY(), targetX, targetY);
            return Integer.compare(f1, f2);
        });

        Set<String> visited = new HashSet<>();

        // Initialize starting state
        ArrayList<String> initialMoves = new ArrayList<>();
        ArrayList<ArrayList<Integer>> initialMapValues = deepCopyValues(map.getValues());
        State initialState = new GameState(player.getX(), player.getY(), initialMapValues, initialMoves, 0);
        openSet.add(initialState);

        State finalState = null;
        boolean solutionFound = false;

        while (!openSet.isEmpty()) {
            State currentState = openSet.poll();

            if (currentState.getPlayerX() == targetX && currentState.getPlayerY() == targetY) {
                solutionFound = true;
                finalState = currentState;
                break;
            }

            String stateId = currentState.getUniqueIdentifier();
            if (visited.contains(stateId))
                continue;
            visited.add(stateId);

            // Try all possible moves
            String[] directions = { "W", "A", "S", "D" };
            int[][] dirVectors = { { 0, -1 }, { -1, 0 }, { 0, 1 }, { 1, 0 } };

            for (int i = 0; i < directions.length; i++) {
                String move = directions[i];
                int dirX = dirVectors[i][0];
                int dirY = dirVectors[i][1];

                State nextState = simulateMove(currentState, dirX, dirY, move);
                if (nextState != null && !visited.contains(nextState.getUniqueIdentifier())) {
                    openSet.add(nextState);
                }
            }
        }

        if (solutionFound) {
            System.out.println("Solution found with total cost: " + finalState.getTotalCost());
            replaySolution(finalState.getMoves());
            return;
        } else {
            System.out.println("No solution found.");
        }
    }

    private int manhattanDistance(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    private State simulateMove(State currentState, int dirX, int dirY, String move) {
        int currentX = currentState.getPlayerX();
        int currentY = currentState.getPlayerY();
        ArrayList<ArrayList<Integer>> values = deepCopyValues(currentState.getMapValues());
        int totalCost = currentState.getTotalCost();

        values.get(currentY).set(currentX, 1);
        boolean canMove = false;

        while (true) {
            int nextX = currentX + dirX;
            int nextY = currentY + dirY;

            if (!isValidMove(nextX, nextY, values))
                break;

            int nextValue = values.get(nextY).get(nextX);
            int heightCost = elevation.getMovementCost(nextX, nextY);
            totalCost += heightCost;
            System.out.printf("Moving to (%d,%d) with height %d, cost: %d\n",
                    nextX, nextY, elevation.getHeight(nextX, nextY), heightCost);

            currentX = nextX;
            currentY = nextY;
            canMove = true;

            if (nextValue == 2) {
                break;
            }

            values.get(currentY).set(currentX, 1);
        }

        if (!canMove) {
            return null;
        }

        values.get(currentY).set(currentX, 3);
        ArrayList<String> newMoves = new ArrayList<>(currentState.getMoves());
        newMoves.add(move);

        return new GameState(currentX, currentY, values, newMoves, totalCost);
    }

    private boolean isValidMove(int x, int y, ArrayList<ArrayList<Integer>> values) {
        if (y < 0 || y >= values.size() || x < 0 || x >= values.get(y).size()) {
            return false;
        }
        int cellValue = values.get(y).get(x);
        return cellValue != 1;
    }

    private void replaySolution(ArrayList<String> moves) {
        // Reset the map to initial state
        map = new ChartMap(deepCopyValues(winningMaps.get(0).getValues()));
        initializeGame();

        // Clear any previous winning moves
        winningMaps.clear();
        winningMaps.add(new ChartMap(deepCopyValues(map.getValues()))); // Save initial state

        moveCount = 0;
        boolean reachedTarget = false;

        // İlk durumu kaydet
        ChartMap initialMap = new ChartMap(deepCopyValues(map.getValues()));
        initialMap.setElevation(elevation);
        winningMaps.add(initialMap);
        saveMapWithElevation(String.format("%s/%04d.png", levelFolder, moveCount));
        moveCount++;

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

            // Turn the player's starting position into a wall
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

                if (nextValue == 2) {
                    reachedTarget = true;
                    break;
                }

                // Turn the traversed cell into a wall
                setCell(currentX, currentY, 1);
            }

            if (!canMove) {
                System.out.println("Cannot move!");
                return;
            }

            // Move player
            movePlayer(currentX, currentY);

            // Sadece bir kere kaydet
            ChartMap newMap = new ChartMap(deepCopyValues(map.getValues()));
            newMap.setElevation(elevation);
            winningMaps.add(newMap);
            saveMapWithElevation(String.format("%s/%04d.png", levelFolder, moveCount));
            moveCount++;

            if (reachedTarget) {
                System.out.println("Target reached! Moving to next level...");
                return; // saveAllMaps() çağrısını kaldırdık
            }
        }

        // saveAllMaps() çağrısını kaldırdık çünkü her adımda zaten kaydediyoruz
    }

    private void saveInitialMapState() {
        String fileName = String.format("%s/%04d.png", levelFolder, moveCount);
        saveMapWithElevation(fileName);
        System.out.println(fileName + " saved.");
        moveCount++;
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
        // The previous position is already turned into a wall
        setCell(x, y, 3);
        player.setPosition(x, y);
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

        ArrayList<ChartMap> maps = ChartMap.readMaps(filePaths);

        for (int i = 0; i < maps.size(); i++) {
            String levelFolder = String.format("level%02d", i + 1);
            System.out.println("\nStarting Level " + (i + 1));
            ChartMap map = maps.get(i);
            PathBlocker game = new PathBlocker(map, levelFolder);
            game.play();

            if (i == maps.size() - 1) {
                System.out.println("Game completed! All levels finished!");
            }
        }
    }

    /*
     * 1) Why you prefer the search algorithm you choose? We prefer Breadth-First
     * Search (BFS) because it explores all possible states in an organized way,
     * going level by level. This approach ensures that you find the shortest path
     * to the target, guaranteeing the most efficient solution with the least number
     * of moves.
     * 2) Can you achieve the optimal result? Why? Why not? Yes, because BFS
     * examines all possible paths at each step and thus ensures that the goal is
     * reached by the shortest path. In other words, it offers an optimal solution.
     * 3) How you achieved efficiency for keeping the states? By using a hash set of
     * unique state identifiers (combining the player's position and blocked cells),
     * the algorithm avoids revisiting the same states, thus reducing unnecessary
     * computations.
     * 4) If you prefer to use DFS (tree version) then do you need to avoid cycles?
     * When using Depth-First Search (DFS), it's crucial to watch out for cycles. If
     * you don't check for them, DFS can end up going in circles, revisiting the
     * same states over and over. This can lead to infinite loops and unnecessary
     * work, making the search inefficient.
     * 5) What will be the path-cost for this problem? Path cost is the total number
     * of moves needed to get from the starting position to the target. Since all
     * moves cost the same, the path cost is just the number of moves in the
     * sequence that leads to the goal.
     * 
     */

    public void saveMapWithElevation(String fileName) {
        map.setElevation(elevation);
        map.saveAsPng(fileName);
    }

}
