package kalah.model;

import kalah.model.players.Player;

/**
 * Represents a pit in the "Kalah" game. This class encapsulates all pit-related
 * information like ownership, the number of seeds and whether the pit is a
 * players store or not in one extra class.
 */
public class Pit implements Cloneable {

    private int seeds = 0;
    private Player owner;
    private boolean isStore;

    /**
     * Instantiates a new {@link Pit} object.
     *
     * @param owner The player this pit belongs to.
     * @param isStore {@code true} if this pit is a players store.
     */
    public Pit(Player owner, boolean isStore) {
        this.owner = owner;
        this.isStore = isStore;
    }

    /**
     * Instantiates a new {@link Pit} object.
     *
     * @param owner The player this pit belongs to.
     * @param seeds The number of seeds this pit holds initially.
     */
    public Pit(Player owner, int seeds) {
        this.owner = owner;
        this.seeds = seeds;
    }

    /**
     * Instantiates a new {@link Pit} object. Used for cloning only.
     *
     * @param owner The player this pit belongs to.
     * @param isStore {@code true} if this pit is a players store.
     * @param seeds The number of seeds this pit holds initially.
     */
    private Pit(Player owner, boolean isStore, int seeds) {
        this.owner = owner;
        this.isStore = isStore;
        this.seeds = seeds;
    }

    /**
     * Gets this pits seeds.
     *
     * @return This pits seeds.
     */
    public int getSeeds() {
        return seeds;
    }

    /**
     * Sets this pits seeds to a new value.
     *
     * @param seeds The number of seeds this should be updated to.
     */
    public void setSeeds(int seeds) {
        this.seeds = seeds;
    }

    /**
     * Adds a number of seeds to the current number of holding seeds in contrast
     * to {@link #setSeeds(int)}.
     *
     * @param seeds The number of seeds which should be added.
     */
    public void addSeeds(int seeds) {
        this.seeds += seeds;
    }

    /**
     * Gets the owner of this pit.
     *
     * @return The player this pit belongs to.
     */
    public Player getOwner() {
        return owner;
    }

    /**
     * Checks if this pit is a players store.
     *
     * @return {@code true} if this pit is a store. {@code false} otherwise.
     */
    public boolean isStore() {
        return isStore;
    }

    /**
     * Gets the string representation of this pit with the number of seeds
     * it currently contains.
     *
     * @return The string representation of this pit.
     */
    @Override
    public String toString() {
        return "" + seeds;
    }

    /**
     * Creates and returns a deep-copy of this pit.
     *
     * @return A clone.
     */
    public Pit clone() {
        return new Pit(this.getOwner(), this.isStore, this.getSeeds());
    }
}
