package apryraz.tworld;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AMessageTest {

    static AMessage msg0, msg1, msg11;

    @BeforeAll
    static void setUp() {
        msg0 = new AMessage("a", "b", "c", "d");
        msg1 = new AMessage("1", "2", "3", "4");
        msg11 = new AMessage("1", "2", "3", "4");
    }

    @Test
    void getComp() {
        assertEquals("a", msg0.getComp(0));
        assertEquals("b", msg0.getComp(1));
        assertEquals("c", msg0.getComp(2));
        assertEquals("d", msg0.getComp(3));
    }

    @Test
    void testEquals() {
        assertNotEquals(msg0, msg1);
        assertEquals(msg11, msg1);
    }

    @Test
    void testHashCode() {
        assertNotEquals(msg0.hashCode(), msg1.hashCode());
        assertEquals(msg1.hashCode(), msg11.hashCode());
    }

    @Test
    void testToString() {
        assertEquals("AMessage{msg=[a, b, c, d]}", msg0.toString());
    }
}