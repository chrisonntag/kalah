package kalah.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Observable;
import java.util.Observer;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import kalah.model.Board;
import kalah.model.BoardMediator;

public class KalahFrame extends JFrame {

    private JPanel mainPanel = new JPanel();
    private JPanel controlPanel;
    private JPanel gamePanel;

    private BoardMediator boardMediator;

    public KalahFrame() {
        super("Kalah Game");
        boardMediator = new BoardMediator();

        controlPanel = new ControlPanel(boardMediator);
        gamePanel = new BoardPanel(boardMediator);

        boardMediator.addObserver((Observer) gamePanel);

        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(gamePanel, BorderLayout.CENTER);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);
        this.getContentPane().add(mainPanel);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(780, 480));
        pack(); // as small as possible
        setKeyCombinations();
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new KalahFrame();
            }
        });
    }

    private void setKeyCombinations() {
        this.getRootPane().getInputMap(2).put(KeyStroke.getKeyStroke("alt N"), "NEW");
        this.getRootPane().getActionMap().put("NEW", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                //boardMediator.newGame();
                System.out.println("KeyComb: NEW");
            }
        });
        this.getRootPane().getInputMap(2).put(KeyStroke.getKeyStroke("alt S"), "SWITCH");
        this.getRootPane().getActionMap().put("SWITCH", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                //boardMediator.switchPlayer();
                System.out.println("KeyComb: SWITCH");
            }
        });
        this.getRootPane().getInputMap(2).put(KeyStroke.getKeyStroke("alt U"), "UNDO");
        this.getRootPane().getActionMap().put("UNDO", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                //boardMediator.undo();
                System.out.println("KeyComb: UNDO");
            }
        });
        this.getRootPane().getInputMap(2).put(KeyStroke.getKeyStroke("alt Q"), "QUIT");
        this.getRootPane().getActionMap().put("QUIT", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                //this.closeWindow();
                System.out.println("KeyComb: QUIT");
            }
        });
    }

}
