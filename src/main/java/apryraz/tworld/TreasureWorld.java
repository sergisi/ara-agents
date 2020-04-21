package apryraz.tworld;


import org.sat4j.reader.ParseFormatException;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

import java.io.IOException;


/**
 * The class for the main program of the Treasure World
 **/
public class TreasureWorld {


    /**
     * This function should execute the sequence of steps stored in the file fileSteps,
     * but only up to numSteps steps. Each step must be executed with function
     * runNextStep() of the BarcenasFinder agent.
     *
     * @param wDim        the dimension of world
     * @param tx          x coordinate of Barcenas position
     * @param ty          y coordinate of Barcenas position
     * @param numSteps    num of steps to perform
     * @param fileSteps   file name with sequence of steps to perform
     * @param filePirates file name with sequence of steps to perform
     * @throws ContradictionException by solver
     * @throws TimeoutException by solver
     **/
    public static void runStepsSequence(int wDim, int tx, int ty,
                                        int numSteps, String fileSteps, String filePirates) throws
            ContradictionException, TimeoutException {
        // Make instances of TreasureFinder agent and environment object classes
        TreasureFinder finder = new TreasureFinder(wDim);
        TreasureWorldEnv worldEnv = new TreasureWorldEnv(wDim, tx, ty, filePirates);

        finder.setEnvironment(worldEnv);
        finder.loadListOfSteps(numSteps, fileSteps);

        for (int i = 0; i < numSteps; i++) {
            finder.runNextStep();
        }
    }

    /**
     * This function should load five arguments from the command line:
     * @param args described as:
     * args[0] = dimension of the word
     * args[1] = x coordinate of treasure position
     * args[2] = y coordinate of treasure position
     * args[3] = num of steps to perform
     * args[4] = file name with sequence of steps to perform
     * args[5] = file name with list of pirate positions
     * @throws ContradictionException by solver
     * @throws TimeoutException by solver
     **/
    public static void main(String[] args) throws
            ContradictionException, TimeoutException {
        if (args.length != 6) {
            System.out.println("ERROR: Number of arguments is not correct: " + args.length);
            System.exit(1);
        }
        // Here I run a concrete example, but you should read parameters from
        // the command line, as decribed above.
        runStepsSequence(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]),
                Integer.parseInt(args[3]), args[4], args[5]);
    }

}
