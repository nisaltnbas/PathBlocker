import java.util.ArrayList;

abstract class State {
    protected int playerX;
    protected int playerY;
    protected ArrayList<ArrayList<Integer>> mapValues;
    protected ArrayList<String> moves;
    protected int totalCost;

    public State(int playerX, int playerY, ArrayList<ArrayList<Integer>> mapValues, ArrayList<String> moves,
            int totalCost) {
        this.playerX = playerX;
        this.playerY = playerY;
        this.mapValues = mapValues;
        this.moves = moves;
        this.totalCost = totalCost;
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

    public int getTotalCost() {
        return totalCost;
    }

    public abstract String getUniqueIdentifier();
}