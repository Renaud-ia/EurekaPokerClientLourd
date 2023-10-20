package analyzor.modele.extraction.ipoker;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class TestLectureXML {
    public static void main(String[] args) {
        String cheminFichier = "5165723056.xml";

        try {

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File(cheminFichier));
            //s'assurer de la bonne structure du fichier
            document.getDocumentElement().normalize();
            System.out.println("Root element: " + document.getDocumentElement().getNodeName());
            Element generalElement = (Element) document.getElementsByTagName("general").item(0);
            String nameTournament = generalElement.getElementsByTagName("tablename").item(0).getTextContent();
            System.out.println(nameTournament);

            NodeList gameElements = document.getElementsByTagName("game");
            for (int i = 0; i < gameElements.getLength(); i++) {
                Element handElement = (Element) gameElements.item(i);
                String gameCode = handElement.getAttribute("gamecode");
                System.out.println("Numéro gamecode : " + gameCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
