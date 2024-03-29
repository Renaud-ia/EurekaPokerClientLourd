package analyzor.modele.poker;

import java.util.*;

public class Deck {
    private final boolean[] cartes = new boolean[Carte.CARTE_MAX + 1];
    private final Random random = new Random();

    public Deck() {
        remplir();
    }
    public void remplir() {
        for (Carte carte : GenerateurCombos.toutesLesCartes) {
            this.cartes[carte.toInt()] = true;
        }
    }
    public void retirerCarte(Carte carteRetiree) {
        this.cartes[carteRetiree.toInt()] = false;
    }

    public void ajouterCartes(Carte[] cartesAjoutees) {
        for (int i = 0; i < cartesAjoutees.length; i++) {
            Carte carteAjoutee = cartesAjoutees[i];
            this.cartes[carteAjoutee.toInt()] = true;
        }
    }

    public void ajouterCarte(Carte carte) {
        cartes[carte.toInt()] = true;
    }

    public Carte carteRandom() {

        while (true) {
            int index = random.nextInt(Carte.CARTE_MAX + 1);
            if (cartes[index]) {

                cartes[index] = false;
                return new Carte(index);
            }
        }
    }

    public List<Carte> cartesRestantes() {
        List<Carte> cartesRestantes = new ArrayList<>();
        for (int i = 0; i < cartes.length; i++) {
            if (cartes[i]) cartesRestantes.add(new Carte(i));
        }
        return cartesRestantes;
    }
}
