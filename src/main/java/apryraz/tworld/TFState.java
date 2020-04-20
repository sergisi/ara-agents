package apryraz.tworld;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class TFState {
    /**
     *
     **/

    int wDim;
    String[][] matrix;
    HashSet<Position> unknown;

    public TFState(int dim) {
        wDim = dim;
        matrix = new String[wDim][wDim];
        unknown = new HashSet<>();
        initializeState();
    }

    /**
     * Initialises state
     */
    public void initializeState() {
        for (int i = 0; i < wDim; i++) {
            for (int j = 0; j < wDim; j++) {
                matrix[i][j] = "?";
                unknown.add(new Position(i+1, j+1));
            }
        }
    }

    /**
     * Gets a position
     * @param i coodinate x of position
     * @param j coordinate y of position
     * @return the value stored in the matrix of positions
     */
    public String get(int i, int j) {
        return matrix[i - 1][j - 1];
    }

    /**
     * Sets a position
     * @param pos Postion to be changed
     * @param val value to be set. Either "?" or "X".
     */
    public void set(Position pos, String val) {
        if ("X".equals(val))
            unknown.remove(pos);
        matrix[pos.x -1][pos.y-1] = val;
    }

    @Override
    public String toString() {
        return "TFState{" +
                "wDim=" + wDim +
                ", matrix=" + Arrays.toString(matrix) +
                ", unknown=" + unknown +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TFState tfState = (TFState) o;
        return wDim == tfState.wDim &&
                Arrays.equals(matrix, tfState.matrix) &&
                Objects.equals(unknown, tfState.unknown);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(wDim, unknown);
        result = 31 * result + Arrays.hashCode(matrix);
        return result;
    }

    /**
     * Prints the treasure state
     */
    public void printState() {
        System.out.println("FINDER => Printing Treasure world matrix");
        for (int i = wDim - 1; i > -1; i--) {
            System.out.print("\t#\t");
            for (int j = 0; j < wDim; j++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println("\t#");
        }
    }

    /**
     * Loads file given the dimensions and the file as  TFState
     * @param wDim dimension of the treasure world
     * @param stateFile file conaining the state
     * @return the state of this world
     * @throws IOException if file does not exist (or other IOExceptions concerning the file)
     */
    public static TFState loadStateFromFile(int wDim, String stateFile) throws IOException {
        TFState tfstate = new TFState(wDim);
        String row;
        String[] rowvalues;
        BufferedReader br = new BufferedReader(new FileReader(stateFile));
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
     * Hash set containing all the positions with "?" in it.
     * It uses the idea that all updates should be done with set and
     * the val will only be "X".
     * @return hash set containing all unknown positions.
     */
    public HashSet<Position> getUnknownPosition() {
        return unknown;
    }
}
