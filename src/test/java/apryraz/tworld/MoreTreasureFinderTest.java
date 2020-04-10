package apryraz.tworld;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

public class MoreTreasureFinderTest {

    TreasureFinder tfinder = new TreasureFinder(4);

    @BeforeEach
    void setUp() {
        TreasureWorldEnv envAgent = new TreasureWorldEnv(4, 3, 3, "tests/pirates1.txt");
        tfinder.setEnvironment(envAgent);
    }

    @Test
    void testProcessDetectorAnswer() {
        fail();
    }

    @Test
    void testProcessPirateAnswer() {
        fail();
    }
}
