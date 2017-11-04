package common;

public enum Direction {
    Up, Left, Down, Right;
    private Direction getWithOffset(int offset) {
        return values()[(this.ordinal() + offset + values().length) % values().length];
    }
    public Direction getLeft() {
        return getWithOffset(1);
    }
    public Direction getRight() {
        return getWithOffset(-1);
    }
    public Direction getBehind() {
        return getWithOffset(2);
    }
    public GridVector toVector2() {
        switch (this) {
            case Up: return new GridVector(-1, 0);
            case Down: return new GridVector(1, 0);
            case Left: return new GridVector(0, -1);
            case Right: return new GridVector(0, 1);
            default: return new GridVector(0, 0);
        }
    }
}
