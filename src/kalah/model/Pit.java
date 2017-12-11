package kalah.model;

import kalah.model.players.Player;

public class Pit implements Cloneable {

  private int seeds = 0;
  private Player owner;
  private boolean isStore;

  public Pit(Player owner, boolean isStore) {
    this.owner = owner;
    this.isStore = isStore;
  }

  public Pit(Player owner, int seeds) {
    this.owner = owner;
    this.isStore = isStore;
    this.seeds = seeds;
  }

  private Pit(Player owner, boolean isStore, int seeds) {
    this.owner = owner;
    this.isStore = isStore;
    this.seeds = seeds;
  }

  public int getSeeds() {
    return seeds;
  }

  public void setSeeds(int seeds) {
    this.seeds = seeds;
  }

  public void addSeeds(int seeds) {
    this.seeds += seeds;
  }

  public Player getOwner() {
    return owner;
  }

  public void setOwner(Player owner) {
    this.owner = owner;
  }

  public boolean isStore() {
    return this.isStore;
  }

  @Override
  public String toString() {
    return "" + this.seeds;
  }

  public Pit clone() {
    return new Pit(this.getOwner(), this.isStore, this.getSeeds());
  }
}
