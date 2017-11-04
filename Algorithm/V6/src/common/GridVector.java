package common;

public class GridVector {

    private int x;
    private int y;

    public GridVector(int i, int j) {
        x = i;
        y = j;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public void x(int i) {
        x = i;
    }

    public void y(int j) {
        y = j;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    public boolean equals(GridVector coord) {
        return coord.x() == x && coord.y() == y;
    }

    public void add(GridVector coord) {
        x += coord.x();
        y += coord.y();
    }

    public void multiply(int mul) {
        x *= mul;
        y *= mul;
    }

    public GridVector fnAdd(GridVector coord) {
        return new GridVector(x + coord.x(), y + coord.y());
    }

    public GridVector fnMultiply(int multiplier) {
        return new GridVector(x * multiplier, y * multiplier);
    }
    
    @Override
    public boolean equals(Object obj) {
        return equals((GridVector) obj);
    }
}
