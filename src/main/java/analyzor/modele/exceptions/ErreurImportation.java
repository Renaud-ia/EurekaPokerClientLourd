package analyzor.modele.exceptions;



public class ErreurImportation extends Exception {
    // on capture l'erreur au niveau du modèle et on indique le nombre d'erreurs au controleur

    public ErreurImportation(String message) {
        super(message);
    }
}
