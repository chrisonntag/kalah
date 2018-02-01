package kalah.view;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Observable;
import kalah.exceptions.IllegalMoveException;
import kalah.model.Board;
import kalah.model.BoardImpl;
import kalah.model.Player;
import kalah.util.UserCommunication;

/**
 * This class acts as the connecting link between the model part and the
 * view part of the application. It holds a {@link Board} object and updates
 * all registered Observers if the state of this {@link Board} changes.
 */
public class BoardMediator extends Observable {
    public static final int DEFAULT_LEVEL = 3;
    private int level = DEFAULT_LEVEL;
    private int seedsPerPit = Board.DEFAULT_SEEDS_PER_PIT;
    private int pitsPerPlayer = Board.DEFAULT_PITS_PER_PLAYER;
    private Player openingPlayer = Player.HUMAN;

    /**
     * Don't need a thread-safe deque here --> Use ArrayDeque.
     */
    private Deque<Board> gameStack;
    private Thread machineThread = new Thread();

    /**
     * Instantiates a new {@link BoardMediator}.
     */
    public BoardMediator() {
        gameStack = new ArrayDeque<>();
        newGame();
    }

    /**
     * Clears the whole {@link #gameStack} and instantiates a new {@link Board}
     * with set attributes.
     */
    public void newGame() {
        this.killMachineThread();

        Board game = new BoardImpl(openingPlayer, pitsPerPlayer, seedsPerPit,
            level);
        gameStack.clear();
        gameStack.push(game);
        setChanged();
        notifyObservers(game);
    }

    /**
     * Starts a new game with given parameters.
     *
     * @param pits The desired pits per player for the new game.
     * @param seeds The desired seeds per pit for the new game.
     * @param level The desired level for the new game.
     */
    public void newGame(int pits, int seeds, int level) {
        this.gameStack.clear();
        this.pitsPerPlayer = pits;
        this.seedsPerPit = seeds;
        this.level = level;

        String message = String.format("Start a new game with %d pits and %d "
            + "seeds per Player?", pits, seeds);
        if (UserCommunication.showYesNoDialog("New Game", message) == 0) {
            newGame();
        }
    }

    /**
     * Starts a new game with the player who started as a second player in the
     * last game.
     */
    public void switchPlayers() {
        this.killMachineThread();
        if (gameStack.size() > 0) {
            openingPlayer = Player.getOpponent(openingPlayer);
            newGame();

            if (gameStack.peek().getOpeningPlayer() == Player.MACHINE) {
                machineMove();
            }
        } else {
            UserCommunication.showConfirmDialog("Error!",
                UserCommunication.NO_GAME_STARTED);
        }
    }

    /**
     * Removes all {@link Board} objects from the {@link #gameStack} until the
     * last human move.
     */
    public void doUndo() {
        this.killMachineThread();
        if (gameStack.size() > 1) {
            gameStack.pop();
            while (gameStack.peek().getOpeningPlayer() == Player.MACHINE) {
                gameStack.pop();
            }

            setChanged();
            notifyObservers(gameStack.peek());
        }
    }

    /**
     * Sets a new level for the current and all following {@link Board} objects
     * on the {@link #gameStack}.
     *
     * @param level The level to be set.
     */
    public void setLevel(int level) {
        this.killMachineThread();
        this.level = level;

        if (gameStack.size() > 0) {
            gameStack.peek().setLevel(level);
        }
    }

