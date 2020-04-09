package apryraz.tworld;


import org.sat4j.reader.ParseFormatException;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

import java.io.IOException;


/**
 * The class for the main program of the Barcenas World
 **/
public class TreasureWorld {


    /**
     * This function should execute the sequence of steps stored in the file fileSteps,
     * but only up to numSteps steps. Each step must be executed with function
     * runNextStep() of the BarcenasFinder agent.
     *
     * @param wDim        the dimension of world
     * @param tX          x coordinate of Barcenas position
     * @param tY          y coordinate of Barcenas position
     * @param numSteps    num of steps to perform
     * @param fileSteps   file name with sequence of steps to perform
     * @param filePirates file name with sequence of steps to perform
     **/
    public static void runStepsSequence(int wDim, int tX, int tY,
                                        int numSteps, String fileSteps, String filePirates) throws
            IOException, ContradictionException, TimeoutException {
        // Make instances of TreasureFinder agent and environment object classes
        TreasureFinder TAgent;
        TreasureWorldEnv EnvAgent;


        // Set environment object, and load list of pirate positions


        // load list of steps into the Finder Agent


        // Execute sequence of steps with the Agent

    }

    /**
     * This function should load five arguments from the command line:
     * arg[0] = dimension of the word
     * arg[1] = x coordinate of treasure position
     * arg[2] = y coordinate of treasure position
     * arg[3] = num of steps to perform
     * arg[4] = file name with sequence of steps to perform
     * arg[5] = file name with list of pirate positions
     **/
    public static void main(String[] args) throws ParseFormatException,
            IOException, ContradictionException, TimeoutException {

        // Here I run a concrete example, but you should read parameters from
        // the command line, as decribed above.
        runStepsSequence(4, 3, 3, 5, "tests/steps1.txt", "tests/pirates1.txt");
    }

}
