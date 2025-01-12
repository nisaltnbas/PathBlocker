package game.model;

/**
 * Represents possible movement directions in the game.
 */
public enum Direction {
    UP("W", 0, -1),
    LEFT("A", -1, 0),
    DOWN("S", 0, 1),
    RIGHT("D", 1, 0);

    private final String key;
    private final int dx;
    private final int dy;

    Direction(String key, int dx, int dy) {
        this.key = key;
        this.dx = dx;
        this.dy = dy;
    }

    public String getKey() {
        return key;
    }

    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }

    public static Direction fromKey(String key) {
        for (Direction dir : Direction.values()) {
            if (dir.key.equals(key)) {
                return dir;
            }
        }
        throw new IllegalArgumentException("Invalid direction key: " + key);
    }
}