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

    public void initializeState() {
        for (int i = 0; i < wDim; i++) {
            for (int j = 0; j < wDim; j++) {
                matrix[i][j] = "?";
                unknown.add(new Position(i+1, j+1));
            }
        }
    }

    public String get(int i, int j) {
        return matrix[i - 1][j - 1];
    }

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

    public HashSet<Position> getUnknownPosition() {
        return null;
    }
}
