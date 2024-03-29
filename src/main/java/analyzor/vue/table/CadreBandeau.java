package analyzor.vue.table;

import analyzor.vue.basiques.CouleursDeBase;
import analyzor.vue.basiques.Polices;
import analyzor.vue.reutilisables.PanneauFonceArrondi;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;


public abstract class CadreBandeau extends PanneauFonceArrondi implements MouseListener {
    protected boolean selectionne;
    public final static int hauteur = 160;
    protected Color couleurFond;
    private JPanel panneauContenu;
    public CadreBandeau(String name) {
        super();
        BORDURE = 5;
        selectionne = false;
        this.setLayout(new FlowLayout(FlowLayout.LEFT));

        panneauContenu = new JPanel();
        panneauContenu.setLayout(new BoxLayout(panneauContenu, BoxLayout.Y_AXIS));
        panneauContenu.setOpaque(false);

        JLabel titre = new JLabel(name);
        titre.setFont(Polices.titre);
        titre.setForeground(Polices.BLANC_CLAIR);
        panneauContenu.add(titre);
        panneauContenu.add(Box.createRigidArea(new Dimension(0, 10)));

        super.add(panneauContenu);
        couleurFond = CouleursDeBase.PANNEAU_FONCE;
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(BORDURE, BORDURE, BORDURE, BORDURE));
        repaint();
    }

    @Override
    public Component add(Component component) {
        panneauContenu.add(component);
        repaint();
        return component;
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
        
        this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (selectionne) couleurFond = CouleursDeBase.PANNEAU_SELECTIONNE;
        else couleurFond = CouleursDeBase.PANNEAU_SURVOLE;
        repaint();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        
        this.setCursor(Cursor.getDefaultCursor());
        if (selectionne) couleurFond = CouleursDeBase.PANNEAU_SELECTIONNE;
        else couleurFond = CouleursDeBase.PANNEAU_FONCE;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            
            g2d.setColor(couleurFond);
            g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, TAILLE_ARRONDI, TAILLE_ARRONDI);

        } finally {
            g2d.dispose();
        }
    }
}
