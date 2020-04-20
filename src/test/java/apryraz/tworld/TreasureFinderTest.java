package apryraz.tworld;

import org.junit.jupiter.api.Test;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.System.exit;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Class for testing the TreasureFinder agent
 **/
public class TreasureFinderTest {


    /**
     * This function should execute the next step of the agent, and the assert
     * whether the resulting state is equal to the targetState
     *
     * @param tAgent      TreasureFinder agent
     * @param targetState the state that should be equal to the resulting state of
     *                    the agent after performing the next step
     **/
    public void testMakeSimpleStep(TreasureFinder tAgent,
                                   TFState targetState) throws
            ContradictionException, TimeoutException {
        tAgent.runNextStep();
        TFState currentState = tAgent.getState();
        assertEquals(currentState, targetState);
    }


    /**
     * Read an state from the current position of the file trough the
     * BufferedReader object
     *
     * @param br   BufferedReader object interface to the opened file of states
     * @param wDim dimension of the world
     **/
    public TFState readTargetStateFromFile(BufferedReader br, int wDim) throws
            IOException {
        TFState tfstate = new TFState(wDim);
        String row;
        String[] rowvalues;

        for (int i = wDim; i >= 1; i--) {
            row = br.readLine();
            rowvalues = row.split(" ");
            for (int j = 1; j <= wDim; j++) {
                tfstate.set(new Position(i, j), rowvalues[j - 1]);
            }
        }
        return tfstate;
    }

    /**
     * Load a sequence of states from a file, and return the list
     *
     * @param wDim       dimension of the world
     * @param numStates  num of states to read from the file
     * @param statesFile file name with sequence of target states, that should
     *                   be the resulting states after each movement in fileSteps
     * @return returns an ArrayList of TFState with the resulting list of states
     **/
    ArrayList<TFState> loadListOfTargetStates(int wDim, int numStates, String statesFile) {
        ArrayList<TFState> listOfStates = new ArrayList<>(numStates);
        try {
            BufferedReader br = new BufferedReader(new FileReader(statesFile));
            // steps = br.readLine();
            for (int s = 0; s < numStates; s++) {
                listOfStates.add(readTargetStateFromFile(br, wDim));
                // Read a blank line between states
                br.readLine();
            }
            br.close();
        } catch (FileNotFoundException ex) {
            System.out.println("MSG.   => States file not found");
            exit(1);
        } catch (IOException ex) {
            Logger.getLogger(TreasureFinderTest.class.getName()).log(Level.SEVERE, null, ex);
            exit(2);
        }
        return listOfStates;
    }


    /**
     * This function should run the sequence of steps stored in the file fileSteps,
     * but only up to numSteps steps.
     *
     * @param wDim        the dimension of world
     * @param tX          x coordinate of Treasure position
     * @param tY          y coordinate of Treasure position
     * @param numSteps    num of steps to perform
     * @param fileSteps   file name with sequence of steps to perform
     * @param fileStates  file name with sequence of target states, that should
     *                    be the resulting states after each movement in fileSteps
     * @param filePirates file containing all the positions of the pirates
     **/
    public void testMakeSeqOfSteps(int wDim, int tX, int tY,
                                   int numSteps, String fileSteps, String fileStates,
                                   String filePirates)
            throws ContradictionException, TimeoutException {
        TreasureFinder finder = new TreasureFinder(wDim);
        TreasureWorldEnv worldEnv = new TreasureWorldEnv(wDim, tX, tY, filePirates);
        ArrayList<TFState> seqOfStates = loadListOfTargetStates(wDim, numSteps, fileStates);

        finder.loadListOfSteps(numSteps, fileSteps);
        finder.setEnvironment(worldEnv);
        for (TFState state : seqOfStates) {
            testMakeSimpleStep(finder, state);
        }
    }

    /**
     * This is an example test. You must replicate this method for each different
     * test sequence, or use some kind of parametric tests with junit
     **/
    @Test
    public void TWorldTest1() throws
            ContradictionException, TimeoutException {
        // Example test for 4x4 world , Treasure at 3,3 and 5 steps
        testMakeSeqOfSteps(4, 3, 3, 5, "tests/steps1.txt", "tests/states1.txt",
                "tests/pirates1.txt");
        testMakeSeqOfSteps(6, 4, 4, 5, "tests/steps2.txt", "tests/states2.txt",
                "tests/pirates2.txt");
        testMakeSeqOfSteps(7, 5, 4, 7, "tests/steps3.txt", "tests/states3.txt",
                "tests/pirates3.txt");
        testMakeSeqOfSteps(8, 3, 7, 6, "tests/steps4.txt", "tests/states4.txt",
                "tests/pirates4.txt");
    }

}
