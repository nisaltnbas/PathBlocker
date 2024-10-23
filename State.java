import java.util.ArrayList;

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