package apryraz.tworld;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        tfinder.assumptions = new VecInt();
        AMessage detected = new AMessage("detected", "2", "3", "2");
        tfinder.processDetectorSensorAnswer(detected);
        int[] expected = new int[]{tfinder.coordToLineal(new Position(2, 3), tfinder.detectorOffsets[2])};
        assertEquals(new VecInt(expected), tfinder.assumptions);
    }

    @Test
    void testProcessPirateAnswer() {
        tfinder.assumptions = new VecInt();
        AMessage message = new AMessage("treasureis", "4", "4", "up");
        tfinder.processPirateAnswer(message);
        int[] expected = new int[]{tfinder.pirateOffset + 4 - 1, tfinder.upOffset};
        assertEquals(new VecInt(expected), tfinder.assumptions);
    }

    @Test
    void testInferenceIsOkay() throws TimeoutException, ContradictionException {
        ArrayList<Position> steps = new ArrayList<>();
        steps.add(new Position(2, 1));
        tfinder.setListOfSteps(steps);
        tfinder.runNextStep();
        assertEquals("?", tfinder.tfstate.get(1, 3));
        assertEquals("X", tfinder.tfstate.get(3, 1));
    }

    @Test
    void testCoord() {
        Position pos = new Position(3, 2);
        int l = tfinder.coordToLineal(pos, tfinder.treasureFutureOffset);
        assertEquals(tfinder.linealToCoord(l, tfinder.treasureFutureOffset), pos);
        l = tfinder.coordToLineal(3, 2, tfinder.treasureFutureOffset);
        assertEquals(tfinder.linealToCoord(l, tfinder.treasureFutureOffset), pos);
    }

    @Test
    void testMoreCoord() {
        int l = tfinder.treasureFutureOffset + 1;
        Position pos = tfinder.linealToCoord(l, tfinder.treasureFutureOffset);
        assertEquals(tfinder.coordToLineal(pos, tfinder.treasureFutureOffset), l);
    }

}
