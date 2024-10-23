
import java.util.ArrayList;

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
