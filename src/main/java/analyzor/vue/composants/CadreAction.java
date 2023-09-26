package analyzor.vue.composants;

import analyzor.controleur.ControleurAccueil;
import analyzor.vue.donnees.InfosAction;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.*;
import java.util.Arrays;

public class CadreAction extends CadreClassique {
    private InfosAction infosAction;
    private ControleurAccueil controleur;
    public CadreAction(InfosAction infosAction, ControleurAccueil controleur) {
        super(infosAction.getPosition());
        this.infosAction = infosAction;
        this.controleur = controleur;

        String[] actions = infosAction.getActions();
        JList<String> comboList = new JList<>(actions);

        this.add(comboList);

        comboList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    String selectedOption = (String) comboList.getSelectedValue();
                    System.out.println("Option sélectionnée : " + selectedOption);
                    controleur.clickAction(selectedOption);
                }
            }
        });
    }
}
