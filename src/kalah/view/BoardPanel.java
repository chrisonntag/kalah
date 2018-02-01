package kalah.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Observable;
import java.util.Observer;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.plaf.ComponentUI;
import kalah.model.Board;
import kalah.model.BoardMediator;

/**
 * The {@link BoardPanel} class represents the part of the Kalah Application,
 * which holds all components regarding the actual "physical" board. Furthermore
 * it has a reference to a {@link BoardMediator} object which acts as a link
 * between the view/controller and the model.
 */
public class BoardPanel extends JPanel implements Observer {

    private static final Color LIGHT_WOOD = new Color(255, 206, 156);
    private static final Color DARK_WOOD = new Color(132, 74, 25);

    private BoardMediator boardMediator;
    private Board gameBoard;

    private JPanel upperNumberPanel;
    private JPanel lowerNumberPanel;
    private JPanel pitsPanel;

    private int numberOfColumns;

    /**
     * Instantiates a new {@link BoardPanel} derived from {@link JPanel}.
     *
     * @param boardMediator The link to the model part of the application.
     */
    public BoardPanel(BoardMediator boardMediator) {
        this.boardMediator = boardMediator;
        gameBoard = this.boardMediator.getGame();
        this.setLayout(new BorderLayout());
        renderBoard();

        setVisible(true);
    }

    /**
     * Adds panels containing the pits and rows to indicate the pit numbers
     * for each pits.
     */
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
                column.add(new Pit(i, true));

                // Set the upperRowNum as well as the lowerRowNum to the same.
                upperRowNum = Integer.toString(i);
                lowerRowNum = upperRowNum;
            } else {
                column.setLayout(new GridLayout(2, 1));
                column.add(new Pit(i, false));
                column.add(new Pit(getOpposingPitNum(i), false));

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

    /**
     * This class represents a single pit in the board panel.
     */
    class Pit extends JPanel {

        private JLabel seedsLabel;
        private int pitNum;
        private boolean isStore;
        private JPanel sourceMark;
        private JPanel targetMark;
        private final Color lightBlue = new Color(49, 156, 255);

        /**
         *
         * @param pitNum The pit number of the pit (1-indexed).
         * @param isStore Is {@code true} if the pit is a players store.
         */
        public Pit(int pitNum, boolean isStore) {
            this.pitNum = pitNum;
            this.isStore = isStore;
            this.setLayout(new BorderLayout());
            this.setBorder(BorderFactory.createLineBorder(DARK_WOOD, 1));
            this.setOpaque(false);
            this.seedsLabel
                = new JLabel(Integer.toString(gameBoard.getSeeds(pitNum)),
                SwingConstants.CENTER);

            int optimizedFontSize = (seedsLabel.getFont().getSize() - 4) * 2;
            Font pitFont = new Font(seedsLabel.getFont().getName(), Font.BOLD,
                optimizedFontSize);
            this.seedsLabel.setFont(pitFont);
            this.add(seedsLabel, BorderLayout.CENTER);

            renderMarkings();

            // Add a MouseListener only if this is a humans pit.
            if (isHumanPit() && !isStore()) {
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

        private void showSourceMark() {
            sourceMark.setVisible(true);
        }

        private void showTargetMark() {
            targetMark.setVisible(true);
        }

        private boolean isHumanPit() {
            return this.pitNum <= gameBoard.getPitsPerPlayer() + 1;
        }

        private boolean isStore() {
            return this.isStore;
        }

        /**
         * Adds source and target markings on the pits NORTH or SOUTH, or
         * EAST or WEST side depending on where on the board the pit is located.
         * The markings are then used to show the user where the last moves
         * source and target pits are located.
         */
        private void renderMarkings() {
            this.sourceMark = new JPanel();
            this.targetMark = new JPanel();
            this.sourceMark.setBackground(lightBlue);
            this.targetMark.setBackground(lightBlue);

            if (this.isStore()) {
                if (isHumanPit()) {
                    this.add(sourceMark, BorderLayout.SOUTH);
                    this.add(targetMark, BorderLayout.NORTH);
                } else {
                    this.add(sourceMark, BorderLayout.NORTH);
                    this.add(targetMark, BorderLayout.SOUTH);
                }
            } else {
                if (isHumanPit()) {
                    this.add(sourceMark, BorderLayout.WEST);
                    this.add(targetMark, BorderLayout.EAST);
                } else {
                    this.add(sourceMark, BorderLayout.EAST);
                    this.add(targetMark, BorderLayout.WEST);
                }
            }

            // Set them invisible by default - only show them after a move.
            this.sourceMark.setVisible(false);
            this.targetMark.setVisible(false);
        }

        /**
         * Calls the UI delegate's paint method, if the UI delegate
         * is non-<code>null</code>.  We pass the delegate a copy of the
         * <code>Graphics</code> object to protect the rest of the
         * paint code from irrevocable changes
         * (for example, <code>Graphics.translate</code>).
         * <p>
         * If you override this in a subclass you should not make permanent
         * changes to the passed in <code>Graphics</code>. For example, you
         * should not alter the clip <code>Rectangle</code> or modify the
         * transform. If you need to do these operations you may find it
         * easier to create a new <code>Graphics</code> from the passed in
         * <code>Graphics</code> and manipulate it. Further, if you do not
         * invoker super's implementation you must honor the opaque property,
         * that is
         * if this component is opaque, you must completely fill in the
         * background in a non-opaque color. If you do not honor the opaque
         * property you will likely see visual artifacts.
         * <p>
         * The passed in <code>Graphics</code> object might
         * have a transform other than the identify transform
         * installed on it.  In this case, you might get
         * unexpected results if you cumulatively apply
         * another transform.
         *
         * @param g the <code>Graphics</code> object to protect
         * @see #paint
         * @see ComponentUI
         */
        @Override
        protected void paintComponent(Graphics g) {
            // Call it's super paintComponent in order to paint the rest of
            // the pit.
            super.paintComponent(g);

            // Check if this pit is the source or target pit of the last move.
            if (gameBoard.sourcePitOfLastMove() == this.pitNum) {
                showSourceMark();
            } else if (gameBoard.targetPitOfLastMove() == this.pitNum) {
                showTargetMark();
            }
        }
    }

}
