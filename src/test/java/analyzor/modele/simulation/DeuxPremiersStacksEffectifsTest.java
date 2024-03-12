package analyzor.modele.simulation;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;


public class DeuxPremiersStacksEffectifsTest {
    @Test
    void stackEffectifRecreeEstDeMemeInstance() {
        final int N_TESTS = 10000;

        for (int i = 0; i < N_TESTS; i++) {
            DeuxPremiersStacksEffectifs stacksEffectifs = genererObjetRandom();
            long idGenere = BuilderStackEffectif.genererId(stacksEffectifs);
            StacksEffectifs objetRecree = BuilderStackEffectif.getStacksEffectifs(idGenere);
            assertInstanceOf(DeuxPremiersStacksEffectifs.class, objetRecree);
        }
    }

    @Test
    void stackEffectifRecreeALesMemesValeurs() {
        final int N_TESTS = 10000;
        final float MARGE_ERREUR = 0.01f;

        for (int i = 0; i < N_TESTS; i++) {
            DeuxPremiersStacksEffectifs stacksEffectifs = genererObjetRandom();
            long idGenere = BuilderStackEffectif.genererId(stacksEffectifs);
            StacksEffectifs objetRecree = BuilderStackEffectif.getStacksEffectifs(idGenere);

            assertEquals(stacksEffectifs.getDimensions(), objetRecree.getDimensions());

            for (int nDonnees = 0; nDonnees < stacksEffectifs.getDonnees().length; nDonnees++) {
                assertTrue(((stacksEffectifs.getDonnees()[nDonnees] - objetRecree.getDonnees()[nDonnees])
                        / stacksEffectifs.getDonnees()[nDonnees]) < MARGE_ERREUR);

            }

            for (int nPoids = 0; nPoids < stacksEffectifs.getDonnees().length; nPoids++) {
                assertTrue(((stacksEffectifs.getPoidsStacks()[nPoids] - objetRecree.getPoidsStacks()[nPoids])
                        / stacksEffectifs.getPoidsStacks()[nPoids]) < MARGE_ERREUR);
            }
        }
    }

    @Test
    void calculeBienLesStacksEffectifs() {
        int stackJoueur;
        int[] stacksVillains;
        DeuxPremiersStacksEffectifs deuxPremiersStacksEffectifs;


        /*
        TEST 1
        stack joueur intermédiaire
         */

        stackJoueur = 22;

        stacksVillains = new int[] {32, 15, 13, 8, 10};

        deuxPremiersStacksEffectifs = new DeuxPremiersStacksEffectifs(stackJoueur);
        for (int stack : stacksVillains) {
            deuxPremiersStacksEffectifs.ajouterStackVillain(stack);
        }

        assertEquals(deuxPremiersStacksEffectifs.getDonnees()[0], 22);
        assertEquals(deuxPremiersStacksEffectifs.getDonnees()[1], 15);

        /*
        TEST 2
        Stack joueur est inférieur à tous les autres
         */

        stackJoueur = 9;

        stacksVillains = new int[] {32, 15, 13, 18, 10};

        deuxPremiersStacksEffectifs = new DeuxPremiersStacksEffectifs(stackJoueur);
        for (int stack : stacksVillains) {
            deuxPremiersStacksEffectifs.ajouterStackVillain(stack);
        }

        assertEquals(deuxPremiersStacksEffectifs.getDonnees()[0], 9);
        assertEquals(deuxPremiersStacksEffectifs.getDonnees()[1], 1);

         /*
        TEST 3
        Stack joueur est supérieur à tous les autres
         */


        stackJoueur = 48;

        stacksVillains = new int[] {32, 15, 13, 8, 10};

        deuxPremiersStacksEffectifs = new DeuxPremiersStacksEffectifs(stackJoueur);
        for (int stack : stacksVillains) {
            deuxPremiersStacksEffectifs.ajouterStackVillain(stack);
        }

        assertEquals(deuxPremiersStacksEffectifs.getDonnees()[0], 32);
        assertEquals(deuxPremiersStacksEffectifs.getDonnees()[1], 15);

    }

    private DeuxPremiersStacksEffectifs genererObjetRandom() {
        final float MIN_STACK = 0.01f;
        final float MAX_STACK = 10000;

        Random random = new Random();

        float randomStack = random.nextFloat() * MAX_STACK;
        DeuxPremiersStacksEffectifs stacksEffectifs = new DeuxPremiersStacksEffectifs((int) randomStack);

        int randomNombreJoueurs = random.nextInt(2, 12);

        for (int i = 0; i < randomNombreJoueurs; i++) {
            randomStack = random.nextFloat() * MAX_STACK;
            stacksEffectifs.ajouterStackVillain((int) randomStack);
        }

        return stacksEffectifs;
    }
}
