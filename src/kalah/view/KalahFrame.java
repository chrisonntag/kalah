package kalah.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Observer;
import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import kalah.model.BoardMediator;

/**
 * This class represents the main frame for the Kalah GUI application, holding
 * all graphical elements and a reference to a {@link BoardMediator} object
 * which acts as a link between the view/controller and the model in order to
 * make Key Combinations work.
 */
public class KalahFrame extends JFrame {

    private static final int PREF_SIZE_WIDTH = 780;
    private static final int PREF_SIZE_HEIGHT = 400;
    private static final int MIN_SIZE_WIDTH = 625;
    private static final int MIN_SIZE_HEIGHT = 250;

    private ControlPanel controlPanel;
    private BoardMediator boardMediator;

    /**
     * Constructs a new {@link KalahFrame} object derived from {@link JFrame}.
     */
    public KalahFrame() {
        super("Kalah Game");
        boardMediator = new BoardMediator();

        controlPanel = new ControlPanel(boardMediator);
        JPanel gamePanel = new BoardPanel(boardMediator);

        boardMediator.addObserver((Observer) gamePanel);
        boardMediator.addObserver((Observer) controlPanel);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(gamePanel, BorderLayout.CENTER);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);
        getContentPane().add(mainPanel);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(PREF_SIZE_WIDTH, PREF_SIZE_HEIGHT));
        setMinimumSize(new Dimension(MIN_SIZE_WIDTH, MIN_SIZE_HEIGHT));

        // Size the Window to fit the preferred size and layouts of
        // its subcomponents.
        pack();

        initializeKeyCombinations();
        setVisible(true);
    }

    /**
     * This is the main method which instantiates the {@link KalahFrame}
     * extending a JFrame.
     *
     * @param args These are unused for this application.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new KalahFrame();
            }
        });
    }

    private void initializeKeyCombinations() {
        JRootPane root = this.getRootPane();
        InputMap inputMap = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

        inputMap.put(KeyStroke.getKeyStroke("alt N"), "NEW");
        inputMap.put(KeyStroke.getKeyStroke("alt S"), "SWITCH");
        inputMap.put(KeyStroke.getKeyStroke("alt U"), "UNDO");
        inputMap.put(KeyStroke.getKeyStroke("alt Q"), "QUIT");

        root.getActionMap().put("NEW", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                controlPanel.onNewButtonClicked();
            }
        });

        root.getActionMap().put("SWITCH", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                boardMediator.switchPlayers();
            }
        });

        root.getActionMap().put("UNDO", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                controlPanel.onUndoButtonClicked();
            }
        });

        root.getActionMap().put("QUIT", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                quitApplication();
            }
        });
    }

    /**
     * Exits the application as well as the JVM.
     */
    private void quitApplication() {
        System.exit(0);
    }

}
