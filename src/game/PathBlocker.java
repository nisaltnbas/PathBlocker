package game;

import java.util.*;
import game.graphics.ChartMap;
import game.model.*;
import game.util.FileUtil;

public class PathBlocker {
    private ChartMap map;
    private Player player;
    private int targetX;
    private int targetY;
    private int moveCount = 0;
    private String levelFolder;
    private ArrayList<ChartMap> winningMaps = new ArrayList<>();
    private Elevation elevation;
    private int finalCost = -1;

    /*
     * Solution Characteristics:
     * 
     * 1. Optimality:
     * - The solution uses A* search algorithm (f(n) = g(n) + h(n))
     * - g(n): Actual cost from start to current state (including elevation costs)
     * - h(n): Manhattan distance heuristic to estimate remaining cost
     * - Since Manhattan distance is admissible (never overestimates), A* guarantees
     * optimal solution
     * 
     * 2. Space Efficiency:
     * Advantages:
     * - Uses HashSet for visited states to prevent cycles and repeated states
     * - Only stores necessary state information (player position, map state, moves)
     * Disadvantages:
     * - Stores complete map state for each node, which can be memory intensive
     * - Keeps track of all winning maps for visualization
     * 
     * 3. Time Efficiency:
     * Advantages:
     * - A* explores promising paths first, reducing unnecessary exploration
     * - Quick state comparison using unique identifiers
     * - Efficient move validation using array bounds checking
     * Disadvantages:
     * - Must explore all equally promising paths until finding goal
     * - Deep copying map states for each new state is expensive
     * - Saving PNG files for visualization adds overhead
     * 
     * 4. Overall Approach:
     * Advantages:
     * - Guarantees shortest path (optimal solution)
     * - Handles elevation costs correctly
     * - Provides visual representation of solution
     * - Prevents infinite loops with visited states tracking
     * Disadvantages:
     * - Higher memory usage compared to simpler algorithms like DFS
     * - May be slower for simple cases where simpler algorithms would suffice
     * - Storage overhead for visualization purposes
     */

    public PathBlocker(ChartMap map, String levelFolder) {
        this.map = map;
        this.levelFolder = FileUtil.getProjectPath(levelFolder);
        this.elevation = new Elevation(map.getValues().size(), 5);
        initializeGame();
        FileUtil.ensureDirectoryExists(this.levelFolder);

        map.setElevation(elevation);
        ChartMap initialMap = new ChartMap(deepCopyValues(map.getValues()));
        initialMap.setElevation(elevation);
        winningMaps.add(initialMap);

        saveInitialMapState();
    }

    private void initializeGame() {
        ArrayList<ArrayList<Integer>> values = map.getValues();
        for (int y = 0; y < values.size(); y++) {
            for (int x = 0; x < values.get(y).size(); x++) {
                int value = values.get(y).get(x);
                if (value == CellType.PLAYER.getValue()) {
                    player = new Player(x, y);
                } else if (value == CellType.TARGET.getValue()) {
                    targetX = x;
                    targetY = y;
                }
            }
        }
    }

    public void play() {
        PriorityQueue<State> openSet = new PriorityQueue<>((a, b) -> {
            int f1 = a.getTotalCost() + manhattanDistance(a.getPlayerX(), a.getPlayerY(), targetX, targetY);
            int f2 = b.getTotalCost() + manhattanDistance(b.getPlayerX(), b.getPlayerY(), targetX, targetY);
            return Integer.compare(f1, f2);
        });

        Set<String> visited = new HashSet<>();
        State initialState = new GameState(player.getX(), player.getY(), deepCopyValues(map.getValues()),
                new ArrayList<>(), 0);
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
            if (visited.contains(stateId)) {
                continue;
            }
            visited.add(stateId);

            for (Direction direction : Direction.values()) {
                State nextState = simulateMove(currentState, direction);
                if (nextState != null && !visited.contains(nextState.getUniqueIdentifier())) {
                    openSet.add(nextState);
                }
            }
        }

