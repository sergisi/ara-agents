

package apryraz.tworld;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.System.exit;


/**
 * This agent performs a sequence of movements, and after each
 * movement it "senses" from the evironment the resulting position
 * and then the outcome from the smell sensor, to try to locate
 * the position of Treasure
 **/
public class TreasureFinder {


    /**
     * The list of steps to perform
     **/
    ArrayList<Position> listOfSteps;
    /**
     * index to the next movement to perform, and total number of movements
     **/
    int idNextStep, numMovements;
    /**
     * Array of clauses that represent conclusiones obtained in the last
     * call to the inference function, but rewritten using the "past" variables
     **/
    ArrayList<VecInt> futureToPast = null;
    /**
     * the current state of knowledge of the agent (what he knows about
     * every position of the world)
     **/
    TFState tfstate;
    /**
     * The object that represents the interface to the Treasure World
     **/
    TreasureWorldEnv envAgent;
    /**
     * SAT solver object that stores the logical boolean formula with the rules
     * and current knowledge about not possible locations for Treasure
     **/
    ISolver solver;
    /**
     * Agent position in the world and variable to record if there is a pirate
     * at that current position
     **/
    int agentX, agentY, pirateFound;
    /**
     * Dimension of the world and total size of the world (Dim^2)
     **/
    int worldDim, worldLinealDim;

    /**
     * This set of variables CAN be use to mark the beginning of different sets
     * of variables in your propositional formula (but you may have more sets of
     * variables in your solution).
     **/
    int treasurePastOffset;
    int pirateOffset;
    int upOffset;
    int[] detectorOffsets = new int[4];
    int treasureFutureOffset;

    int actualLiteral;

    /**
     * The class constructor must create the initial Boolean formula with the
     * rules of the Treasure World, initialize the variables for indicating
     * that we do not have yet any movements to perform, make the initial state.
     *
     * @param wDim the dimension of the Treasure World
     **/
    public TreasureFinder(int wDim) {

        worldDim = wDim;
        worldLinealDim = worldDim * worldDim;

        try {
            solver = buildGamma();
        } catch (IOException | ContradictionException ex) {
            Logger.getLogger(TreasureFinder.class.getName()).log(Level.SEVERE, null, ex);
        }
        numMovements = 0;
        idNextStep = 0;
        System.out.println("STARTING TREASURE FINDER AGENT...");


        tfstate = new TFState(worldDim);  // Initialize state (matrix) of knowledge with '?'
        tfstate.printState();
    }

    /**
     * Store a reference to the Environment Object that will be used by the
     * agent to interact with the Treasure World, by sending messages and getting
     * answers to them. This function must be called before trying to perform any
     * steps with the agent.
     *
     * @param environment the Environment object
     **/
    public void setEnvironment(TreasureWorldEnv environment) {

        envAgent = environment;
    }


    /**
     * Load a sequence of steps to be performed by the agent. This sequence will
     * be stored in the listOfSteps ArrayList of the agent.  Steps are represented
     * as objects of the class Position.
     *
     * @param numSteps  number of steps to read from the file
     * @param stepsFile the name of the text file with the line that contains
     *                  the sequence of steps: x1,y1 x2,y2 ...  xn,yn
     **/
    public void loadListOfSteps(int numSteps, String stepsFile) {
        String[] stepsList;
        String steps = ""; // Prepare a list of movements to try with the FINDER Agent
        try {
            BufferedReader br = new BufferedReader(new FileReader(stepsFile));
            System.out.println("STEPS FILE OPENED ...");
            steps = br.readLine();
            br.close();
        } catch (FileNotFoundException ex) {
            System.out.println("MSG.   => Steps file not found");
            exit(1);
        } catch (IOException ex) {
            Logger.getLogger(TreasureFinder.class.getName()).log(Level.SEVERE, null, ex);
            exit(2);
        }
        stepsList = steps.split(" ");
        listOfSteps = new ArrayList<>(numSteps);
        for (int i = 0; i < numSteps; i++) {
            String[] coords = stepsList[i].split(",");
            listOfSteps.add(new Position(Integer.parseInt(coords[0]), Integer.parseInt(coords[1])));
        }
        numMovements = listOfSteps.size(); // Initialization of numMovements
        idNextStep = 0;
    }

    /**
     * Returns the current state of the agent.
     *
     * @return the current state of the agent, as an object of class TFState
     **/
    public TFState getState() {
        return tfstate;
    }

