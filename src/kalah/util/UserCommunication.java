package kalah.util;

import javax.swing.JOptionPane;

/**
 * Data class for the "Kalah" game. It holds messages to inform the user about
 * the game status and errors. All messages regarding user communication
 * should be in this class in order to have a central unit for changes or
 * translation.
 */
public final class UserCommunication {

    /**
     * Used if the machine must miss a turn.
     */
    public static final String MACHINE_MISS = "Machine must miss a turn.";

    /**
     * Used if the human must miss a turn.
     */
    public static final String HUMAN_MISS = "You must miss a turn.";

    /**
     * Used if the human wins the game.
     */
    public static final String WIN = "Congratulations! You won with %d seeds "
        + "versus %d seeds of the machine.%n";

    /**
     * Used if the human looses the game.
     */
    public static final String LOOSE = "Sorry! Machine wins with %d seeds "
        + "versus your %d.%n";

    /**
     * Used if nobody wins and the game ends with a tie.
     */
    public static final String STALEMATE = "Nobody wins. Tie with %d seeds "
        + "for each player.%n";

    /**
     * Used for informing the human player which pit the machine has chosen in
     * it's move.
     */
    public static final String MACHINE_MOVE = "Machine chose pit %d with seeds "
        + "reaching pit %d.%n";

    public static final String ALL_ARGS_NUM = "All arguments must be numbers.";
    public static final String WRONG_ARGS_TWO_NUM = "Wrong number of arguments: +2 numbers expected.";
    public static final String WRONG_ARGS_ONE_NUM = "Wrong number of arguments: +1 numbers expected.";
    public static final String POSITIVE_NOT_ZERO = "Parameter must be positive and not zero.";
    public static final String LEVEL_NUM = "The level must be a number.";

    public static final String ENTER_COMMAND = "You must enter a command.";
    public static final String NEED_LEVEL = "You must specify a level.";

    public static final String NO_GAME_STARTED = "No game started yet.";
    public static final String GAME_OVER = "The game is already over!";
    public static final String NO_VALID_COMMAND = "No valid command.";

    public static final String NOT_YOUR_TURN = "It's not your turn.";
    public static final String NOT_ON_GRID = "The pit is not on the grid!";
    public static final String INVALID_MOVE = "Invalid move!";
    public static final String LEVEL_RANGE = "Level must be between 1 and 7.";

    /**
     * Shows a {@link JOptionPane} with an Ok or Cancel dialog to the user.
     *
     * @param title The desired title of the pane.
     * @param message The message for the user to display.
     * @return The result of the user interaction.
     */
    public static int showConfirmDialog(String title, String message) {
        return JOptionPane.showConfirmDialog(null, message,
            title, JOptionPane.OK_CANCEL_OPTION);
    }

    /**
     * Shows a {@link JOptionPane} with an Yes or No dialog to the user.
     *
     * @param title The desired title of the pane.
     * @param message The message for the user to display.
     * @return The result of the user interaction.
     */
    public static int showYesNoDialog(String title, String message) {
        return JOptionPane.showConfirmDialog(null, message,
            title, JOptionPane.YES_NO_OPTION);
    }

    private UserCommunication() { }
}
