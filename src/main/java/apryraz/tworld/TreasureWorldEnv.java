

package apryraz.tworld;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.System.exit;

public class TreasureWorldEnv {
    /**
     * X,Y position of Treasure and world dimension
     **/
    private int WorldDim;

    private List<Position> pirateList;
    private Position treasure;

    /**
     * Class constructor
     *
     * @param dim         dimension of the world
     * @param tx          X position of Treasure
     * @param ty          Y position of Treasure
     * @param piratesFile File with list of pirates locations
     **/
    public TreasureWorldEnv(int dim, int tx, int ty, String piratesFile) {
        treasure = new Position(tx, ty);
        WorldDim = dim;
        pirateList = loadPiratesLocations(piratesFile);
    }

    /**
     * Load the list of pirates locations. It has been made
     * static for easy testing.
     *
     * @param piratesFile name of the file that should contain a
     *                    set of pirate locations in a single line.
     * @return List of all the position where the pirates are.
     **/
    protected static List<Position> loadPiratesLocations(String piratesFile) {
        List<Position> pirates = new ArrayList<>();
        String[] stepsList;
        String steps = ""; // Prepare a list of movements to try with the FINDER Agent
        try {
            BufferedReader br = new BufferedReader(new FileReader(piratesFile));
            System.out.println("PIRATES FILE OPENED ...");
            steps = br.readLine();
            br.close();
        } catch (FileNotFoundException ex) {
            System.out.println("MSG.   => Pirates file not found");
            exit(1);
        } catch (IOException ex) {
            Logger.getLogger(TreasureFinder.class.getName()).log(Level.SEVERE, null, ex);
            exit(2);
        }
        stepsList = steps.split(" ");
        for (String s : stepsList) {
            String[] coords = s.split(",");
            pirates.add(new Position(Integer.parseInt(coords[0]), Integer.parseInt(coords[1])));
        }
        return pirates;
    }


    /**
     * Process a message received by the TFinder agent,
     * by returning an appropriate answer
     * This version only process answers to moveto and detectsat messages
     *
     * @param msg message sent by the Agent
     * @return a msg with the answer to return to the agent
     **/
    public AMessage acceptMessage(AMessage msg) {
        msg.showMessage();
        switch (msg.getComp(0)) {
            case "moveto":
                return getMoveTo(msg);
            case "detectsat":
                return getDetectSat(msg);
            case "treasureup":
                return getTreasureUp(msg);
        }
        //   ( "treasureup", "x", "y", "" )
        return new AMessage("voidmsg", "", "", "");

    }

    private AMessage getMoveTo(AMessage msg) {
        Position agent = new Position(Integer.parseInt(msg.getComp(1)),
                Integer.parseInt(msg.getComp(2)));
        if (withinLimits(agent)) {
            int pirate = isPirateInMyCell(agent);

            return new AMessage("movedto", msg.getComp(1), msg.getComp(2),
                    (Integer.valueOf(pirate)).toString());
        } else
            return new AMessage("notmovedto", msg.getComp(1), msg.getComp(2), "");
    }

    private AMessage getDetectSat(AMessage msg) {
        Position agent = new Position(Integer.parseInt(msg.getComp(1)),
                Integer.parseInt(msg.getComp(2)));
        if (agent.equals(treasure)) {
            return new AMessage("detected", msg.getComp(1),
                    msg.getComp(2), "1");
        } else if (agent.distanceOf(treasure) == 1) {
            return new AMessage("detected", msg.getComp(1),
                    msg.getComp(2), "2");
        } else if (agent.distanceOf(treasure) == 2) {
            return new AMessage("detected", msg.getComp(1),
                    msg.getComp(2), "3");
        }

        return new AMessage("detected", msg.getComp(1),
                msg.getComp(2), "0");
    }

    private AMessage getTreasureUp(AMessage msg) {
        Position agent = new Position(Integer.parseInt(msg.getComp(1)),
                Integer.parseInt(msg.getComp(2)));
        if (isPirateInMyCell(agent) == 1) {
            if (agent.y > treasure.y) {
                return new AMessage("treasureis", msg.getComp(1),
                        msg.getComp(2), "up");
            }
            return new AMessage("treasureis", msg.getComp(1),
                    msg.getComp(2), "down");
        }
        return new AMessage("nopirate", msg.getComp(1),
                msg.getComp(2), "");
    }

    /**
     * Check if there is a pirate in position (x,y)
     *
     * @param agent coordinate of agent position
     * @return 1  if (x,y) contains a pirate, 0 otherwise
     **/
    public int isPirateInMyCell(Position agent) {
        for (Position pirate : pirateList) {
            if (agent.equals(pirate)) {
                return 1;
            }
        }
        return 0;
    }


    /**
     * Check if position x,y is within the limits of the
     * WorldDim x WorldDim   world
     *
     * @param p agent position
     * @return true if position is within the limits of the world
     **/
    public boolean withinLimits(Position p) {
        return (p.x >= 1 && p.x <= WorldDim && p.y >= 1 && p.y <= WorldDim);
    }

    @Override
    public String toString() {
        return "TreasureWorldEnv{" +
                "WorldDim=" + WorldDim +
                ", pirateList=" + pirateList +
                ", treasure=" + treasure +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TreasureWorldEnv that = (TreasureWorldEnv) o;
        return WorldDim == that.WorldDim &&
                Objects.equals(pirateList, that.pirateList) &&
                Objects.equals(treasure, that.treasure);
    }

    @Override
    public int hashCode() {
        return Objects.hash(WorldDim, pirateList, treasure);
    }

    public int getWorldDim() {
        return WorldDim;
    }

    public Position getTreasure() {
        return treasure;
    }

    public List<Position> getPirateList() {
        return pirateList;
    }
}
