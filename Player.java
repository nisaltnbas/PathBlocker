public class Player {
    private int x;
    private int y;

    public Player(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // Getters
    public int getX() { return x; }
    public int getY() { return y; }

    // Setters
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