    /**
     * Executes a human move on the first element of the {@link #gameStack}.
     *
     * @param pit The number of the human pit whose contained seeds will be
     * sowed counter-clockwise.
     */
    public void humanMove(int pit) {
        if (gameStack.size() > 0) {
            try {
                Board game = gameStack.peek().move(pit);
                gameStack.push(game);
                setChanged();
                notifyObservers(game);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                return;
            } catch (IllegalMoveException e) {
                UserCommunication.showConfirmDialog("Error!",
                    e.getMessage());
                return;
            } catch (IllegalStateException e) {
                UserCommunication.showConfirmDialog("Error!",
                    UserCommunication.INVALID_MOVE);
                return;
            }

            if (gameStack.peek().isGameOver()) {
                getWinner();
            } else if (gameStack.peek().getOpeningPlayer() == Player.MACHINE) {
                machineMove();
                if (gameStack.peek().isGameOver()) {
                    getWinner();
                }
            } else {
                UserCommunication.showConfirmDialog("Well done!",
                    UserCommunication.MACHINE_MISS);
            }
        } else {
            UserCommunication.showConfirmDialog("Error!",
                UserCommunication.NO_GAME_STARTED);
        }
    }

    /**
     * Executes a machine move on the board and displays appropriate messages
     * which pit the machine took and if the other player must miss a turn.
     */
    public void machineMove() {
        machineThread = new Thread() {
            @Override
            public void run() {
                while (gameStack.peek().getOpeningPlayer() == Player.MACHINE
                    && !gameStack.peek().isGameOver()) {
                    // Measure how long the model algorithm takes.
                    long begin = System.currentTimeMillis();
                    Board game = gameStack.peek().machineMove();
                    long end = System.currentTimeMillis();

                    long modelTime = 3000 - (end - begin);
                    if (modelTime > 0) {
                        // Add a random delay between one and three seconds.
                        int delay = (int) (Math.random() * (3000 - 1000))
                            + 1000;
                        delay(delay);
                    }

                    gameStack.push(game);
                    setChanged();
                    notifyObservers(game);

                    String message
                        = String.format(UserCommunication.MACHINE_MOVE,
                        game.sourcePitOfLastMove(), game.targetPitOfLastMove());

                    UserCommunication.showConfirmDialog("Machines Move",
                        message);

                    if (game.getOpeningPlayer() == Player.MACHINE) {
                        UserCommunication.showConfirmDialog("Sorry!",
                            UserCommunication.HUMAN_MISS);
                    }
                }
            }

            private void delay(long millis) {
                try {
                    sleep(millis);
                } catch (InterruptedException ex) {
                    currentThread().interrupt();
                }
            }
        };

        machineThread.start();
    }

    /**
     * Checks the board for the winner of a game and displays an appropriate
     * message to the user.
     */
    private void getWinner() {
        if (gameStack.peek().getWinner() == Player.NONE) {
            String message = String.format(UserCommunication.STALEMATE,
                gameStack.peek().getSeedsOfPlayer(Player.HUMAN));

            UserCommunication.showConfirmDialog("Sorry!", message);
        } else if (gameStack.peek().getWinner() == Player.HUMAN) {
            String message = String.format(UserCommunication.WIN,
                gameStack.peek().getSeedsOfPlayer(Player.HUMAN),
                gameStack.peek().getSeedsOfPlayer(Player.MACHINE));

            UserCommunication.showConfirmDialog("Well done!", message);
        } else {
            String message = String.format(UserCommunication.LOOSE,
                gameStack.peek().getSeedsOfPlayer(Player.MACHINE),
                gameStack.peek().getSeedsOfPlayer(Player.HUMAN));

            UserCommunication.showConfirmDialog("Sorry!", message);
        }
    }

    /**
     * Kills the {@link #machineThread} which may be interrupted. This method
     * uses the {@link Thread#stop()} method although it is deprecated because
     * it would be to time-consuming to adapt present code to implement a
     * correct thread shutdown routine.
     */
    private void killMachineThread() {
        if (machineThread != null && machineThread.isAlive()) {
            machineThread.stop();
        }
    }

    /**
     * Checks if the {@link #gameStack} contains more than it's initial
     * game {@link Board}.
     *
     * @return A boolean value if the {@link #gameStack} contains more than one.
     */
    public boolean isUndoAllowed() {
        return gameStack.size() > 1;
    }

    /**
     * Gets the first {@link Board} element on the {@link #gameStack}.
     *
     * @return The latest {@link Board} object.
     */
    public Board getGame() {
        return gameStack.peek();
    }

}
