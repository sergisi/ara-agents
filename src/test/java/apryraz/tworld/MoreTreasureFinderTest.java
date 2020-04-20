package apryraz.tworld;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class MoreTreasureFinderTest {

    TreasureFinder tfinder = new TreasureFinder(5);

    @BeforeEach
    void setUp() {
        TreasureWorldEnv envAgent = new TreasureWorldEnv(4, 3, 3, "tests/pirates2.txt");
        tfinder.setEnvironment(envAgent);
    }

    @Test
    void testNumberOfVars() {
        assertEquals(157, tfinder.solver.nVars());
    }

    @Test
    void testOffsets() {
        assertEquals(1, tfinder.treasurePastOffset);
        assertEquals(26, tfinder.pirateOffset);
        assertEquals(31, tfinder.upOffset);
        assertEquals(32, tfinder.detectorOffsets[0]);
        assertEquals(57, tfinder.detectorOffsets[1]);
        assertEquals(82, tfinder.detectorOffsets[2]);
        assertEquals(107, tfinder.detectorOffsets[3]);
        assertEquals(132, tfinder.treasureFutureOffset);
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
        AMessage message = new AMessage("treasureis", "1", "3", "down");
        tfinder.processPirateAnswer(message);
        int[] expected = new int[]{tfinder.pirateOffset + 3 - 1, -tfinder.upOffset};
        assertEquals(new VecInt(expected), tfinder.assumptions);
    }

    @Test
    void testInferenceIsOkay() throws TimeoutException, ContradictionException {
        ArrayList<Position> steps = new ArrayList<>();
        steps.add(new Position(1, 1));
        steps.add(new Position(2, 1));
        tfinder.setListOfSteps(steps);
        tfinder.runNextStep();
        assertEquals("?", tfinder.tfstate.get(new Position(1, 3)));
        assertEquals("?", tfinder.tfstate.get(new Position(3, 1)));
        tfinder.runNextStep();
        assertEquals("?", tfinder.tfstate.get(new Position(2, 3)));
        assertEquals("X", tfinder.tfstate.get(new Position(3, 2)));
    }

    @Test
    void testPirateInferenceIsOkay() throws TimeoutException, ContradictionException {
        ArrayList<Position> steps = new ArrayList<>();
        steps.add(new Position(1, 3));
        tfinder.setListOfSteps(steps);
        tfinder.runNextStep();
        assertEquals(new VecInt(new int[]{109, tfinder.pirateOffset + 2, -tfinder.upOffset}), tfinder.assumptions);
        assertEquals("?", tfinder.tfstate.get(new Position(3, 1)));
        assertEquals("?", tfinder.tfstate.get(new Position(3, 2)));
        assertEquals("?", tfinder.tfstate.get(new Position(3, 3)));
        assertEquals("X", tfinder.tfstate.get(new Position(3, 4)));
    }

    @Test
    void testSolverSolvable() throws TimeoutException {
        boolean expectedTrue = tfinder.solver.isSatisfiable(new VecInt(new int[]
                {109, 28, -31, tfinder.coordToLineal(new Position(3, 3), tfinder.treasureFutureOffset)}));
        assertTrue(expectedTrue);
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
