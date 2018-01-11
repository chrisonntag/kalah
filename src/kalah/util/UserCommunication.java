package kalah.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Data class for the "Kalah" game. It holds messages to inform the user about
 * the game status and errors. All messages regarding user communication
 * should be in this class in order to have a central unit for changes or
 * translation.
 */
public class UserCommunication {

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
   * Holds error codes and it's associated messages to inform the user in case
   * of an error.
   * Error codes are structured as follows:
   * <ul>
   *   <li>1xx: Semantically wrong commands.</li>
   *   <li>2xx: Syntactically wrong commands.</li>
   *   <li>3xx: Empty commands.</li>
   *   <li>4xx: Commands with a missing context.</li>
   *   <li>5xx: Incorrect commands according to the game rules or due to
   *   implementation limitations.</li>
   * </ul>
   */
  public static final Map<Integer, String> ERROR_MESSAGES =
      new HashMap<Integer, String>() {
        {
          put(100, "All arguments must be numbers.");
          put(101, "Wrong number of arguments: +2 numbers expected.");
          put(102, "All arguments must be numbers.");
          put(103, "Parameter must be positive and not zero.");
          put(104, "Wrong number of arguments: +1 integers expected.");
          put(105, "The level must be a number.");

          put(200, "You must enter a command.");
          put(201, "You must specify a level.");

          put(300, "No game started yet.");
          put(301, "The game is already over!");
          put(302, "No valid command.");

          // game logic
          put(400, "It's not your turn.");
          put(401, "The pit is not on the grid!");
          put(402, "Invalid move!");
          put(403, "Level must be between 1 and 7");
        }
      };

}
