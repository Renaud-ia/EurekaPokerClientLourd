package analyzor.modele.licence;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Base64;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


class ConnexionServeur {
    private static final String urlServeur = "https://eureka-poker.fr";
    boolean connexionImpossible() {
        try {
            URI uri = new URI(urlServeur);


            URL serverUrl = uri.toURL();


            HttpURLConnection connection = (HttpURLConnection) serverUrl.openConnection();


            connection.setRequestMethod("HEAD");


            int responseCode = connection.getResponseCode();


            boolean connexionOk = responseCode == HttpURLConnection.HTTP_OK;


            connection.disconnect();

            return !connexionOk;
        }

        catch (Exception e) {
            return true;
        }
    }

    
    boolean verifierLicence(String cleLicence) {
        try {

            String url = "https://eureka-poker.fr/wp-json/lmfwc/v2/licenses/" + cleLicence;
            JSONObject jsonLicence = getJSON(url);

            return jsonLicence.get("success").toString().equals("true");
        }

        catch (Exception e) {
            return false;
        }
    }

    
    int activerLicence(String cleLicence) {
        try {
            String url = "https://eureka-poker.fr/wp-json/lmfwc/v2/licenses/activate/" + cleLicence;
            JSONObject jsonReponse = getJSON(url);

            if (jsonReponse.get("success").toString().equals("true")) {
                if (jsonReponse.containsKey("data")) {
                    JSONObject dataObject = (JSONObject) jsonReponse.get("data");
                    if (dataObject.containsKey("errors")) {
                        JSONObject dataErreurs = (JSONObject) dataObject.get("errors");
                        String nomErreur = ((JSONArray) dataErreurs.get("lmfwc_rest_data_error")).getFirst().toString();
                        if (nomErreur.contains("could not be found")) return 2;
                        else if (nomErreur.contains("reached maximum activation count")) return 3;
                        else return 1;

                    }
                }
                return 0;
            }
            else {
                return 1;
            }
        }
        catch (Exception e) {
            return 1;
        }
    }

    
    private JSONObject getJSON(String endPoint) throws IOException, URISyntaxException, ParseException {


        String username = "ck_de91e3b7182dc523ebbc32140d090bfb7f07773c";
        String password = "cs_27023ecfac7a732e3c04a91c3b9f7a231120e1cd";


        String authString = username + ":" + password;
        String encodedAuthString = Base64.getEncoder().encodeToString(authString.getBytes());

        URI uri = new URI(endPoint);


        URL apiUrl = uri.toURL();


        HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();


        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Basic " + encodedAuthString);
        int responsecode = connection.getResponseCode();



        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        JSONObject jsonRecupere = null;

        if (responsecode == 200) {
            JSONParser parser = new JSONParser();
            jsonRecupere = (JSONObject) parser.parse(response.toString());
        }


        connection.disconnect();

        return jsonRecupere;
    }
}
