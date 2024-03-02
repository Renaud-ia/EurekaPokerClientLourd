package analyzor.vue.table;

import analyzor.vue.reutilisables.PanneauFond;

import javax.swing.*;

public class PanneauHaut extends PanneauFond {
    public PanneauHaut() {
        JScrollPane scrollPane = new JScrollPane(this);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }
}
