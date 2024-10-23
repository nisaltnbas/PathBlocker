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
        // Initialize winningMaps with the initial map state
        winningMaps.add(new ChartMap(deepCopyValues(map.getValues())));
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
        // We create a queue, BFS method
        Queue<State> queue = new LinkedList<>();

        // We use a set to keep track of the states we visit so we don't go to the same
        // place again
        Set<String> visited = new HashSet<>();

        // Initialize the initial state
        ArrayList<String> initialMoves = new ArrayList<>();
        ArrayList<ArrayList<Integer>> initialMapValues = deepCopyValues(map.getValues());
        State initialState = new GameState(player.getX(), player.getY(), initialMapValues, initialMoves);
        queue.add(initialState);
        visited.add(initialState.getUniqueIdentifier());

        boolean solutionFound = false;
        State finalState = null;

        while (!queue.isEmpty()) {
            State currentState = queue.poll();

            if (currentState.getPlayerX() == targetX && currentState.getPlayerY() == targetY) {
                solutionFound = true;
                finalState = currentState;
                break;
            }

            // Directions that can be moved and their coordinate changes
            String[] directions = { "W", "A", "S", "D" };
            int[][] dirVectors = { { 0, -1 }, { -1, 0 }, { 0, 1 }, { 1, 0 } };

            // We try to move in all directions (W, A, S, D)
            for (int i = 0; i < directions.length; i++) {
                String move = directions[i];
                int dirX = dirVectors[i][0];
                int dirY = dirVectors[i][1];

                // Simulate the move
                State nextState = simulateMove(currentState, dirX, dirY, move);

                // If the move is valid and we haven't been to this situation before
                if (nextState != null && !visited.contains(nextState.getUniqueIdentifier())) {
                    queue.add(nextState);// We add the new state to the queue
                    visited.add(nextState.getUniqueIdentifier());// We mark it as visited
                }
            }
        }
        // If a solution is found, we print how many moves it was found and play the
        // solution again.
        if (solutionFound) {
            System.out.println("Solution found in " + finalState.getMoves().size() + " moves.");
            // Replay the moves to save the maps
            replaySolution(finalState.getMoves());
        } else {
            System.out.println("No solution found.");
        }
    }

    private State simulateMove(State currentState, int dirX, int dirY, String move) {
        int currentX = currentState.getPlayerX();
        int currentY = currentState.getPlayerY();
        ArrayList<ArrayList<Integer>> values = deepCopyValues(currentState.getMapValues());

        // Turn the player's starting position into a wall
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

            // Turn the traversed cell into a wall
            values.get(currentY).set(currentX, 1);
        }

        if (!canMove) {
            return null;
        }

        // Move the player to the new position
        values.get(currentY).set(currentX, 3);

        // Update moves
        ArrayList<String> newMoves = new ArrayList<>(currentState.getMoves());
        newMoves.add(move);

        State nextState = new GameState(currentX, currentY, values, newMoves);
        return nextState;
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

                if (nextValue == 2)
                    break;

                // Turn the traversed cell into a wall
                setCell(currentX, currentY, 1);
            }

            if (!canMove) {
                System.out.println("Cannot move!");
                break;
            }

            // Move player
            movePlayer(currentX, currentY);

            // Save current map state
            winningMaps.add(new ChartMap(deepCopyValues(map.getValues())));
            moveCount++;
        }

        // Save all maps
        saveAllMaps();
    }

    private void saveInitialMapState() {
        String fileName = String.format("%s/%04d.png", levelFolder, moveCount);
        map.saveAsPng(fileName);
        System.out.println(fileName + " saved.");
        moveCount++;
    }

    private void saveAllMaps() {
        for (int i = 0; i < winningMaps.size(); i++) {
            String fileName = String.format("%s/%04d.png", levelFolder, i);
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
            ChartMap map = maps.get(i);
            PathBlocker game = new PathBlocker(map, levelFolder);
            game.play();
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

}
