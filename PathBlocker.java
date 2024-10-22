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
        Queue<State> queue = new LinkedList<>();
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

            // Try moving in all directions
            String[] directions = {"W", "A", "S", "D"};
            int[][] dirVectors = {{0, -1}, {-1, 0}, {0, 1}, {1, 0}};

            for (int i = 0; i < directions.length; i++) {
                String move = directions[i];
                int dirX = dirVectors[i][0];
                int dirY = dirVectors[i][1];

                // Simulate the move
                State nextState = simulateMove(currentState, dirX, dirY, move);

                if (nextState != null && !visited.contains(nextState.getUniqueIdentifier())) {
                    queue.add(nextState);
                    visited.add(nextState.getUniqueIdentifier());
                }
            }
        }

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

    // Abstract State class
    abstract class State {
        protected int playerX;
        protected int playerY;
        protected ArrayList<ArrayList<Integer>> mapValues;
        protected ArrayList<String> moves;

        public State(int playerX, int playerY, ArrayList<ArrayList<Integer>> mapValues, ArrayList<String> moves) {
            this.playerX = playerX;
            this.playerY = playerY;
            this.mapValues = mapValues;
            this.moves = moves;
        }

        public int getPlayerX() {
            return playerX;
        }

        public int getPlayerY() {
            return playerY;
        }

        public ArrayList<ArrayList<Integer>> getMapValues() {
            return mapValues;
        }

        public ArrayList<String> getMoves() {
            return moves;
        }

        public abstract String getUniqueIdentifier();
    }

    // Concrete subclass extending State
    class GameState extends State {

        public GameState(int playerX, int playerY, ArrayList<ArrayList<Integer>> mapValues, ArrayList<String> moves) {
            super(playerX, playerY, mapValues, moves);
        }

        @Override
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
