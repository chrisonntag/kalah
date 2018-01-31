package kalah.view;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;
import javax.swing.Timer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import kalah.model.Board;
import kalah.model.BoardMediator;
import kalah.util.UserCommunication;

public class ControlPanel extends JPanel implements Observer {

    private JButton newButton = new JButton("New");
    private JButton switchButton = new JButton("Switch");
    private JButton undoButton = new JButton("Undo");
    private JButton quitButton = new JButton("Quit");
    private JComboBox<Integer> pitsCombo;
    private JComboBox<Integer> seedsCombo;
    private JComboBox<Integer> levelCombo;
    private JLabel timeLabel = new JLabel();
    private Timer timer;

    private BoardMediator boardMediator;

    public ControlPanel(BoardMediator boardMediator) {
        this.boardMediator = boardMediator;
        // new BoxLayout(this, BoxLayout.X_AXIS)
        this.setLayout(new FlowLayout());

        pitsCombo = new JComboBox<Integer>(generateComboboxRange(1, 12));
        seedsCombo = new JComboBox<Integer>(generateComboboxRange(1, 12));
        levelCombo = new JComboBox<Integer>(generateComboboxRange(1, 7));
        pitsCombo.setSelectedItem(Board.DEFAULT_PITS_PER_PLAYER);
        seedsCombo.setSelectedItem(Board.DEFAULT_SEEDS_PER_PIT);
        levelCombo.setSelectedItem(BoardMediator.DEFAULT_LEVEL);

        this.undoButton.setEnabled(false);

        timer = createTimer(1000);
        timer.setInitialDelay(1000);

        this.add(newButton);
        this.add(switchButton);
        this.add(undoButton);
        this.add(quitButton);
        this.add(pitsCombo);
        this.add(seedsCombo);
        this.add(levelCombo);
        this.add(timeLabel);

        timer.start();
        initializeActionListeners();
    }

    private void initializeActionListeners() {
        newButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int pits = (Integer) pitsCombo.getSelectedItem();
                int seeds = (Integer) seedsCombo.getSelectedItem();
                int level = (Integer) levelCombo.getSelectedItem();

                boardMediator.newGame(pits, seeds, level);
            }
        });

        switchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boardMediator.switchPlayers();
            }
        });

        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                quitApplication();
            }
        });

        undoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!boardMediator.isStackEmpty()) {
                    boardMediator.doUndo();
                }
            }
        });

        levelCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int level = (Integer) levelCombo.getSelectedItem();
                boardMediator.setLevel(level);
            }
        });
    }

    private Timer createTimer(int delay) {
        return new Timer(1000, new ActionListener() {
            private int time = 1;

            @Override
            public void actionPerformed(ActionEvent e) {
                timeLabel.setText(Integer.toString(time) + "s");
                time += 1;
            }
        });
    }

    /**
     * This method is called whenever the observed object is changed. An
     * application calls an <tt>Observable</tt> object's
     * <code>notifyObservers</code> method to have all the object's
     * observers notified of the change.
     *
     * @param observable the observable object.
     * @param data an argument passed to the <code>notifyObservers</code>
     */
    @Override
    public void update(Observable observable, Object data) {
        // Reset undo button in case of a new game.
        undoButton.setEnabled(false);

        // Reset timer.
        timer.stop();
        timer = createTimer(1000);
        timer.setInitialDelay(1000);
        timer.start();

        // Check if undo is possible.
        if (!boardMediator.isStackEmpty()) {
            undoButton.setEnabled(true);
        }
    }

    /**
     * Generates an Integer array of a closed range of numbers between a given
     * start and end value.
     *
     * @param start The start of the range.
     * @param end The end of the range.
     * @return  An Integer array containing values between start and end.
     */
    private Integer[] generateComboboxRange(int start, int end) {
        if (start > end) {
            throw new IllegalArgumentException("The end of the Combobox range "
                + "must be greater-equal than it's end.");
        }

        int elements = end - start + 1;
        Integer[] range = new Integer[elements];
        for (int i = 0; i < elements; i++, start++) {
            range[i] = start;
        }

        return range;
    }

    private void quitApplication() {
        // TODO: Change to appropriate exit.
        System.exit(0);
    }

}
