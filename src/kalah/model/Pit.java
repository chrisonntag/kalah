package kalah.model;

import kalah.model.players.Player;

public class Pit implements Cloneable {

  private int seeds = 0;
  private Player owner;

  public Pit(Player owner) {
    this.owner = owner;
  }

  public Pit(int seeds, Player owner) {
    this.seeds = seeds;
    this.owner = owner;
  }

  public int getSeeds() {
    return seeds;
  }

  public void setSeeds(int seeds) {
    this.seeds = seeds;
  }

  public Player getOwner() {
    return owner;
  }

  public void setOwner(Player owner) {
    this.owner = owner;
  }

  @Override
  public String toString() {
    return "" + this.seeds;
  }

  public Pit clone() {
    return new Pit(this.getSeeds(), this.getOwner());
  }
}
