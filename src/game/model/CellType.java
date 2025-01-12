package game.model;

/**
 * Represents different types of cells in the game map.
 */
public enum CellType {
    EMPTY(0),
    WALL(1),
    TARGET(2),
    PLAYER(3);

    private final int value;

    CellType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static CellType fromValue(int value) {
        for (CellType type : CellType.values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid cell value: " + value);
    }
}