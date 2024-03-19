package analyzor.vue.table;

import analyzor.vue.basiques.Polices;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

class LabelSelectionnable extends JLabel implements MouseListener {
    private boolean selectionne = false;
    LabelSelectionnable(String nom) {
        super(nom);
        selectionne = false;
        this.setOpaque(false);
        this.addMouseListener(this);
        this.setForeground(Polices.BLANC_CASSE);
    }

    LabelSelectionnable() {
        super();
    }

    void selectionner() {
        selectionne = true;
        this.setFont(Polices.selectionne);
        this.setForeground(Polices.BLANC_CLAIR);
    }

    void deselectionner() {
        selectionne = false;
        this.setFont(Polices.standard);
        this.setForeground(Polices.BLANC_CASSE);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        if (selectionne) return;
        this.setFont(Polices.selectionne);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if (selectionne) return;
        this.setFont(Polices.standard);
    }
}
