package apryraz.tworld;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class TFStateTest {

    TFState state1, state2, state3;

    @BeforeEach
    void setUp() {
        state1 = new TFState(3);
        state2 = new TFState(5);
        state3 = new TFState(5);
    }

    @Test
    void initializeState() {
        state1.initializeState();
        for (int i = 1; i <= 3; i++) {
            for (int j = 1; j <= 3; j++) {
                assertEquals("?", state1.get(i, j));
            }
        }
    }

    @Test
    void set() {
        state1.set(new Position(2, 1), "T");
        assertEquals("T", state1.get(2, 1));

    }

    @Test
    void testEquals() {
        assertEquals(state2, state3);
        assertNotEquals(state1, state2);
    }

    @Test
    void testLoadFromFile() throws IOException {
        TFState state = new TFState(4);
        state.set(new Position(2, 2), "X");
        assertEquals(state, TFState.loadStateFromFile(4, "tests/states/initial.txt"));
    }

}