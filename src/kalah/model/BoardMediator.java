package kalah.model;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Observable;
import kalah.exceptions.IllegalMoveException;
import kalah.util.UserCommunication;

public class BoardMediator extends Observable {

    // TODO: Klassengeheimnis?
    public static final int DEFAULT_LEVEL = 3;
    private int level = DEFAULT_LEVEL;
    private int seedsPerPit = Board.DEFAULT_SEEDS_PER_PIT;
    private int pitsPerPlayer = Board.DEFAULT_PITS_PER_PLAYER;
    private Player openingPlayer = Player.HUMAN;
    private Board game = null;

    private Deque<Board> undoStack;
    private Thread machineThread;

    public BoardMediator() {
        newGame();
        undoStack = new ArrayDeque<>();
    }

    public void newGame() {
        game = new BoardImpl(openingPlayer, pitsPerPlayer, seedsPerPit, level);
        setChanged();
        notifyObservers(game);
    }

    public void newGame(int pits, int seeds, int level) {
        this.pitsPerPlayer = pits;
        this.seedsPerPit = seeds;
        this.level = level;
        newGame();
    }

    public void switchPlayers() {
        if (game != null) {
            openingPlayer = Player.getOpponent(openingPlayer);
            newGame();

            if (game.getOpeningPlayer() == Player.MACHINE) {
                machineMove();
            }
        } else {
            System.out.println(getError(300));
        }
    }

    public void doUndo() {

    }

    public void humanMove(int pit) {
        if (game != null) {
            try {
                game = game.move(pit);
                setChanged();
                notifyObservers(game);

            } catch (IllegalMoveException
                | IllegalArgumentException e) {
                System.out.println(e.getMessage());
            } catch (IllegalStateException e) {
                System.out.println(getError(402));
            }

            if (game.isGameOver()) {
                getWinner();
            } else if (game.getOpeningPlayer() == Player.MACHINE) {
                machineMove();
                if (game.isGameOver()) {
                    getWinner();
                }
            } else {
                System.out.println(UserCommunication.MACHINE_MISS);
            }
        } else {
            System.out.println(getError(300));
        }
    }

    public void machineMove() {
        game = game.machineMove();
        setChanged();
        notifyObservers(game);

        System.out.format(UserCommunication.MACHINE_MOVE,
            game.sourcePitOfLastMove(), game.targetPitOfLastMove());

        while (game.getOpeningPlayer() == Player.MACHINE && !game.isGameOver()) {
            System.out.println(UserCommunication.HUMAN_MISS);
            machineMove();
        }
    }

    /**
     * Checks the board for the winner of a game and prints an appropriate
     * message on the screen.
     */
    private void getWinner() {
        if (game.getWinner() == Player.NONE) {
            System.out.format(UserCommunication.STALEMATE,
                game.getSeedsOfPlayer(Player.HUMAN));
        } else if (game.getWinner() == Player.HUMAN) {
            System.out.format(UserCommunication.WIN,
                game.getSeedsOfPlayer(Player.HUMAN),
                game.getSeedsOfPlayer(Player.MACHINE));
        } else {
            System.out.format(UserCommunication.LOOSE,
                game.getSeedsOfPlayer(Player.MACHINE),
                game.getSeedsOfPlayer(Player.HUMAN));
        }
    }

    public Board getGame() {
        return game;
    }

    /**
     * Fetches the belonging error message to a given code and returns it as a
     * string in order to work in text based applications as well as be reusable
     * in applications with a graphical user interface.
     *
     * @param code The error code as stated in {@link UserCommunication}.
     * @return The error message.
     */
    private static String getError(int code) {
        return "Error! " + UserCommunication.ERROR_MESSAGES.get(code);
    }

}