    /**
     * Execute the next step in the sequence of steps of the agent, and then
     * use the agent sensor to get information from the environment. In the
     * original Treasure World, this would be to use the Smelll Sensor to get
     * a binary answer, and then to update the current state according to the
     * result of the logical inferences performed by the agent with its formula.
     **/
    public void runNextStep() throws
            IOException, ContradictionException, TimeoutException {
        pirateFound = 0;
        // Add the conclusions obtained in the previous step
        // but as clauses that use the "past" variables
        addLastFutureClausesToPastClauses();

        // Ask to move, and check whether it was successful
        // Also, record if a pirate was found at that position
        processMoveAnswer(moveToNext());


        // Next, use Detector sensor to discover new information
        processDetectorSensorAnswer(DetectsAt());
        // If a pirate was found at new agent position, ask question to
        // pirate and process Answer to discover new information
        if (pirateFound == 1) {
            processPirateAnswer(IsTreasureUpOrDown());
        }

        // Perform logical consequence questions for all the positions
        // of the Treasure World
        performInferenceQuestions();
        tfstate.printState();      // Print the resulting knowledge matrix
    }


    /**
     * Ask the agent to move to the next position, by sending an appropriate
     * message to the environment object. The answer returned by the environment
     * will be returned to the caller of the function.
     *
     * @return the answer message from the environment, that will tell whether the
     * movement was successful or not.
     **/
    public AMessage moveToNext() {
        Position nextPosition;

        if (idNextStep < numMovements) {
            nextPosition = listOfSteps.get(idNextStep);
            idNextStep = idNextStep + 1;
            return moveTo(nextPosition.x, nextPosition.y);
        } else {
            System.out.println("NO MORE steps to perform at agent!");
            return (new AMessage("NOMESSAGE", "", "", ""));
        }
    }

    /**
     * Use agent "actuators" to move to (x,y)
     * We simulate this why telling to the World Agent (environment)
     * that we want to move, but we need the answer from it
     * to be sure that the movement was made with success
     *
     * @param x horizontal coordinate of the movement to perform
     * @param y vertical coordinate of the movement to perform
     * @return returns the answer obtained from the environment object to the
     * moveto message sent
     **/
    public AMessage moveTo(int x, int y) {
        // Tell the EnvironmentAgentID that we want  to move
        AMessage msg, ans;

        msg = new AMessage("moveto", (Integer.valueOf(x)).toString(), (Integer.valueOf(y)).toString(), "");
        ans = envAgent.acceptMessage(msg);
        System.out.println("FINDER => moving to : (" + x + "," + y + ")");

        return ans;
    }

    /**
     * Process the answer obtained from the environment when we asked
     * to perform a movement
     *
     * @param moveans the answer given by the environment to the last move message
     **/
    public void processMoveAnswer(AMessage moveans) {
        if (moveans.getComp(0).equals("movedto")) {
            agentX = Integer.parseInt(moveans.getComp(1));
            agentY = Integer.parseInt(moveans.getComp(2));
            pirateFound = Integer.parseInt(moveans.getComp(3));
            System.out.println("FINDER => moved to : (" + agentX + "," + agentY + ")" + " Pirate found : " + pirateFound);
        }
    }

    /**
     * Send to the environment object the question:
     * "Does the detector sense something around(agentX,agentY) ?"
     *
     * @return return the answer given by the environment
     **/
    public AMessage DetectsAt() {
        AMessage msg, ans;

        msg = new AMessage("detectsat", (Integer.valueOf(agentX)).toString(),
                (Integer.valueOf(agentY)).toString(), "");
        ans = envAgent.acceptMessage(msg);
        System.out.println("FINDER => detecting at : (" + agentX + "," + agentY + ")");
        return ans;
    }


    /**
     * Process the answer obtained for the query "Detects at (x,y)?"
     * by adding the appropriate evidence clause to the formula
     *
     * @param ans message obtained to the query "Detects at (x,y)?".
     *            It will a message with three fields: [0,1,2,3] x y
     **/
    public void processDetectorSensorAnswer(AMessage ans) throws
            IOException, ContradictionException, TimeoutException {

        int x = Integer.parseInt(ans.getComp(1));
        int y = Integer.parseInt(ans.getComp(2));
        String detects = ans.getComp(0);

        // Call your function/functions to add the evidence clauses
        // to Gamma to then be able to infer new NOT possible positions


        // CALL your functions HERE
    }


    /**
     * Send to the pirate (using the environment object) the question:
     * "Is the treasure up or down of (agentX,agentY)  ?"
     *
     * @return return the answer given by the pirate
     **/
    public AMessage IsTreasureUpOrDown() {
        AMessage msg, ans;

        msg = new AMessage("treasureup", (Integer.valueOf(agentX)).toString(),
                (Integer.valueOf(agentY)).toString(), "");
        ans = envAgent.acceptMessage(msg);
        System.out.println("FINDER => checking treasure up of : (" + agentX + "," + agentY + ")");
        return ans;
    }

