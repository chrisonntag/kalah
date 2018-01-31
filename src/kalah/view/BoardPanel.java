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
import javax.swing.border.Border;
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

    private int numberOfColumns;

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
        numberOfColumns = gameBoard.getPitsPerPlayer() + 2;
        int maxPitNum = (gameBoard.getPitsPerPlayer() + 1) * 2;

        upperNumberPanel = new JPanel();
        lowerNumberPanel = new JPanel();
        upperNumberPanel.setBackground(DARK_WOOD);
        lowerNumberPanel.setBackground(DARK_WOOD);
        upperNumberPanel.setLayout(new GridLayout(1, numberOfColumns));
        lowerNumberPanel.setLayout(new GridLayout(1, numberOfColumns));

        pitsPanel = new JPanel();
        pitsPanel.setLayout(new GridLayout(1, numberOfColumns));
        pitsPanel.setBackground(LIGHT_WOOD);

        // Adding pits and stores.
        for (int i = maxPitNum; i >= gameBoard.getPitsPerPlayer() + 1; i--) {
            JPanel column = new JPanel();
            column.setOpaque(false);
            String upperRowNum;
            String lowerRowNum;

            if (i == maxPitNum || i == gameBoard.getPitsPerPlayer() + 1) {
                // If the pit is a store, take the whole columns space.
                column.setLayout(new GridLayout(1, 1));
                column.add(new Pit(i));

                // Set the upperRowNum as well as the lowerRowNum to the same.
                upperRowNum = Integer.toString(i);
                lowerRowNum = upperRowNum;
            } else {
                column.setLayout(new GridLayout(2, 1));
                column.add(new Pit(i));
                column.add(new Pit(getOpposingPitNum(i)));

                // Set both the upperRowNum and lowerRowNum
                upperRowNum = Integer.toString(i);
                lowerRowNum = Integer.toString(getOpposingPitNum(i));
            }
            pitsPanel.add(column);

            // Add row number labels.
            JLabel upperRowNumberLabel = new JLabel(upperRowNum, 0);
            upperRowNumberLabel.setForeground(LIGHT_WOOD);
            upperNumberPanel.add(upperRowNumberLabel, BorderLayout.CENTER);

            // Add row number labels.
            JLabel lowerRowNumberLabel = new JLabel(lowerRowNum, 0);
            lowerRowNumberLabel.setForeground(LIGHT_WOOD);
            lowerNumberPanel.add(lowerRowNumberLabel, BorderLayout.CENTER);
        }

        this.add(upperNumberPanel, BorderLayout.NORTH);
        this.add(pitsPanel, BorderLayout.CENTER);
        this.add(lowerNumberPanel,  BorderLayout.SOUTH);
    }

    /**
     * Calculates the pit number of the opposing pit for a given number.
     *
     * @param pit The pit number whose opposing pit number should be
     * calculated.
     * @return The opposing pit number.
     */
    // TODO: Duplicate in Board Implementation.
    private int getOpposingPitNum(int pit) {
        int maxPitNum = (gameBoard.getPitsPerPlayer() + 1) * 2;
        if (pit == maxPitNum) {
            return maxPitNum / 2;
        } else if (pit == maxPitNum / 2) {
            return maxPitNum;
        } else {
            return maxPitNum - pit;
        }
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
