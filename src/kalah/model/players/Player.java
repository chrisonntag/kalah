package kalah.model.players;

/**
 * Represents a player which can be either MACHINE, HUMAN or NONE to return
 * in case of an error.
 */
public enum Player {

  MACHINE,
  HUMAN,
  NONE;

  // TODO: Implement getMaxPitNum and getMinPitNum methods ?

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
    } else if (player == MACHINE){
      return HUMAN;
    } else {
      return NONE;
    }
  }
}