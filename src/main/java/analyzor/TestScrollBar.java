package test;

import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

public class TestScrollBar extends JFrame {

    public static void main(String[] args) {
        new TestScrollBar();
    }

    TestScrollBar() {
        JPanel panEvent = new JPanel(); //Panel où on place tous les événements
        JScrollPane scroll = new JScrollPane(panEvent, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBounds(0, 0, 930, 610);
        this.add(scroll);//ajout du panel qui contient tous les panel/event
//on a donc un panel avec dedans des panels/event et une scrollbar sur la gauche

//j'ajoute quelques boutons pour remplir le jpanel
        panEvent.setLayout(new GridLayout(50, 1));
        for (int i = 0; i < 50; i++) {
            panEvent.add(new JButton("Button n°" + i));
        }
        setSize(930, 610);//je redimensionne la fenetre
        setVisible(true);

    }

}