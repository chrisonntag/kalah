package kalah.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import kalah.exceptions.IllegalMoveException;
import kalah.minimax.Node;

/**
 * {@inheritDoc}
 */
public class BoardImpl implements Board {

    private Player openingPlayer;
    private int level;
    private int currentPitsPerPlayer;
    private int currentSeedsPerPit;

    private Pit[][] pits;

    private int sourcePitOfLastMove = 0;
    private int targetPitOfLastMove = 0;

    /**
     * Instantiates a new {@link BoardImpl} object.
     *
     * @param openingPlayer The player who opens the game.
     * @param pitsCount The number of pits per player on the object.
     * @param seedsCount The number of seeds per player on the object.
     * @param level The desired difficulty level of the game which
     * simultaneously is the maximum depth of the calculated score tree of the
     * machine.
     */
    public BoardImpl(Player openingPlayer, int pitsCount,
        int seedsCount, int level) {
        this.openingPlayer = openingPlayer;
        this.level = level;
        this.currentPitsPerPlayer = pitsCount;
        this.currentSeedsPerPit = seedsCount;
        this.pits = new Pit[2][pitsCount + 1];

        populateBoard();
    }

    /**
     * Initializes the two dimensional {@link #pits} array with the desired
     * number of initial seeds per pit. The array itself doesn't hold the seeds
     * but rather objects of {@link Pit} in order to encapsulate all pit-related
     * information like ownership, the number of seeds and whether the pit is a
     * players store or not in one extra class.
     */
    private void populateBoard() {
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < getPitsPerPlayer() + 1; j++) {
                if (i == 0 && j == 0) {
                    // Create the store for machine player.
                    pits[i][j] = new Pit(Player.MACHINE, true);
                } else if (i == 1 && j == getPitsPerPlayer()) {
                    // Create the store for human player.
                    pits[i][j] = new Pit(Player.HUMAN, true);
                } else if (i == 0) {
                    // Create the pits for machine player.
                    pits[i][j] = new Pit(Player.MACHINE, getSeedsPerPit());
                } else {
                    // Create the pits for human player.
                    pits[i][j] = new Pit(Player.HUMAN, getSeedsPerPit());
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Player getOpeningPlayer() {
        return openingPlayer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Player next() {
        if (targetPitOfLastMove() > 0 && !isGameOver()) {
            Pit lastPit = getPit(targetPitOfLastMove());

            // Check if the next user must miss a turn.
            if (lastPit.isStore() && lastPit.getOwner() == getOpeningPlayer()) {
                return getOpeningPlayer();
            } else {
                return Player.getOpponent(getOpeningPlayer());
            }
        }

        return Player.NONE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Board move(int pit) {
        if (isGameOver()) {
            throw new IllegalMoveException("Error! The game is already over!");
        } else if (getOpeningPlayer() != Player.HUMAN) {
            throw new IllegalMoveException("Error! It's not your turn.");
        } else {
            if (pit <= (getPitsPerPlayer() + 1) * 2) {
                if (getSeeds(pit) == 0 || getPit(pit).isStore()
                    || getPit(pit).getOwner() != getOpeningPlayer()) {
                    throw new IllegalStateException();
                }
            } else {
                throw
                    new IllegalArgumentException(
                        "Error! The pit is not on the grid!");
            }
        }

        BoardImpl board = this.clone();
        board.sowSeeds(pit);

        board.openingPlayer = board.next();

        return board;
    }

    /**
     * Takes a pit number as an input and sows all seeds in it counter-clockwise
     * to the following pits on this board with one new seed per pit. If the
     * last pit in this process is empty, the pits opposite seeds plus the new
     *
     * @param pit The pit number the player takes.
     */
    private void sowSeeds(int pit) {
        int seeds = getSeeds(pit);
        getPit(pit).setSeeds(0);

        // Sow seeds counter-clockwise.
        int pitCount = seeds + pit;
        for (int i = pit + 1; i <= pitCount; i++) {
            int normalizedPitNum = normalizePitNum(i);
            Pit nextPit = getPit(normalizedPitNum);

            // Update pit if it's not the opponents store.
            if (nextPit.isStore() && nextPit.getOwner() != getOpeningPlayer()) {
                pitCount += 1;
            } else {
                nextPit.addSeeds(1);
            }
        }

        sourcePitOfLastMove = pit;
        targetPitOfLastMove = normalizePitNum(pitCount);

        // Check if catching is possible.
        Pit targetPit = getPit(targetPitOfLastMove());
        Pit opposingPit = getPit(getOpposingPitNum(targetPitOfLastMove()));
        if (!targetPit.isStore() && targetPit.getOwner() == getOpeningPlayer()
            && opposingPit.getSeeds() > 0 && targetPit.getSeeds() == 1) {
            // Update the seeds in the corresponding pits.
            int holdingSeeds = opposingPit.getSeeds() + targetPit.getSeeds();
            opposingPit.setSeeds(0);
            targetPit.setSeeds(0);

            // Decide which player gets the captured seeds.
            if (getOpeningPlayer() == Player.HUMAN) {
                getPit(getPitsPerPlayer() + 1).addSeeds(holdingSeeds);
            } else {
                getPit((getPitsPerPlayer() + 1) * 2).addSeeds(holdingSeeds);
            }
        }
    }

    /**
     * Converts numbers bigger than the maximum of pits into a
     * valid pit number bigger than zero.
     *
     * @param pit The pit number.
     * @return The normalized pit number.
     */
    private int normalizePitNum(int pit) {
        // Subtract one and add it again after the modulo
        // operation in order to start at number one.
        return (pit - 1) % ((getPitsPerPlayer() + 1) * 2) + 1;
    }

    /**
     * Calculates the pit number of the opposing pit for a given number.
     *
     * @param pit The pit number whose opposing pit number should be
     * calculated.
     * @return The opposing pit number.
     */
    private int getOpposingPitNum(int pit) {
        int maxPitNum = (getPitsPerPlayer() + 1) * 2;
        if (pit == maxPitNum) {
            return maxPitNum / 2;
        } else if (pit == maxPitNum / 2) {
            return maxPitNum;
        } else {
            return maxPitNum - pit;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Board machineMove() {
        Node root = constructTree(0);
        root.updateScore();
        return root.getMaxChild().getBoard();
    }

    /**
     * Recursively construct the tree.
     *
     * @param depth The current depth of the tree in this recursion.
     * @return The root node of the tree.
     */
    private Node constructTree(int depth) {
        if (depth == level || isGameOver()) {
            // Create a leave.
            return new Node(this.clone(), null,
                this.calcScore(depth), depth);
        } else {
            // Create a new sub-tree.
            Node root = new Node(this.clone(), this.calcScore(depth), depth);

            List<Board> possibleStates =
                getPossibleGameStates(getOpeningPlayer());
            for (Board board : possibleStates) {
                Node child = ((BoardImpl) board).constructTree(depth + 1);
                root.addChild(child);
            }
            return root;
        }
    }

    /**
     * Generates a list of all possible game states for a given player which can
     * be reached within one move on the current board.
     *
     * @param player The player for which to generate game states.
     * @return A list of possible game states.
     */
    private List<Board> getPossibleGameStates(Player player) {
        List<Board> gameStates = new ArrayList<>();
        for (int pitNum : getSourcePits(player)) {
            if (getPit(pitNum).getSeeds() != 0) {
                BoardImpl state = this.clone();
                state.sowSeeds(pitNum);

                state.openingPlayer = state.next();
                gameStates.add(state);
            }
        }

        return gameStates;
    }

    /**
     * Calculates a local score for this board in order to appraise the current
     * situation from the perspective of the machine. Following part scores are
     * being considered to evaluate the value of the local score.
     * calculated by following part-scores:
     * <ul>
     * <li>Score S, which compares the seeds in each players store.</li>
     * <li>Score C, which evaluates the number of opposing seeds which can be
     * captured in this move.</li>
     * <li>Score P, which counts the number of empty pits whose opposing
     * opposite pits contain at least twice the number of seeds as initial.</li>
     * <li>Score V, which evaluates if a move leads to a victory
     * immediately.</li>
     * </ul>
     *
     * @param depth The boards depth in the evaluation tree.
     * @return The calculated score.
     */
    private double calcScore(int depth) {
        // Evaluate seeds in the stores.
        int machineStore = (getPitsPerPlayer() + 1) * 2;
        int humanStore = getPitsPerPlayer() + 1;
        double scoreS = getSeeds(machineStore) - 1.5 * getSeeds(humanStore);

        // Evaluate number of opposing seeds which can be captured in this move.
        // the ones the machine can catch
        double catchableHumanSeeds = getCatchableSeeds(Player.MACHINE);

        // the ones the human can catch
        double catchableMachineSeeds = getCatchableSeeds(Player.HUMAN);
        double scoreC = catchableHumanSeeds - 1.5 * catchableMachineSeeds;

        // Evaluate the number of empty pits whose opposing opposite
        // pits contain at least twice the number of seeds as initial
        // per pit at the game start.
        double emptyHumanPits = getEmptyPits(Player.HUMAN);
        double emptyMachinePits = getEmptyPits(Player.MACHINE);
        double scoreP = emptyMachinePits - 1.5 * emptyHumanPits;

        // Evaluate if a move leads to a victory immediately.
        double scoreV;
        if (!isGameOver()) {
            scoreV = 0;
        } else {
            if (getWinner() == Player.NONE) {
                scoreV = 0;
            } else {
                if (getWinner() == Player.HUMAN) {
                    return -1.5 * (500.0 / depth);
                } else {
                    return 500.0 / depth;
                }
            }
        }

        return 3 * scoreS + scoreC + scoreP + scoreV;
    }

    /**
     * Get the number of empty pits whose opposing opposite
     * pits contain at least twice the number of seeds as initial
     * per pit at the game start.
     *
     * @param player The player where to look for.
     * @return The number of empty pits.
     */
    private int getEmptyPits(Player player) {
        int emptyPits = 0;
        for (int pitNum : getSourcePits(player)) {
            if (getPit(pitNum).getSeeds() == 0
                && getPit(getOpposingPitNum(pitNum)).getSeeds()
                >= 2 * getSeedsPerPit()) {
                emptyPits += 1;
            }
        }

        return emptyPits;
    }

    /**
     * Loops through all possible target pits of the player in order to find
     * catchable seeds of the opponent.
     *
     * @param player The player which to look for.
     * @return All catchable seeds.
     */
    private int getCatchableSeeds(Player player) {
        Set<Integer> targetPits = getPossibleTargetPits(player);
        int catchableSeeds = 0;

        // Loops through all possible target pits of the human in order to find
        // catchable seeds of the machine.
        for (int target : targetPits) {
            if (getPit(target).getSeeds() == 0
                && getPit(target).getOwner() == player
                && !getPit(target).isStore()) {
                catchableSeeds += getPit(getOpposingPitNum(target)).getSeeds();
            }
        }

        return catchableSeeds;
    }

    /**
     * Gets a list of game pit numbers a given player owns (no stores).
     *
     * @param player The player whose game pit numbers should be returned.
     * @return A list of game pits for a given player.
     */
    private List<Integer> getSourcePits(Player player) {
        List<Integer> playerPits = new ArrayList<>();
        int firstPitNum = 1;
        int lastPitNum = getPitsPerPlayer();
        if (player == Player.MACHINE) {
            firstPitNum = getPitsPerPlayer() + 2;
            lastPitNum = getPitsPerPlayer() * 2 + 1;
        }

        for (int i = firstPitNum; i <= lastPitNum; i++) {
            playerPits.add(i);
        }

        return playerPits;
    }

    /**
     * Returns a set of target pits which can be reached in this move. Due to
     * the property of a set not allowing duplicates the target pits will occur
     * only once even if they can be reached by several different source pits.
     * This ensures that pits won't be counted twice.
     *
     * @param player The player which can capture.
     * @return A set of pits
     */
    private Set<Integer> getPossibleTargetPits(Player player) {
        Set<Integer> targetPitNums = new HashSet<>();

        for (int pitNum : getSourcePits(player)) {
            Pit sourcePit = getPit(pitNum);
            if (sourcePit.getSeeds() != 0) {
                int targetPitNum = normalizePitNum(
                    pitNum + sourcePit.getSeeds());
                targetPitNums.add(targetPitNum);
            }
        }

        return targetPitNums;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLevel(int level) {
        if (level > 0) {
            this.level = level;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isGameOver() {
        boolean upperRowPitsEmpty = true;
        boolean lowerRowPitsEmpty = true;

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j <= getPitsPerPlayer(); j++) {
                // Check if a players row only contains empty pits.
                if (!pits[i][j].isStore() && pits[i][j].getSeeds() > 0) {
                    if (i == 0) {
                        upperRowPitsEmpty = false;
                    } else {
                        lowerRowPitsEmpty = false;
                    }
                    break; // Break at the first pit with more than one seed.
                }
            }
        }

        // The game ends if either the players or the opponents pits are empty.
        return upperRowPitsEmpty || lowerRowPitsEmpty;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Player getWinner() {
        if (getSeedsOfPlayer(Player.MACHINE) > getSeedsOfPlayer(Player.HUMAN)) {
            return Player.MACHINE;
        } else if (getSeedsOfPlayer(Player.MACHINE)
            < getSeedsOfPlayer(Player.HUMAN)) {
            return Player.HUMAN;
        } else {
            return Player.NONE;
        }
    }

    /**
     * Returns the pit object of a given pit number.
     *
     * @param pit The pit number.
     * @return The pit object on number {@code pit}.
     */
    private Pit getPit(int pit) {
        int pitsPerRow = getPitsPerPlayer() + 1;

        if (pit > pitsPerRow) {
            // One of the upper pits.
            return pits[0][(pit - pitsPerRow * 2) * (-1)];
        } else {
            // One of the lower pits.
            return pits[1][pit - 1];
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSeeds(int pit) {
        return getPit(pit).getSeeds();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int sourcePitOfLastMove() {
        return sourcePitOfLastMove;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int targetPitOfLastMove() {
        return targetPitOfLastMove;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPitsPerPlayer() {
        return currentPitsPerPlayer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSeedsPerPit() {
        return currentSeedsPerPit;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSeedsOfPlayer(Player player) {
        int playerRow = 0;
        if (player == Player.HUMAN) {
            playerRow = 1;
        }

        List<Integer> seeds = new ArrayList<>();
        for (Pit pit : pits[playerRow]) {
            seeds.add(pit.getSeeds());
        }

        // Sum up the list of seeds of each pit.
        return seeds.stream().reduce(0, (a, b) -> a + b);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BoardImpl clone() {
        Pit[][] clonedPits = new Pit[2][getPitsPerPlayer() + 1];

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < getPitsPerPlayer() + 1; j++) {
                clonedPits[i][j] = pits[i][j].clone();
            }
        }

        try {
            BoardImpl clonedBoard = (BoardImpl) super.clone();
            clonedBoard.pits = clonedPits;

            return clonedBoard;
        } catch (CloneNotSupportedException e) {
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        int maxDecimalNum = 1;
        for (int i = 0; i < pits.length; i++) {
            for (int j = 0; j < pits[i].length; j++) {
                if (pits[i][j].toString().length() > maxDecimalNum) {
                    maxDecimalNum = pits[i][j].toString().length();
                }
            }
        }
        StringBuilder upperRow = new StringBuilder();
        StringBuilder lowerRow = new StringBuilder();
        for (int i = 0; i < pits.length; i++) {
            for (int j = 0; j < pits[i].length; j++) {
                String pit = String.format("%" + maxDecimalNum + "d",
                    pits[i][j].getSeeds());
                if (i == 0) {
                    upperRow.append(pit);
                    if (j < getPitsPerPlayer()) {
                        upperRow.append(" ");
                    }
                } else {
                    lowerRow.append(pit);
                    if (j < getPitsPerPlayer()) {
                        lowerRow.append(" ");
                    }
                }
            }
        }
        int lowerRowMargin = pits[0][0].toString().length()
            + pits[1][0].toString().length();
        for (int i = 0; i < lowerRowMargin; i++) {
            lowerRow.insert(0, " ");
        }

        return upperRow.toString() + "\n" + lowerRow.toString();
    }

}
