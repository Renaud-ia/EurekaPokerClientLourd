package analyzor.modele.poker;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RangeReelleTest {
    @Test
    void nombreDeCombosConforme() {
        RangeReelle rangeReelle = new RangeReelle();
        assertEquals(0, rangeReelle.nCombos());

        rangeReelle.remplir();

        assertEquals(1326, rangeReelle.nCombos());
    }

    @Test
    void retirerUneCarteRetireLeBonNombreDeCombos() {
        RangeReelle rangeReelle = new RangeReelle();
        rangeReelle.remplir();

        int nCombos = rangeReelle.nCombos();

        Deck deck = new Deck();

        int nCartesRetires = 51;
        for (int i = 0; i < 10; i++) {
            Carte carte = deck.carteRandom();
            rangeReelle.retirerCarte(carte);
            nCombos -= nCartesRetires;

            assertEquals(nCombos, rangeReelle.nCombos());

            nCartesRetires--;
        }
    }

    @Test
    void conversionRangeIsoVersRangeReelleGardeLesMemesProportions() {
        RangeIso rangeIso = new RangeIso();
        rangeIso.rangeVide();

        HashMap<ComboIso, Float> combosAjoutes = new HashMap<>();

        for (int i = 0; i < 10; i++) {
            Random random = new Random();
            int randomIndexCombo = random.nextInt(0, GenerateurCombos.combosIso.size());
            ComboIso randomCombo = GenerateurCombos.combosIso.get(randomIndexCombo);

            if (combosAjoutes.containsKey(randomCombo)) continue;

            float randomValeur = random.nextFloat();
            combosAjoutes.put(randomCombo, randomValeur);

            rangeIso.incrementerCombo(randomCombo, randomValeur);
        }

        RangeReelle rangeReelle = new RangeReelle(rangeIso);

        for (ComboIso comboIso : combosAjoutes.keySet()) {
            float valeur = combosAjoutes.get(comboIso);

            for (ComboReel comboReel : comboIso.toCombosReels()) {
                assertEquals(valeur, rangeReelle.valeurCombo(comboReel));
            }
        }
    }

    /**
     * test pas vraiment efficace car même des algos pas top niveau précision le valident
     */
    @Test
    void echantillonRepresentatif() {
        final float deltaErreur = 0.30f;
        final int nEchantillons = 1000;

        // on crée une range réelle avec des valeurs Random de combos
        RangeIso rangeIso = new RangeIso();
        rangeIso.rangeVide();

        HashMap<ComboIso, Float> combosInitiaux = new HashMap<>();
        float totalValeursDepart = 0f;
        int nTotalCombosReels = 0;

        for (int i = 0; i < 10; i++) {
            Random random = new Random();
            int randomIndexCombo = random.nextInt(0, GenerateurCombos.combosIso.size());
            ComboIso randomCombo = GenerateurCombos.combosIso.get(randomIndexCombo);

            if (combosInitiaux.containsKey(randomCombo)) continue;

            float randomValeur = random.nextFloat();
            combosInitiaux.put(randomCombo, randomValeur);

            rangeIso.incrementerCombo(randomCombo, randomValeur);
            totalValeursDepart += randomValeur;
            nTotalCombosReels += randomCombo.getNombreCombos();
        }

        RangeReelle rangeReelle = new RangeReelle(rangeIso);

        // on prend un très grand échantillon et on vérifie que la proportion est à peu près identique
        List<ComboReel> grandEchantillon = rangeReelle.obtenirEchantillon(nEchantillons);

        HashMap<ComboIso, Float> combosEchantillon = new HashMap<>();

        // on compte les occurences de ComboIso de l'échantillon
        for (ComboReel comboEchantillon : grandEchantillon) {
            ComboIso comboConverti = new ComboIso(comboEchantillon);
            combosEchantillon.putIfAbsent(comboConverti, 0f);
            combosEchantillon.put(comboConverti, combosEchantillon.get(comboConverti) + 1);
        }

        float erreurMoyenne = 0f;

        // on compare les proportions
        for (ComboIso comboIso : combosInitiaux.keySet()) {
            float proportionDepart = (10 * combosInitiaux.get(comboIso) * comboIso.getNombreCombos())
                    / (totalValeursDepart * nTotalCombosReels);
            float proportionArrivee = combosEchantillon.get(comboIso) / nEchantillons;

            float pctErreur = Math.abs(proportionArrivee - proportionDepart) / proportionDepart;
            erreurMoyenne += pctErreur / combosInitiaux.size();

            assertTrue(erreurMoyenne < deltaErreur);

        }
    }
}
