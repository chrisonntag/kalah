package kalah.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Observable;
import java.util.Observer;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import kalah.model.Board;
import kalah.model.BoardMediator;

public class BoardPanel extends JPanel implements Observer {

    private static final Color LIGHT_WOOD = new Color(255, 206, 156);
    private static final Color DARK_WOOD = new Color(132, 74, 25);

    private BoardMediator boardMediator;
    private Board gameBoard;

    private JPanel upperNumberPanel;
    private JPanel lowerNumberPanel;
    private JPanel pitsPanel;

    private int columns;

    public BoardPanel(BoardMediator boardMediator) {
        this.boardMediator = boardMediator;
        gameBoard = this.boardMediator.getGame();
        this.setLayout(new BorderLayout());
        renderBoard();

        setVisible(true);
    }

    private void renderBoard() {
        this.removeAll();
        this.gameBoard = this.boardMediator.getGame();
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
            JLabel rowNumberLabel = new JLabel(Integer.toString(i), 0);
            rowNumberLabel.setForeground(LIGHT_WOOD);
            upperNumberPanel.add(rowNumberLabel, BorderLayout.CENTER);

            if (i == columns) {
                rowNumberLabel = new JLabel(Integer.toString(i - 1), 0);
                rowNumberLabel.setForeground(LIGHT_WOOD);
                upperNumberPanel.add(rowNumberLabel, BorderLayout.CENTER);
            }
        }
        pitsPanel.add(new JPanel());
        pitsPanel.add(new JPanel());

        // Adding pits and stores.
        JLabel rowNumberLabel = new JLabel(Integer.toString((gameBoard.getPitsPerPlayer() + 1) * 2), 0);
        rowNumberLabel.setForeground(LIGHT_WOOD);
        lowerNumberPanel.add(rowNumberLabel, BorderLayout.CENTER);
        for (int i = 1; i <= gameBoard.getPitsPerPlayer() + 1; i++) {
            pitsPanel.add(new Pit(i));
            rowNumberLabel = new JLabel(Integer.toString(i), 0);
            rowNumberLabel.setForeground(LIGHT_WOOD);
            lowerNumberPanel.add(rowNumberLabel, BorderLayout.CENTER);
        }

        // Adding number labels.
        //for (int i = 1; i <= board.getPitsPerPlayer();

        this.add(upperNumberPanel, BorderLayout.NORTH);
        this.add(pitsPanel, BorderLayout.CENTER);
        this.add(lowerNumberPanel,  BorderLayout.SOUTH);
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
        // Update local game board.
        if (data instanceof Board) {
            this.gameBoard = (Board) data;

            renderBoard();
            this.repaint();
            this.revalidate();
        }
    }

    class Pit extends JPanel {

        private JLabel seedsLabel;
        private int pitNum;

        public Pit(int pitNum) {
            this.pitNum = pitNum;
            this.setLayout(new BorderLayout());
            this.setBorder(BorderFactory.createLineBorder(DARK_WOOD, 1));
            this.setOpaque(false);
            this.seedsLabel =
                new JLabel(Integer.toString(gameBoard.getSeeds(pitNum)),
                    SwingConstants.CENTER);
            this.add(seedsLabel, BorderLayout.CENTER);

            // Add a MouseListener only if this is a humans pit.
            if (isHumanPit()) {
                this.seedsLabel.addMouseListener(new MouseAdapter() {
                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        boardMediator.humanMove(pitNum);
                    }

                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    public void mousePressed(MouseEvent e) {
                        setBorder(BorderFactory.createLineBorder(DARK_WOOD, 3));
                    }

                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    public void mouseReleased(MouseEvent e) {
                        setBorder(BorderFactory.createLineBorder(DARK_WOOD, 1));
                    }
                });
            }
        }

        private boolean isHumanPit() {
            return this.pitNum <= gameBoard.getPitsPerPlayer();
        }

    }

}
