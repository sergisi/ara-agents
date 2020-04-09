package apryraz.tworld;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TreasureWorldEnvTest {

    TreasureWorldEnv world;

    @BeforeEach
    void setUp() {
        world = new TreasureWorldEnv(4, 3, 3, "tests/pirates1");
    }



    @Test
    void loadPiratesLocations() {
        assertEquals(List.of(new Position(1, 3),
                             new Position(2, 5)),
                     TreasureWorldEnv.loadPiratesLocations("tests/pirates3.txt")
                );
    }

    @Test
    void acceptMessage() {
        //TODO: make test of accept message.
    }

    @Test
    void isPirateInMyCell() {
        assertEquals(0, world.isPirateInMyCell(2, 2));
        assertEquals(1, world.isPirateInMyCell(4, 4));
    }

    @Test
    void withinLimits() {
        assertTrue(world.withinLimits(1, 1));
        assertTrue(world.withinLimits(2, 1));
        assertTrue(world.withinLimits(4, 3));
        assertFalse(world.withinLimits(0, 3));
        assertFalse(world.withinLimits(5, 3));
    }
}