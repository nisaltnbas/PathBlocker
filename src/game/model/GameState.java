package game.model;

import java.util.ArrayList;

/**
 * Concrete implementation of game state that includes a unique identifier based
 * on player position and wall locations.
 */
public class GameState extends State {

    public GameState(int playerX, int playerY, ArrayList<ArrayList<Integer>> mapValues, ArrayList<String> moves,
            int totalCost) {
        super(playerX, playerY, mapValues, moves, totalCost);
    }

    @Override
    public String getUniqueIdentifier() {
        StringBuilder sb = new StringBuilder();
        sb.append(playerX).append(',').append(playerY).append(';');
        // Serialize only the blocked cells
        for (int y = 0; y < mapValues.size(); y++) {
            for (int x = 0; x < mapValues.get(y).size(); x++) {
                int cell = mapValues.get(y).get(x);
                if (cell == CellType.WALL.getValue()) {
                    sb.append(x).append(',').append(y).append(';');
                }
            }
        }
        return sb.toString();
    }
}