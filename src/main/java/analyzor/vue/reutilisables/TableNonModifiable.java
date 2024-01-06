package analyzor.vue.reutilisables;

import javax.swing.table.DefaultTableModel;

public class TableNonModifiable extends DefaultTableModel {
    public TableNonModifiable() {
        super();
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        // Renvoie false pour désactiver l'édition des cellules
        return false;
    }

    @Override
    public void addRow(Object[] rowData) {
        super.addRow(rowData);
        // Vous pouvez également ajouter ici un appel à la méthode de notification appropriée si nécessaire
    }
}
