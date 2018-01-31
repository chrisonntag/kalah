package kalah.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Observer;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
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
        boardMediator.addObserver((Observer) controlPanel);

        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(gamePanel, BorderLayout.CENTER);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);
        this.getContentPane().add(mainPanel);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(780, 400));
        setMinimumSize(new Dimension(625, 250));
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
                boardMediator.newGame();
            }
        });
        this.getRootPane().getInputMap(2).put(KeyStroke.getKeyStroke("alt S"), "SWITCH");
        this.getRootPane().getActionMap().put("SWITCH", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                boardMediator.switchPlayers();
            }
        });
        this.getRootPane().getInputMap(2).put(KeyStroke.getKeyStroke("alt U"), "UNDO");
        this.getRootPane().getActionMap().put("UNDO", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                boardMediator.doUndo();
            }
        });
        this.getRootPane().getInputMap(2).put(KeyStroke.getKeyStroke("alt Q"), "QUIT");
        this.getRootPane().getActionMap().put("QUIT", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                quitApplication();
            }
        });
    }

    private void quitApplication() {
        // TODO: Change to appropriate exit.
        System.exit(0);
    }

}
