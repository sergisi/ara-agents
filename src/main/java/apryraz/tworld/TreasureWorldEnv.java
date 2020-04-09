

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
     * Load the list of pirates locations
     *
     * @param: name of the file that should contain a
     * set of pirate locations in a single line.
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
        AMessage ans = new AMessage("voidmsg", "", "", "");

        msg.showMessage();
        if (msg.getComp(0).equals("moveto")) {
            int nx = Integer.parseInt(msg.getComp(1));
            int ny = Integer.parseInt(msg.getComp(2));

            if (withinLimits(nx, ny)) {
                int pirate = isPirateInMyCell(nx, ny);

                ans = new AMessage("movedto", msg.getComp(1), msg.getComp(2),
                        (Integer.valueOf(pirate)).toString());
            } else
                ans = new AMessage("notmovedto", msg.getComp(1), msg.getComp(2), "");

        } else {
            // YOU MUST ANSWER ALSO TO THE OTHER MESSAGE TYPES:
            //   ( "detectsat", "x" , "y", "" )
            //   ( "treasureup", "x", "y", "" )
        }
        return ans;

    }

    /**
     * Check if there is a pirate in position (x,y)
     *
     * @param x x coordinate of agent position
     * @param y y coordinate of agent position
     * @return 1  if (x,y) contains a pirate, 0 otherwise
     **/
    public int isPirateInMyCell(int x, int y) {
        return -1;
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
