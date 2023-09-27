package analyzor.modele.extraction;

public interface LecteurPartie {

    // retourne le nombre de mains enregistrées sinon null
    Integer sauvegarderPartie();

    boolean fichierEstValide();
}
