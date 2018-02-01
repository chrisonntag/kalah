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

    /**
     * Used when there has been no game started yet.
     */
    public static final String NO_GAME_STARTED = "No game started yet.";

    /**
     * Tells the user that the game is already over.
     */
    public static final String GAME_OVER = "The game is already over!";

    /**
     * Displayed when it's not the users turn.
     */
    public static final String NOT_YOUR_TURN = "It's not your turn.";

    /**
     * Used when the pit is not on the grid.
     */
    public static final String NOT_ON_GRID = "The pit is not on the grid!";

    /**
     * Used when the move is invalid.
     */
    public static final String INVALID_MOVE = "Invalid move!";

    /**
     * Used as a help text for the 'New'-Button instantiating a new game.
     */
    public static final String HELP_NEW = "Start a new game with the values set"
        + " in the boxes besides.";

    /**
     * Used as a help text for the 'Switch'-Button which starts a new game,
     * but with switched players.
     */
    public static final String SWITCH_HELP = "Start a new game where the "
        + "second player of the last game begins.";

    /**
     * Used as a help text for the undo button.
     */
    public static final String UNDO_HELP = "Undo your last move.";

    /**
     * Used as a help text for the quit button.
     */
    public static final String QUIT_HELP = "Exit the game.";

    /**
     * Used as a help text for a combobox where users can choose the initial
     * number of seeds per pit.
     */
    public static final String SEEDS_HELP = "Initial seeds per pit.";

    /**
     * Used as a help text for a combobox where users can choose the initial
     * number pits per player.
     */
    public static final String PITS_HELP = "Initial pits per player.";

    /**
     * Used as a help text for a combobox where users can choose the difficulty
     * level.
     */
    public static final String LEVEL_HELP = "Difficulty level.";

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
