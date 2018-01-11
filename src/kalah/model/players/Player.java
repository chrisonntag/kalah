package kalah.model.players;

/**
 * Represents a player in the game "Kalah".
 */
public enum Player {

  /**
   * The machine player who is fully controlled by the game.
   */
  MACHINE,

  /**
   * The human player who represents the user of this game.
   */
  HUMAN,

  /**
   * A dummy player to return in case of an error.
   */
  NONE;

  /**
   * Takes a given Player and returns it's opponent provided that there
   * are a maximum of two different players.
   *
   * @param player The player whose opponent should be determined.
   * @return The opponent. NONE if the given player is unknown.
   */
  public static Player getOpponent(Player player) {
    // TODO: Check location of method (static in Player?)
    if (player == HUMAN) {
      return MACHINE;
    } else if (player == MACHINE) {
      return HUMAN;
    } else {
      return NONE;
    }
  }
}