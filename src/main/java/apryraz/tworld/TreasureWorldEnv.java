

package apryraz.tworld;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
     *             set of pirate locations in a single line.
     * @return List of all the position where the pirates are.
     **/
    protected static List<Position> loadPiratesLocations(String piratesFile) {
        return new ArrayList<>();
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
        if (msg.getComp(0).equals("moveto")) {
            int nx = Integer.parseInt(msg.getComp(1));
            int ny = Integer.parseInt(msg.getComp(2));
            if (withinLimits(nx, ny)) {
                int pirate = isPirateInMyCell(nx, ny);

                return new AMessage("movedto", msg.getComp(1), msg.getComp(2),
                        (Integer.valueOf(pirate)).toString());
            } else
                return new AMessage("notmovedto", msg.getComp(1), msg.getComp(2), "");
        }
        // YOU MUST ANSWER ALSO TO THE OTHER MESSAGE TYPES:
        //   ( "detectsat", "x" , "y", "" )
        //   ( "treasureup", "x", "y", "" )
        return new AMessage("voidmsg", "", "", "");

    }

    /**
     * Check if there is a pirate in position (x,y)
     *
     * @param x x coordinate of agent position
     * @param y y coordinate of agent position
     * @return 1  if (x,y) contains a pirate, 0 otherwise
     **/
    public int isPirateInMyCell(int x, int y) {
        Position agent = new Position(x, y);
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
     * @param x x coordinate of agent position
     * @param y y coordinate of agent position
     * @return true if (x,y) is within the limits of the world
     **/
    public boolean withinLimits(int x, int y) {
        return (x >= 1 && x <= WorldDim && y >= 1 && y <= WorldDim);
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
