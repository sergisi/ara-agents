package apryraz.tworld;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

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
        assertEquals(2001, tfinder.solver.nConstraints());
    }

    @Test
    void testIfSatisfiable() throws TimeoutException {
        assertTrue(tfinder.solver.isSatisfiable());
    }

    @Test
    void testEspecificNumberOfClauses() throws ContradictionException {
        tfinder.createSolver(); // restarts solver
        assertEquals(0, tfinder.solver.nConstraints());
        tfinder.addAtLeastOneTresureRule();
        assertEquals(1, tfinder.solver.nConstraints());
        tfinder.addDetectorRule();
        assertEquals(1876, tfinder.solver.nConstraints()); // is it?
        tfinder.addPirateRule();
        assertEquals(2001, tfinder.solver.nConstraints());
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
