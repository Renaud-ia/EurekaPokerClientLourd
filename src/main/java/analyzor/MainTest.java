package analyzor;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;

public class MainTest extends JFrame implements ActionListener {
    private final JButton startButton, stopButton;
    private JScrollPane scrollPane = new JScrollPane();
    private JList listBox = null;
    private DefaultListModel listModel = new DefaultListModel();
    private final JProgressBar progressBar;
    private mySwingWorker swingWorker;

    public MainTest() {
        super();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(new GridLayout(4, 1));
        startButton = makeButton("Start");
        stopButton = makeButton("Stop");
        stopButton.setEnabled(false);
        progressBar = makeProgressBar(0, 99);
        listBox = new JList(listModel);
        scrollPane.setViewportView(listBox);
        add(scrollPane);
        pack();
        setVisible(true);
    }

    private class mySwingWorker extends
            javax.swing.SwingWorker<ArrayList<Integer>, Integer> {
        private TacheTestWorker tacheTestWorker;
        @Override
        protected ArrayList<Integer> doInBackground() {
            if (javax.swing.SwingUtilities.isEventDispatchThread()) {
                System.out.println("javax.swing.SwingUtilities.isEventDispatchThread() returned true.");
            }
            ArrayList<Integer> list = new ArrayList<Integer>();
                try {
                    tacheTestWorker = new TacheTestWorker();
                    tacheTestWorker.faireQuelqueChose();
                } catch (Exception e) {
                    System.out.println("EXCEPTION APPARUE DANS DO IN BACKGROUND");
                    e.printStackTrace();
                    tacheTestWorker.interrompre();
                    return null;
                }
                if (isCancelled()) {
                    System.out.println("SwingWorker - isCancelled");
                    tacheTestWorker.interrompre();
                    return list;
                }
            return list;
        }

        @Override
        protected void process(java.util.List<Integer> progressList) {
            if (!javax.swing.SwingUtilities.isEventDispatchThread()) {
                System.out.println("javax.swing.SwingUtilities.isEventDispatchThread() + returned false.");
            }
            Integer percentComplete = progressList.get(progressList.size() - 1);
            progressBar.setValue(percentComplete.intValue());
        }

        @Override
        protected void done() {
            System.out.println("TACHE FINIE" + Thread.currentThread());
            if (!javax.swing.SwingUtilities.isEventDispatchThread()) {
                System.out.println("javax.swing.SwingUtilities.isEventDispatchThread() + returned false.");
            }
            try {
                ArrayList<Integer> results = get();
                for (Integer i : results) {
                    listModel.addElement(i.toString());
                }
            } catch (Exception e) {
                System.out.println("Caught an exception: " + e);
            }
            startButton();
        }

        public void annulerTache() {
            System.out.println(Thread.currentThread());
            System.out.println("INTERRUPTION DANS WORKER");
            tacheTestWorker.interrompre();
        }

    }

    private JButton makeButton(String caption) {
        JButton b = new JButton(caption);
        b.setActionCommand(caption);
        b.addActionListener(this);
        getContentPane().add(b);
        return b;
    }

    private JProgressBar makeProgressBar(int min, int max) {
        JProgressBar progressBar1 = new JProgressBar();
        progressBar1.setMinimum(min);
        progressBar1.setMaximum(max);
        progressBar1.setStringPainted(true);
        progressBar1.setBorderPainted(true);
        getContentPane().add(progressBar1);
        return progressBar1;
    }

    private void startButton() {
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if ("Start" == null ? e.getActionCommand() == null : "Start".equals(e
                .getActionCommand())) {
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            (swingWorker = new mySwingWorker()).execute();
        } else if ("Stop" == null ? e.getActionCommand() == null : "Stop".equals(e
                .getActionCommand())) {
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            swingWorker.annulerTache();
            swingWorker = null;
        }
    }

    public static void main(String[] args) {
        new MainTest();
    }
}