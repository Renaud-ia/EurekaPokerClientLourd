package analyzor.modele.licence;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CleMachineTest {
    /**
     * vérifie que la clé machine générée est toujours identique
     */
    @Test
    void cleIdentique() {
        final int N_TESTS = 10;
        for (int i = 0; i < N_TESTS; i++) {
            assertTrue(CleMachine.verifier(CleMachine.generer()));
        }
    }
}
