package apryraz.tworld;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PositionTest {

    @Test
    void test_postion_ok() {
        Position position = new Position(3, 2);
        Position position1 = new Position(3, 2);
        assertEquals(position, position1);
        assertEquals(3, position.x);
        assertEquals(2, position.y);
    }

}