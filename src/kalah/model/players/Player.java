package kalah.model.players;

public enum Player {
  MACHINE,
  HUMAN;

  public static Player getOpponent(Player player) {
    // TODO: Check location of method (static in Player?)
    if (player == HUMAN) {
      return MACHINE;
    } else
      return HUMAN;
  }
}