    public void processPirateAnswer(AMessage ans) throws
            IOException, ContradictionException, TimeoutException {

        int y = Integer.parseInt(ans.getComp(2));
        String isUp = ans.getComp(0);
        if ("treasureis".equals(ans.getComp(0))) {
            // isUp should be either "yes" (is up of agent position), or "no"

            // Call your function/functions to add the evidence clauses
            // to Gamma to then be able to infer new NOT possible positions

            // CALL your functions HERE to update the solver object with more
            // clauses
        }
    }


    /**
     * This function should add all the clauses stored in the list
     * futureToPast to the formula stored in solver.
     * Use the function addClause( VecInt ) to add each clause to the solver
     **/
    public void addLastFutureClausesToPastClauses() throws IOException,
            ContradictionException, TimeoutException {


    }

    /**
     * This function should check, using the future variables related
     * to possible positions of Treasure, whether it is a logical consequence
     * that Treasure is NOT at certain positions. This should be checked for all the
     * positions of the Treasure World.
     * The logical consequences obtained, should be then stored in the futureToPast list
     * but using the variables corresponding to the "past" variables of the same positions
     * <p>
     * An efficient version of this function should try to not add to the futureToPast
     * conclusions that were already added in previous steps, although this will not produce
     * any bad functioning in the reasoning process with the formula.
     **/
    public void performInferenceQuestions() throws IOException,
            ContradictionException, TimeoutException {
        // EXAMPLE code to check this for position (2,3):
        // Get variable number for position 2,3 in past variables
        int linealIndex = coordToLineal(2, 3, treasureFutureOffset);
        // Get the same variable, but in the past subset
        int linealIndexPast = coordToLineal(2, 3, treasurePastOffset);

        VecInt variablePositive = new VecInt();
        variablePositive.insertFirst(linealIndex);

        // Check if Gamma + variablePositive is unsatisfiable:
        // This is only AN EXAMPLE for a specific position: (2,3)
        if (!(solver.isSatisfiable(variablePositive))) {
            // Add conclusion to list, but rewritten with respect to "past" variables
            VecInt concPast = new VecInt();
            concPast.insertFirst(-(linealIndexPast));

            futureToPast.add(concPast);
            tfstate.set(2, 3, "X");
        }

    }

    /**
     * This function builds the initial logical formula of the agent and stores it
     * into the solver object.
     *
     * @return returns the solver object where the formula has been stored
     **/
    public ISolver buildGamma() throws
            IOException, ContradictionException {

        // You must set this variable to the total number of boolean variables
        // in your formula Gamma
        int totalNumVariables = getTotalNumVariables();
        solver = SolverFactory.newDefault();
        solver.setTimeout(3600);
        solver.newVar(totalNumVariables);
        // This variable is used to generate, in a particular sequential order,
        // the variable indentifiers of all the variables
        actualLiteral = 1;

        // call here functions to add the differen sets of clauses
        // of Gamma to the solver object

        return solver;
    }

    private int getTotalNumVariables() {
        treasurePastOffset = 0;
        pirateOffset = worldDim * worldDim;
        upOffset = pirateOffset + worldDim;
        detectorOffsets[0] = upOffset + 1;
        for (int i = 0; i < 3; i++){
            detectorOffsets[i + 1] = detectorOffsets[i] + pirateOffset; //Pirate offset is n^2
        }
        treasureFutureOffset =  detectorOffsets[3] + pirateOffset;
        return treasureFutureOffset + pirateOffset;
    }


    /**
     * Convert a coordinate pair (x,y) to the integer value  t_[x,y]
     * of variable that stores that information in the formula, using
     * offset as the initial index for that subset of position variables
     * (past and future position variables have different variables, so different
     * offset values)
     *
     * @param x      x coordinate of the position variable to encode
     * @param y      y coordinate of the position variable to encode
     * @param offset initial value for the subset of position variables
     *               (past or future subset)
     * @return the integer indentifer of the variable  b_[x,y] in the formula
     **/
    public int coordToLineal(int x, int y, int offset) {
        return ((x - 1) * worldDim) + (y - 1) + offset;
    }

    /**
     * Perform the inverse computation to the previous function.
     * That is, from the identifier t_[x,y] to the coordinates  (x,y)
     * that it represents
     *
     * @param lineal identifier of the variable
     * @param offset offset associated with the subset of variables that
     *               lineal belongs to
     * @return array with x and y coordinates
     **/
    public int[] linealToCoord(int lineal, int offset) {
        lineal = lineal - offset + 1;
        int[] coords = new int[2];
        coords[1] = ((lineal - 1) % worldDim) + 1;
        coords[0] = (lineal - 1) / worldDim + 1;
        return coords;
    }


}
