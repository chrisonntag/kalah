package kalah.model;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Observable;
import kalah.exceptions.IllegalMoveException;
import kalah.util.UserCommunication;

public class BoardMediator extends Observable {
    // TODO: class secret?
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

    public BoardMediator() {
        gameStack = new ArrayDeque<>();
        newGame();
    }

    public void newGame() {
        this.killMachineThread();

        Board game = new BoardImpl(openingPlayer, pitsPerPlayer, seedsPerPit,
            level);
        gameStack.clear();
        gameStack.push(game);
        setChanged();
        notifyObservers(game);
    }

    public void newGame(int pits, int seeds, int level) {
        this.gameStack.clear();
        this.pitsPerPlayer = pits;
        this.seedsPerPit = seeds;
        this.level = level;

        String message = String.format("Start a new game with %d pits and %d "
            + "seeds per Player?", pits, seeds);
        if (UserCommunication.showConfirmDialog(message, "New Game") == 0) {
            newGame();
        }
    }

    public void switchPlayers() {
        this.killMachineThread();
        if (gameStack.size() > 0) {
            openingPlayer = Player.getOpponent(openingPlayer);
            newGame();

            if (gameStack.peek().getOpeningPlayer() == Player.MACHINE) {
                machineMove();
            }
        } else {
            System.out.println(getError(300));
        }
    }

    public void doUndo() {
        this.killMachineThread();
        if (gameStack.size() > 0) {
            if (gameStack.pop().getOpeningPlayer() == Player.HUMAN) {
                gameStack.pop();
            }

            setChanged();
            notifyObservers(gameStack.peek());
        }
    }

    public void setLevel(int level) {
        this.killMachineThread();
        this.level = level;

        if (gameStack.size() > 0) {
            gameStack.peek().setLevel(level);
        }
    }

    public void humanMove(int pit) {
        if (gameStack.size() > 0) {
            try {
                Board game = gameStack.peek().move(pit);
                gameStack.push(game);
                setChanged();
                notifyObservers(game);
            } catch (IllegalMoveException
                | IllegalArgumentException e) {
                System.out.println(e.getMessage());
            } catch (IllegalStateException e) {
                System.out.println(getError(402));
            }

            if (gameStack.peek().isGameOver()) {
                getWinner();
            } else if (gameStack.peek().getOpeningPlayer() == Player.MACHINE) {
                machineMove();
                if (gameStack.peek().isGameOver()) {
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
        machineThread = new Thread() {
            @Override
            public void run() {
                delay(3000);

                while (gameStack.peek().getOpeningPlayer() == Player.MACHINE
                    && !gameStack.peek().isGameOver()) {
                    Board game = gameStack.peek().machineMove();
                    gameStack.push(game);
                    setChanged();
                    notifyObservers(game);

                    System.out.format(UserCommunication.MACHINE_MOVE,
                        game.sourcePitOfLastMove(), game.targetPitOfLastMove());

                    if (game.getOpeningPlayer() == Player.MACHINE) {
                        System.out.println(UserCommunication.HUMAN_MISS);
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
     * Checks the board for the winner of a game and prints an appropriate
     * message on the screen.
     */
    private void getWinner() {
        if (gameStack.peek().getWinner() == Player.NONE) {
            System.out.format(UserCommunication.STALEMATE,
                gameStack.peek().getSeedsOfPlayer(Player.HUMAN));
        } else if (gameStack.peek().getWinner() == Player.HUMAN) {
            System.out.format(UserCommunication.WIN,
                gameStack.peek().getSeedsOfPlayer(Player.HUMAN),
                gameStack.peek().getSeedsOfPlayer(Player.MACHINE));
        } else {
            System.out.format(UserCommunication.LOOSE,
                gameStack.peek().getSeedsOfPlayer(Player.MACHINE),
                gameStack.peek().getSeedsOfPlayer(Player.HUMAN));
        }
    }

    private void killMachineThread() {
        if (machineThread != null && machineThread.isAlive()) {
            machineThread.stop();
        }
    }

    public boolean isStackEmpty() {
        return gameStack.isEmpty();
    }

    public Board getGame() {
        return gameStack.peek();
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
