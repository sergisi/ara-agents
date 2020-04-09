package apryraz.tworld;

import java.util.Arrays;
import java.util.Objects;

public class TFState {
    /**
     *
     **/

    int wDim;
    String[][] matrix;

    public TFState(int dim) {
        wDim = dim;
        matrix = new String[wDim][wDim];
        initializeState();
    }

    public void initializeState() {
        for (int i = 0; i < wDim; i++) {
            for (int j = 0; j < wDim; j++) {
                matrix[i][j] = "?";
            }
        }
    }

    public String get(int i, int j) {
        return matrix[i - 1][j - 1];
    }

    public void set(int i, int j, String val) {
        matrix[i - 1][j - 1] = val;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TFState tfState = (TFState) o;
        return wDim == tfState.wDim &&
                Arrays.deepEquals(matrix, tfState.matrix);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(wDim);
        result = 31 * result + Arrays.hashCode(matrix);
        return result;
    }

    @Override
    public String toString() {
        return "TFState{" +
                "wDim=" + wDim +
                ", matrix=" + Arrays.toString(matrix) +
                '}';
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

}
