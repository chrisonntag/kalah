package kalah.view;

import java.awt.Component;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import kalah.model.Board;
import kalah.model.BoardMediator;

public class ControlPanel extends JPanel {

    private JButton newButton = new JButton("New");
    private JButton switchButton = new JButton("Switch");
    private JButton undoButton = new JButton("Undo");
    private JButton quitButton = new JButton("Quit");
    private JComboBox<Integer> pitsCombo;
    private JComboBox<Integer> seedsCombo;
    private JComboBox<Integer> levelCombo;

    private BoardMediator boardMediator;

    public ControlPanel(BoardMediator boardMediator) {
        this.boardMediator = boardMediator;
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        pitsCombo = new JComboBox<Integer>(generateComboboxRange(1, 12));
        seedsCombo = new JComboBox<Integer>(generateComboboxRange(1, 12));
        levelCombo = new JComboBox<Integer>(generateComboboxRange(1, 7));
        pitsCombo.setSelectedItem(Board.DEFAULT_PITS_PER_PLAYER);
        seedsCombo.setSelectedItem(Board.DEFAULT_SEEDS_PER_PIT);
        levelCombo.setSelectedItem(BoardMediator.DEFAULT_LEVEL);

        this.add(newButton);
        this.add(switchButton);
        this.add(undoButton);
        this.add(quitButton);
        this.add(pitsCombo);
        this.add(seedsCombo);
        this.add(levelCombo);
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

    private int showConfirmDialog(String message, String title) {
        return JOptionPane.showConfirmDialog((Component) null, message, title, 0);
    }

    private void newGame() {
        int pits = (Integer) pitsCombo.getSelectedItem();
        int seeds = (Integer) seedsCombo.getSelectedItem();

        String message = String.format("Start a new game with %d pits and %d seeds per Player?", pits, seeds);
        if (showConfirmDialog(message, "New Game") == 0) {
            boardMediator.newGame();
        }
    }

}
