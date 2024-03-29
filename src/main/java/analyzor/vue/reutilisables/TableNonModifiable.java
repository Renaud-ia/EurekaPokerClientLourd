package analyzor.vue.reutilisables;

import javax.swing.table.DefaultTableModel;

public class TableNonModifiable extends DefaultTableModel {
    public TableNonModifiable() {
        super();
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        
        return false;
    }

    @Override
    public void addRow(Object[] rowData) {
        super.addRow(rowData);
        
    }
}
