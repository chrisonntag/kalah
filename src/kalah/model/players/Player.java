package kalah.model.players;

public enum Player {

  // TODO: Implement getMaxPitNum and getMinPitNum methods ?
  MACHINE,
  HUMAN,
  NONE;

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