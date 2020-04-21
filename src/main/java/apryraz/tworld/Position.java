package apryraz.tworld;

import java.util.Objects;

import static java.lang.Math.abs;

/**
 * A class for representing a Position in the world. It's
 * immutable and comes with a bunch of useful methods.
 */
public class Position {
    /**
     *
     **/
    public final int x, y;

    /**
     * Constructor of the class.
     * @param a in coordinates x
     * @param b in coordinates y
     */
    public Position(int a, int b) {
        x = a;
        y = b;
    }

    /**
     * Gets the distance in square to another position
     *
     * @param p other position
     * @return the distance (int) between two points. Distance is calculated as
     * max(|x-p.x|, |y-p.y|)
     */
    public int distanceOf(Position p) {
        return Integer.max(abs(x - p.x), abs(y - p.y));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Position{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return x == position.x &&
                y == position.y;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
