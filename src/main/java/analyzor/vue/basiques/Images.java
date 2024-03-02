package analyzor.vue.basiques;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import java.net.URL;

public class Images {
    public static Image icone;

    static  {
        try {
            URL fichier = Objects.requireNonNull(Images.class.getResource("/images/icone.png"));
            icone = new ImageIcon(fichier).getImage();
        }
        catch (Exception e) {
            icone = null;
        }
    }

    public static void main(String[] args) {
        System.out.println(Images.icone);
    }
}
