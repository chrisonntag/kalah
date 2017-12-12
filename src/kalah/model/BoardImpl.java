package kalah.model;

import java.util.ArrayList;
import java.util.List;
import kalah.model.players.Player;
import kalah.exceptions.IllegalMoveException;

public class BoardImpl implements Board {

  /**
   * {@inheritDoc}
   */
  private int DEFAULT_PITS_PER_PLAYER = 6;

  /**
   * {@inheritDoc}
   */
  private int DEFAULT_SEEDS_PER_PIT = 3;

  private Player openingPlayer;
  private int level;

  private Pit[][] pits;

  private int sourcePitOfLastMove = 0;
  private int targetPitOfLastMove = 0;

  private BoardImpl(Player openingPlayer, int pitsCount, int seedsCount,
      int level, Pit[][] pits) {
    this.openingPlayer = openingPlayer;
    this.level = level;
    DEFAULT_PITS_PER_PLAYER = pitsCount;
    DEFAULT_SEEDS_PER_PIT = seedsCount;
    this.pits = pits;
  }

  public BoardImpl(Player openingPlayer, int pitsCount,
      int seedsCount, int level) {
    this.openingPlayer = openingPlayer;
    this.level = level;
    DEFAULT_PITS_PER_PLAYER = pitsCount;
    DEFAULT_SEEDS_PER_PIT = seedsCount;
    this.pits = new Pit[2][DEFAULT_PITS_PER_PLAYER + 1];

    populateBoard();
  }

