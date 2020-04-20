package apryraz.tworld;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
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
    int idNextStep;
    /**
     * Array of clauses that represent conclusiones obtained in the last
     * call to the inference function, but rewritten using the "past" variables
     **/
    ArrayList<IVecInt> futureToPast;
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
    int worldDim;

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

    // Saves assumptions found at the current step
    VecInt assumptions;

    /**
     * The class constructor must create the initial Boolean formula with the
     * rules of the Treasure World, initialize the variables for indicating
     * that we do not have yet any movements to perform, make the initial state.
     *
     * @param wDim the dimension of the Treasure World
     **/
    public TreasureFinder(int wDim) {
        worldDim = wDim;
        try {
            solver = buildGamma();
        } catch (ContradictionException ex) { // deleted IOException (not happening)
            Logger.getLogger(TreasureFinder.class.getName()).log(Level.SEVERE, null, ex);
        }
        idNextStep = 0;
        System.out.println("STARTING TREASURE FINDER AGENT...");
        tfstate = new TFState(worldDim);  // Initialize state (matrix) of knowledge with '?'
        tfstate.printState();
        futureToPast = new ArrayList<>();
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
            ContradictionException, TimeoutException {
        pirateFound = 0;
        addLastFutureClausesToPastClauses();
        this.assumptions = new VecInt();
        processMoveAnswer(moveToNext());
        processDetectorSensorAnswer(DetectsAt());
        if (pirateFound == 1) {
            processPirateAnswer(IsTreasureUpOrDown());
        }
        performInferenceQuestions();
        tfstate.printState();  // Print the resulting knowledge matrix
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
        if (idNextStep < listOfSteps.size()) {
            nextPosition = listOfSteps.get(idNextStep);
            idNextStep++;
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
        moveans.showMessage();
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
    public void processDetectorSensorAnswer(AMessage ans) {
        ans.showMessage();
        if ("detected".equals(ans.getComp(0))) {
            int x = Integer.parseInt(ans.getComp(1));
            int y = Integer.parseInt(ans.getComp(2));
            int detects = Integer.parseInt(ans.getComp(3));
            assumptions.push(coordToLineal(x, y, detectorOffsets[detects]));
        }
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

    /**
     * Processes a pirate answer
     *
     * @param ans pirate answer. Should start with treasureis
     */
    public void processPirateAnswer(AMessage ans) {
        ans.showMessage();
        int y = Integer.parseInt(ans.getComp(2));
        String isUp = ans.getComp(0);
        if ("treasureis".equals(ans.getComp(0))) {
            // treasureis x y up|down
            assumptions.push(pirateOffset - 1 + Integer.parseInt(ans.getComp(2))); // Existeix pirata y
            assumptions.push(("up".equals(ans.getComp(3))) ? upOffset : -upOffset);
        } else {
            System.out.println("THIS SHOULDN'T HAPPEN: PIRATE IS NOT CONSISTENT");
        }
    }


    /**
     * This function should add all the clauses stored in the list
     * futureToPast to the formula stored in solver.
     * Use the function addClause( VecInt ) to add each clause to the solver
     **/
    public void addLastFutureClausesToPastClauses() throws
            ContradictionException {
        for (IVecInt clause : futureToPast) {
            solver.addClause(clause);
        }
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
    public void performInferenceQuestions() throws
            TimeoutException {
        // TODO: Clean
        futureToPast = new ArrayList<>();
        ArrayList<Position> positionsFound = new ArrayList<>();
        for (Position pos : tfstate.getUnknownPosition()) {
            assumptions.push(coordToLineal(pos, treasureFutureOffset));
            if (!(solver.isSatisfiable(assumptions))) {
                // Add conclusion to list, but rewritten with respect to "past" variables
                positionsFound.add(pos);
            }
            assumptions.pop();  // Deletes the pos variable
        }
        for (Position pos : positionsFound) {
            IVecInt concPast = new VecInt();
            concPast.insertFirst(-coordToLineal(pos, treasureFutureOffset));
            futureToPast.add(concPast);
            tfstate.set(pos, "X");
        }
    }

    /**
     * This function builds the initial logical formula of the agent and stores it
     * into the solver object.
     *
     * @return returns the solver object where the formula has been stored
     **/
    public ISolver buildGamma() throws
            ContradictionException {
        createSolver();
        addAtLeastOneTresureRule();
        addDetectorRule();
        addPirateRule();
        return solver;
    }

    /**
     * Initializes solver instance. It also initialitzates offset instances.
     */
    protected void createSolver() {
        int totalNumVariables = getTotalNumVariables();
        solver = SolverFactory.newDefault();
        solver.setTimeout(3600);
        solver.newVar(totalNumVariables);
    }

    /**
     * Adds pirate rules
     *
     * @throws ContradictionException by solver
     */
    protected void addPirateRule() throws ContradictionException {
        for (int pirate = 0; pirate < worldDim; pirate++) {
            for (int i = 1; i <= worldDim; i++) {
                for (int j = 1; j <= worldDim; j++) {
                    int[] clause = new int[3];
                    clause[0] = -(pirate + pirateOffset);
                    if (pirate + pirateOffset == 144) {
                        System.out.println('a');
                    }
                    if (j <= pirate + 1) {
                        clause[1] = -upOffset;
                    } else {
                        clause[1] = upOffset;
                    }
                    clause[2] = -coordToLineal(i, j, treasureFutureOffset);
                    solver.addClause(new VecInt(clause));
                }
            }
        }
    }

    /**
     * Adds all rules of detector without the rule specified
     * at addDetectorReturned1Rule
     */
    protected void addDetectorRule() throws ContradictionException {
        for (int i1 = 1; i1 <= worldDim; i1++) {
            for (int j1 = 1; j1 <= worldDim; j1++) {
                Position pos1 = new Position(i1, j1);
                for (int i2 = 1; i2 <= worldDim; i2++) {
                    for (int j2 = 1; j2 <= worldDim; j2++) {
                        Position pos2 = new Position(i2, j2);
                        addDetectorRulePosition(pos1, pos2);
                    }
                }
            }
        }
    }

    /**
     * Adds reletaed to two positions all the rules concerning detections
     *
     * @param pos1 the first position, where the detector belongs
     * @param pos2 the second position, where is compared
     * @throws ContradictionException by solver.
     */
    protected void addDetectorRulePosition(Position pos1, Position pos2) throws ContradictionException {
        int distance = pos1.distanceOf(pos2);
        if (distance < 3) {
            ruleOffset(pos1, pos2, 0);
        }
        if (distance != 0) {
            ruleOffset(pos1, pos2, 1);
        }
        if (distance != 1) {
            ruleOffset(pos1, pos2, 2);
        }
        if (distance != 2) {
            ruleOffset(pos1, pos2, 3);
        }
    }

    private void ruleOffset(Position pos1, Position pos2, int detector) throws ContradictionException {
        int[] clause = new int[2];
        clause[0] = -coordToLineal(pos1, detectorOffsets[detector]);
        clause[1] = -coordToLineal(pos2, treasureFutureOffset);
        solver.addClause(new VecInt(clause));
    }

    /**
     * Adds the rule that at least a treasure is in the search
     *
     * @throws ContradictionException some exception of the solver.
     */
    protected void addAtLeastOneTresureRule() throws ContradictionException {
        int[] constr = new int[pirateOffset - treasurePastOffset];
        for (int i = 0; i < worldDim * worldDim; i++) {
            constr[i] = i + treasurePastOffset;  // clause with 1 2 3 4 ... pirateOffset 0
        }
        solver.addClause(new VecInt(constr));
    }

    private int getTotalNumVariables() {
        // initializate offset instances
        int worldLinealDim = worldDim * worldDim;
        treasurePastOffset = 1;
        pirateOffset = worldLinealDim + treasurePastOffset;
        upOffset = pirateOffset + worldDim;
        detectorOffsets[0] = upOffset + 1;
        for (int i = 0; i < 3; i++) {
            detectorOffsets[i + 1] = detectorOffsets[i] + worldLinealDim;
        }
        treasureFutureOffset = detectorOffsets[3] + worldLinealDim;
        return treasureFutureOffset + worldLinealDim;
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
     * Convert a coordinate pair (x,y) to the integer value  t_[x,y]
     * of variable that stores that information in the formula, using
     * offset as the initial index for that subset of position variables
     * (past and future position variables have different variables, so different
     * offset values)
     *
     * @param pos    postion coordinate
     * @param offset initial value for the subset of position variables
     *               (past or future subset)
     * @return the integer indentifer of the variable  b_[x,y] in the formula
     **/
    public int coordToLineal(Position pos, int offset) {
        return coordToLineal(pos.x, pos.y, offset);
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
    public Position linealToCoord(int lineal, int offset) {
        lineal = lineal - offset + 1;
        int x = (lineal - 1) / worldDim + 1;
        int y = ((lineal - 1) % worldDim) + 1;
        return new Position(x, y);
    }

    public void setListOfSteps(ArrayList<Position> listOfSteps) {
        this.listOfSteps = listOfSteps;
    }
}
