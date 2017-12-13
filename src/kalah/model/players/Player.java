package kalah.model.players;

public enum Player {

  // TODO: Implement getMaxPitNum and getMinPitNum methods ?
  MACHINE,
  HUMAN;

  public static Player getOpponent(Player player) {
    // TODO: Check location of method (static in Player?)
    // TODO: Check not valid parameters
    if (player == HUMAN) {
      return MACHINE;
    } else
      return HUMAN;
  }
}