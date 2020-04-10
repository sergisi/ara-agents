package apryraz.tworld;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class MoreTreasureFinderTest {

    TreasureFinder tfinder = new TreasureFinder(5);

    @BeforeEach
    void setUp() {
        TreasureWorldEnv envAgent = new TreasureWorldEnv(4, 3, 3, "tests/pirates1.txt");
        tfinder.setEnvironment(envAgent);
    }

    @Test
    void testNumberOfVars() {
        assertEquals(156, tfinder.solver.nVars());
    }

    @Test
    void testOffsets() {
        assertEquals(0, tfinder.treasurePastOffset);
        assertEquals(25, tfinder.pirateOffset);
        assertEquals(30, tfinder.upOffset);
        assertEquals(31, tfinder.detectorOffsets[0]);
        assertEquals(56, tfinder.detectorOffsets[1]);
        assertEquals(81, tfinder.detectorOffsets[2]);
        assertEquals(106, tfinder.detectorOffsets[3]);
        assertEquals(131, tfinder.treasureFutureOffset);
    }

    @Test
    void testNumberOfClausesGamma() {
        assertEquals(1811, tfinder.solver.nConstraints());
    }

    @Test
    void testProcessDetectorAnswer() {
        fail();
    }

    @Test
    void testProcessPirateAnswer() {
        fail();
    }
}
