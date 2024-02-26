package analyzor.modele.licence;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Base64;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * classe qui gère la connexion au serveur et les vérifications de licence
 */
class ConnexionServeur {
    private static final String urlServeur = "https://eureka-poker.fr";
    boolean connexionImpossible() {
        try {
            URI uri = new URI(urlServeur);

            // Récupération de l'URL à partir de l'objet URI
            URL serverUrl = uri.toURL();

            // Ouverture de la connexion HttpURLConnection
            HttpURLConnection connection = (HttpURLConnection) serverUrl.openConnection();

            // Configuration de la méthode de requête
            connection.setRequestMethod("HEAD"); // Utilisation de HEAD pour obtenir uniquement les en-têtes

            // Récupération du code de réponse HTTP
            int responseCode = connection.getResponseCode();

            // Vérification de la réponse
            boolean connexionOk = responseCode == HttpURLConnection.HTTP_OK;

            // Fermeture de la connexion
            connection.disconnect();

            return !connexionOk;
        }

        catch (Exception e) {
            return true;
        }
    }

    /**
     * vérifie si la licence est valide sans l'activer
     * @param cleLicence clé de licence
     * @return true si la licence est bonne
     */
    boolean verifierLicence(String cleLicence) {
        try {
            // URL de l'API REST à interroger
            String url = "https://eureka-poker.fr/wp-json/lmfwc/v2/licenses/" + cleLicence;
            JSONObject jsonLicence = getJSON(url);

            return jsonLicence.get("success").toString().equals("true");
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * essaye d'activer la licence
     * @param cleLicence la clé de licence à activer
     * @return 0 si c'est bon, 1 si problème de connexion, 2 si mauvaise clé, 3 si clé déjà activée
     */
    int activerLicence(String cleLicence) {
        try {
            String url = "https://eureka-poker.fr/wp-json/lmfwc/v2/licenses/activate/" + cleLicence;
            JSONObject jsonReponse = getJSON(url);

            if (jsonReponse.get("success").toString().equals("true")) {
                return 0;
            }
            else {
                // todo savoir quel est le problème
                return 2;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }

    /**
     * retourne le JSON correspondant à l'endpoint qu'on cherche
     * @param endPoint le endPoint à requêter pour obtenir le JSON
     * @return le JSON si requête bonne, sinon null
     */
    private JSONObject getJSON(String endPoint) throws IOException, URISyntaxException, ParseException {
        // Nom d'utilisateur et mot de passe pour l'authentification basique
        String username = "ck_de91e3b7182dc523ebbc32140d090bfb7f07773c";
        String password = "cs_27023ecfac7a732e3c04a91c3b9f7a231120e1cd";

        // Encodage des informations d'authentification basique
        String authString = username + ":" + password;
        String encodedAuthString = Base64.getEncoder().encodeToString(authString.getBytes());

        URI uri = new URI(endPoint);

        // Récupération de l'URL à partir de l'objet URI
        URL apiUrl = uri.toURL();

        // Ouverture de la connexion HttpURLConnection
        HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();

        // Configuration de la méthode de requête et des en-têtes d'authentification
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Basic " + encodedAuthString);
        int responsecode = connection.getResponseCode();


        // Lecture de la réponse de l'API
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        // Affichage de la réponse de l'API
        System.out.println("Réponse de l'API : " + response.toString());

        JSONObject jsonRecupere = null;

        if (responsecode == 200) {
            JSONParser parser = new JSONParser();
            jsonRecupere = (JSONObject) parser.parse(response.toString());
        }

        // Fermeture de la connexion
        connection.disconnect();

        return jsonRecupere;
    }

    public static void main(String[] args) {
        ConnexionServeur connexionServeur = new ConnexionServeur();
        System.out.println(connexionServeur.connexionImpossible());

        System.out.println(connexionServeur.verifierLicence("A5FE-1BF3-9FEE-5D48"));
    }
}