  private void populateBoard() {
    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < DEFAULT_PITS_PER_PLAYER + 1; j++) {
        if (i == 0 && j == 0) {
          // Create the store for machine player.
          this.pits[i][j] = new Pit(Player.MACHINE, true);
        } else if (i == 1 && j == DEFAULT_PITS_PER_PLAYER) {
          // Create the store for human player.
          this.pits[i][j] = new Pit(Player.HUMAN, true);
        } else if (i == 0) {
          // Create the pits for machine player.
          this.pits[i][j] = new Pit(Player.MACHINE, DEFAULT_SEEDS_PER_PIT);
        } else {
          // Create the pits for human player.
          this.pits[i][j] = new Pit(Player.HUMAN, DEFAULT_SEEDS_PER_PIT);
        }
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Player getOpeningPlayer() {
    return this.openingPlayer;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Player next() {
    if (targetPitOfLastMove() > 0) {
      Pit lastPit = getPit(targetPitOfLastMove());

      // Check if the next user must miss a turn.
      if (lastPit.isStore() && lastPit.getOwner() == getOpeningPlayer()) {
        return getOpeningPlayer();
      } else {
        return Player.getOpponent(getOpeningPlayer());
      }
    }

    return Player.getOpponent(getOpeningPlayer());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Board move(int pit) {
    if (isGameOver()) {
      throw new IllegalMoveException("Error! The game is already over!");
    } else if (getOpeningPlayer() != Player.HUMAN) {
      throw new IllegalMoveException("Error! It's not your turn.");
    } else {
      try {
        if (getSeeds(pit) == 0 || getPit(pit).isStore()
            || getPit(pit).getOwner() != getOpeningPlayer()) {
          return null;
        }
      } catch (ArrayIndexOutOfBoundsException e) {
        throw new IllegalArgumentException(
            "Error! The pit is not on the grid!");
      }
    }

    BoardImpl board = this.clone();
    board.sowSeeds(pit);

    // TODO: Check role of opening and next player.
    board.openingPlayer = board.next();

    return board;
  }

  private void sowSeeds(int pit) {
    int seeds = getSeeds(pit);
    getPit(pit).setSeeds(0);

    // Sow seeds counter-clockwise.
    int pitCount = seeds + pit;
    for (int i = pit + 1; i <= pitCount; i++) {
      int normalizedPitNum = normalizePitNum(i);
      Pit nextPit = getPit(normalizedPitNum);

      // Update pit if it's not the opponents store.
      if (nextPit.isStore() && nextPit.getOwner() != getOpeningPlayer()) {
        pitCount += 1;
      } else {
        nextPit.addSeeds(1);
      }
    }

    this.sourcePitOfLastMove = pit;
    this.targetPitOfLastMove = normalizePitNum(pitCount);

    // Check if catching is possible and update the pits.
    Pit targetPit = getPit(targetPitOfLastMove());
    Pit opposingPit = getPit(
        (DEFAULT_PITS_PER_PLAYER + 1) * 2 - targetPitOfLastMove()
    );
    if (!targetPit.isStore()
        && opposingPit.getSeeds() > 0 && targetPit.getSeeds() == 1) {
      int holdingSeeds = opposingPit.getSeeds() + targetPit.getSeeds();
      opposingPit.setSeeds(0);
      targetPit.setSeeds(0);

      // Decide which player gets the captured seeds.
      if (getOpeningPlayer() == Player.HUMAN) {
        getPit(DEFAULT_PITS_PER_PLAYER + 1).addSeeds(holdingSeeds);
      } else {
        getPit((DEFAULT_PITS_PER_PLAYER + 1) * 2).addSeeds(holdingSeeds);
      }
    }
  }

  /**
   * Converts numbers bigger than the maximum of pits into a
   * valid pit number bigger than zero.
   *
   * @param pit The pit number.
   * @return The normalized pit number.
   */
  private int normalizePitNum(int pit) {
    // Subtract one and add it again after the modulo
    // operation in order to start at number one.
    return (pit - 1) % ((DEFAULT_PITS_PER_PLAYER + 1) * 2) + 1;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Board machineMove() {
    BoardImpl board = this.clone();
    board.openingPlayer = board.next();

    return board;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setLevel(int level) {
    if (level > 0) {
      this.level = level;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isGameOver() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Player getWinner() {
    return null;
  }

  /**
   * Returns the pit object of a given pit number.
   *
   * @param pit The pit number.
   * @return The pit object on number {@code pit}.
   */
  private Pit getPit(int pit) {
    int pitsPerRow = getPitsPerPlayer() + 1;

    if (pit > pitsPerRow) {
      // One of the upper pits.
      return this.pits[0][(pit - pitsPerRow * 2) * (-1)];
    } else {
      // One of the lower pits.
      return this.pits[1][pit - 1];
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getSeeds(int pit) {
    return getPit(pit).getSeeds();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int sourcePitOfLastMove() {
    return this.sourcePitOfLastMove;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int targetPitOfLastMove() {
    return this.targetPitOfLastMove;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getPitsPerPlayer() {
    return DEFAULT_PITS_PER_PLAYER;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getSeedsPerPit() {
    return DEFAULT_SEEDS_PER_PIT;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getSeedsOfPlayer(Player player) {
    // TODO: Magic number: Set HUMAN_ROW and MACHINE_ROW
    int playerRow = 0;
    if (player == Player.HUMAN) {
      playerRow = 1;
    }

    List<Integer> seeds = new ArrayList<>();
    for (Pit pit : this.pits[playerRow]) {
      seeds.add(pit.getSeeds());
    }

    // Sum up the list of seeds of each pit.
    return seeds.stream().reduce(0, (a, b) -> a + b);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public BoardImpl clone() {
    // TODO: use Arrays.copy
    Pit[][] newPits = new Pit[2][this.DEFAULT_PITS_PER_PLAYER + 1];

    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < this.DEFAULT_PITS_PER_PLAYER + 1; j++) {
        newPits[i][j] = this.pits[i][j].clone();
      }
    }

    return new BoardImpl(this.getOpeningPlayer(), this.DEFAULT_PITS_PER_PLAYER,
        this.DEFAULT_SEEDS_PER_PIT, this.level, newPits);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    // TODO: only one space between pits
    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < this.pits.length; i++) {
      for (int j = 0; j < this.pits[i].length; j++) {
        sb.append(this.pits[i][j]);
        sb.append(" \t");

        if (this.pits[i].length == j + 1 && this.pits.length - 1 != i) {
          sb.append("\n\t\t");
        }
      }
    }
    return sb.toString();
  }
}
