package kalah.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.Observable;
import java.util.Observer;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import kalah.model.Board;

public class BoardPanel extends JPanel {

    private static final Color LIGHT_WOOD = new Color(198, 159, 111);
    private static final Color DARK_WOOD = new Color(136, 97, 54);

    private Board gameBoard;

    private JPanel upperNumberPanel;
    private JPanel lowerNumberPanel;
    private JPanel pitsPanel;

    private int columns;

    public BoardPanel(Board board) {
        gameBoard = board;
        this.setLayout(new BorderLayout());

        columns = gameBoard.getPitsPerPlayer() + 2;

        upperNumberPanel = new JPanel();
        lowerNumberPanel = new JPanel();
        upperNumberPanel.setBackground(DARK_WOOD);
        lowerNumberPanel.setBackground(DARK_WOOD);
        upperNumberPanel.setLayout(new GridLayout(1, columns));
        lowerNumberPanel.setLayout(new GridLayout(1, columns));

        pitsPanel = new JPanel();
        pitsPanel.setLayout(new GridLayout(2, columns));
        pitsPanel.setBackground(LIGHT_WOOD);

        // Adding pits and stores.
        for (int i = (gameBoard.getPitsPerPlayer() + 1) * 2; i > gameBoard.getPitsPerPlayer() + 1; i--) {
            pitsPanel.add(new Pit(i));
            upperNumberPanel.add(new JLabel(Integer.toString(i), 0), BorderLayout.CENTER);

            if (i == columns) {
                upperNumberPanel.add(new JLabel(Integer.toString(i - 1), 0), BorderLayout.CENTER);
            }
        }
        pitsPanel.add(new JPanel());
        pitsPanel.add(new JPanel());
        // Adding pits and stores.
        lowerNumberPanel.add(new JLabel(Integer.toString((gameBoard.getPitsPerPlayer() + 1) * 2), 0),
            BorderLayout.CENTER);
        for (int i = 1; i <= gameBoard.getPitsPerPlayer() + 1; i++) {
            pitsPanel.add(new Pit(i));
            lowerNumberPanel.add(new JLabel(Integer.toString(i), 0),
                BorderLayout.CENTER);
        }

        // Adding number labels.
        //for (int i = 1; i <= board.getPitsPerPlayer();

        this.add(upperNumberPanel, BorderLayout.NORTH);
        this.add(pitsPanel, BorderLayout.CENTER);
        this.add(lowerNumberPanel,  BorderLayout.SOUTH);
        setVisible(true);
    }

    class Pit extends JPanel {

        private JLabel seedsLabel;
        private int pitNum;

        public Pit(int pitNum) {
            this.pitNum = pitNum;
            this.setLayout(new BorderLayout());
            this.setBorder(
                BorderFactory.createLineBorder(DARK_WOOD, 1));
            this.setOpaque(false);
            this.seedsLabel =
                new JLabel(Integer.toString(gameBoard.getSeeds(pitNum)),
                    SwingConstants.CENTER);
            this.add(seedsLabel, BorderLayout.CENTER);
        }

    }

}
