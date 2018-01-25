package kalah.view;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

public class ControlPanel extends JPanel {

    private JButton newButton = new JButton("New");
    private JButton switchButton = new JButton("Switch");
    private JButton undoButton = new JButton("Undo");
    private JButton quitButton = new JButton("Quit");
    private JComboBox<Integer> pitsCombo = new JComboBox<>();
    private JComboBox<Integer> seedsCombo = new JComboBox<>();
    private JComboBox<Integer> levelCombo = new JComboBox<>();

    public ControlPanel() {
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        this.add(newButton);
        this.add(switchButton);
        this.add(undoButton);
        this.add(quitButton);
        this.add(pitsCombo);
        this.add(seedsCombo);
        this.add(levelCombo);
    }

}
