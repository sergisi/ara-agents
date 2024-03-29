package apryraz.tworld;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TreasureWorldEnvTest {

    TreasureWorldEnv world;

    @BeforeEach
    void setUp() {
        world = new TreasureWorldEnv(4, 3, 3, "tests/pirates1.txt");
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
        AMessage msg1, msg2, msg3, msg4, msg5, error;
        msg1 = new AMessage("moveto", "2", "3", "");
        msg2 = new AMessage("moveto", "0", "3", "");
        msg3 = new AMessage("detectsat", "2", "3", "");
        msg4 = new AMessage("treasureup", "4", "4", "");
        msg5 = new AMessage("treasureup", "2", "2", "");
        error = new AMessage("Nobody expects", "the",
                "spanish", "inquisition");
        AMessage exp1, exp2, exp3, exp4, exp5, voidmsm;
        exp1 = new AMessage("movedto", "2", "3", "0");
        exp2 = new AMessage("notmovedto", "0", "3", "");
        exp3 = new AMessage("detected", "2", "3", "2");
        exp4 = new AMessage("treasureis", "4", "4", "up");
        exp5 = new AMessage("nopirate", "2", "2", "");
        voidmsm = new AMessage("voidmsg", "", "", "");
        assertEquals(exp1, world.acceptMessage(msg1));
        assertEquals(exp2, world.acceptMessage(msg2));
        assertEquals(exp3, world.acceptMessage(msg3));
        assertEquals(exp4, world.acceptMessage(msg4));
        assertEquals(exp5, world.acceptMessage(msg5));
        assertEquals(voidmsm, world.acceptMessage(error));
    }

    @Test
    void isPirateInMyCell() {

        assertEquals(0, world.isPirateInMyCell(new Position(2, 2)));
        assertEquals(1, world.isPirateInMyCell(new Position(4, 4)));
    }

    @Test
    void withinLimits() {
        assertTrue(world.withinLimits(new Position(1, 1)));
        assertTrue(world.withinLimits(new Position(2, 1)));
        assertTrue(world.withinLimits(new Position(4, 3)));
        assertFalse(world.withinLimits(new Position(0, 3)));
        assertFalse(world.withinLimits(new Position(5, 3)));
    }
}