        if (solutionFound) {
            finalCost = finalState.getTotalCost();
            replaySolution(finalState.getMoves());
        }
    }

    private int manhattanDistance(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    private State simulateMove(State currentState, Direction direction) {
        int currentX = currentState.getPlayerX();
        int currentY = currentState.getPlayerY();
        ArrayList<ArrayList<Integer>> values = deepCopyValues(currentState.getMapValues());
        int totalCost = currentState.getTotalCost();

        values.get(currentY).set(currentX, CellType.WALL.getValue());
        boolean canMove = false;

        while (true) {
            int nextX = currentX + direction.getDx();
            int nextY = currentY + direction.getDy();

            if (!isValidMove(nextX, nextY, values)) {
                break;
            }

            int nextValue = values.get(nextY).get(nextX);
            int heightCost = elevation.getMovementCost(nextX, nextY);
            totalCost += heightCost;

            currentX = nextX;
            currentY = nextY;
            canMove = true;

            if (nextValue == CellType.TARGET.getValue()) {
                break;
            }

            values.get(currentY).set(currentX, CellType.WALL.getValue());
        }

        if (!canMove) {
            return null;
        }

        values.get(currentY).set(currentX, CellType.PLAYER.getValue());
        ArrayList<String> newMoves = new ArrayList<>(currentState.getMoves());
        newMoves.add(direction.getKey());

        return new GameState(currentX, currentY, values, newMoves, totalCost);
    }

    private boolean isValidMove(int x, int y, ArrayList<ArrayList<Integer>> values) {
        if (y < 0 || y >= values.size() || x < 0 || x >= values.get(y).size()) {
            return false;
        }
        return values.get(y).get(x) != CellType.WALL.getValue();
    }

    private void replaySolution(ArrayList<String> moves) {
        map = new ChartMap(deepCopyValues(winningMaps.get(0).getValues()));
        initializeGame();
        winningMaps.clear();
        winningMaps.add(new ChartMap(deepCopyValues(map.getValues())));

        moveCount = 0;
        boolean reachedTarget = false;

        ChartMap initialMap = new ChartMap(deepCopyValues(map.getValues()));
        initialMap.setElevation(elevation);
        winningMaps.add(initialMap);
        saveMapWithElevation(String.format("%s/%04d.png", levelFolder, moveCount));
        moveCount++;

        for (String moveKey : moves) {
            Direction direction = Direction.fromKey(moveKey);
            int currentX = player.getX();
            int currentY = player.getY();

            setCell(currentX, currentY, CellType.WALL.getValue());

            boolean canMove = false;

            while (true) {
                int nextX = currentX + direction.getDx();
                int nextY = currentY + direction.getDy();

                if (!isValidMove(nextX, nextY, map.getValues())) {
                    break;
                }

                int nextValue = map.getValues().get(nextY).get(nextX);

                currentX = nextX;
                currentY = nextY;
                canMove = true;

                if (nextValue == CellType.TARGET.getValue()) {
                    reachedTarget = true;
                    break;
                }

                setCell(currentX, currentY, CellType.WALL.getValue());
            }

            if (!canMove) {
                return;
            }

            movePlayer(currentX, currentY);

            ChartMap newMap = new ChartMap(deepCopyValues(map.getValues()));
            newMap.setElevation(elevation);
            winningMaps.add(newMap);
            saveMapWithElevation(String.format("%s/%04d.png", levelFolder, moveCount));
            moveCount++;

            if (reachedTarget) {
                return;
            }
        }
    }

    private void saveInitialMapState() {
        String fileName = String.format("%s/%04d.png", levelFolder, moveCount);
        saveMapWithElevation(fileName);
        moveCount++;
    }

    private ArrayList<ArrayList<Integer>> deepCopyValues(ArrayList<ArrayList<Integer>> original) {
        ArrayList<ArrayList<Integer>> copy = new ArrayList<>();
        for (ArrayList<Integer> row : original) {
            copy.add(new ArrayList<>(row));
        }
        return copy;
    }

    private void setCell(int x, int y, int value) {
        map.getValues().get(y).set(x, value);
    }

    private void movePlayer(int x, int y) {
        setCell(x, y, CellType.PLAYER.getValue());
        player.setPosition(x, y);
    }

    private void saveMapWithElevation(String fileName) {
        map.setElevation(elevation);
        map.saveAsPng(fileName);
    }

    public int getFinalCost() {
        return finalCost;
    }

    public static void main(String[] args) {
        String[] filePaths = {
                "level1.txt", "level2.txt", "level3.txt", "level4.txt", "level5.txt",
                "level6.txt", "level7.txt", "level8.txt", "level9.txt", "level10.txt"
        };

        ArrayList<ChartMap> maps = ChartMap.readMaps(filePaths);
        int totalCost = 0;

        // Create solutions directory
        FileUtil.ensureDirectoryExists("solutions");
        System.out.println("Starting PathBlocker game...\n");

        for (int i = 0; i < maps.size(); i++) {
            String levelFolder = String.format("solutions/level%02d", i + 1);
            System.out.println("Starting Level " + (i + 1) + "...");
            ChartMap map = maps.get(i);
            PathBlocker game = new PathBlocker(map, levelFolder);
            game.play();

            int levelCost = game.getFinalCost();
            if (levelCost != -1) {
                totalCost += levelCost;
                System.out.println("Level " + (i + 1) + " completed! Cost: " + levelCost);
            } else {
                System.out.println("Level " + (i + 1) + " could not be completed.");
            }
            System.out.println();
        }

        System.out.println("Game completed!");
        System.out.println("Total cost for all levels: " + totalCost);
    }
}