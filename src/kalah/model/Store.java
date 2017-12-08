package kalah.model;

import kalah.model.players.Player;

public class Store extends Pit {

  public Store(Player owner) {
    super(owner);
  }

  @Override
  public Pit clone() {
    return super.clone();
  }
}
