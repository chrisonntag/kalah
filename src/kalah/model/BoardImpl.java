package kalah.model;

import java.util.ArrayList;
import java.util.List;
import kalah.model.players.Player;
import kalah.exceptions.IllegalMoveException;

public class BoardImpl implements Board {

  /**
   * The number of pits per player in the classical Kalah game.
   */
  private int DEFAULT_PITS_PER_PLAYER = 6;

  /**
   * The initial number of seeds in each pit.
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
   * Gets the player who should open or already has opened the game by the
   * initial move.
   *
   * @return The player who makes the initial move.
   */
  @Override
  public Player getOpeningPlayer() {
    return this.openingPlayer;
  }

  /**
   * Gets the player who owns the next game turn.
   *
   * @return The player who is allowed to make the next turn.
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
   * Executes a human move. This method does not change the state of this
   * instance, which is treated here as immutable. Instead, a new board/game
   * is returned, which is a copy of {@code this} with the move executed.
   *
   * @param pit The number of the human pit whose contained seeds will be sowed
   * counter-clockwise.
   * @return A new board with the move executed. If the move is not valid, i.e.,
   * the pit is empty, then {@code null} will be returned.
   * @throws IllegalMoveException If the game is already over, or it is not the
   * human's turn.
   * @throws IllegalArgumentException If the provided parameter is invalid,
   * e.g., the defined pit is not on the grid.
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
   * Executes a machine move. This method does not change the state of this
   * instance, which is treated here as immutable. Instead, a new board/game
   * is returned, which is a copy of {@code this} with the move executed.
   *
   * @return A new board with the move executed.
   * @throws IllegalMoveException If the game is already over, or it is not the
   * machine's turn.
   */
  @Override
  public Board machineMove() {
    BoardImpl board = this.clone();
    board.openingPlayer = board.next();

    return board;
  }

  /**
   * Sets the skill level of the machine.
   *
   * @param level The skill as a number, must be at least 1.
   */
  @Override
  public void setLevel(int level) {
    if (level > 0) {
      this.level = level;
    }
  }

  /**
   * Checks if the game is over. Either one player has won or there is a tie,
   * i.e., both players gained the same number of seeds.
   *
   * @return {@code true} if and only if the game is over.
   */
  @Override
  public boolean isGameOver() {
    return false;
  }

  /**
   * Checks if the game state is won. Should only be called if
   * {@link #isGameOver()} returns {@code true}.
   *
   * A game is won by a player if her own or the opponents pits are all empty,
   * and the number of seeds in the own store plus the seeds in the own pits
   * is more than the sum of seeds in the opponents pits and store.
   *
   * @return The winner or nobody in case of a tie.
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
   * Gets the number of seeds of the specified pit index {@code pit}.
   *
   * @param pit The number of the pit.
   * @return The pit's content.
   */
  @Override
  public int getSeeds(int pit) {
    return getPit(pit).getSeeds();
  }

  /**
   * Gets the number of the source pit of the last executed move. A number of
   * one of the stores is not possible.
   *
   * @return The ordering number of the last move's source pit.
   */
  @Override
  public int sourcePitOfLastMove() {
    return this.sourcePitOfLastMove;
  }

  /**
   * Gets the number of the target pit of the last executed move. The number
   * of the move opponent's stores is not possible.
   *
   * @return The ordering number of the last move's target pit.
   */
  @Override
  public int targetPitOfLastMove() {
    return this.targetPitOfLastMove;
  }

  /**
   * Gets the number of pits per player in this game.
   *
   * @return The number of pits per player.
   */
  @Override
  public int getPitsPerPlayer() {
    return DEFAULT_PITS_PER_PLAYER;
  }

  /**
   * Gets the initial number of seeds in each pit of the players.
   *
   * @return The initial number of seeds per pit.
   */
  @Override
  public int getSeedsPerPit() {
    return DEFAULT_SEEDS_PER_PIT;
  }

  /**
   * Gets the current number of the seeds of the player {@code player}. This
   * is the sum of the seeds in her pits and in her store.
   *
   * @param player The player for which to sum up her seeds.
   * @return The sum of the pits per player.
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
   * Creates and returns a deep copy of this board.
   *
   * @return A clone.
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
   * Gets the string representation of the current board with the numbers of
   * contained seeds representing a pit. The upper line belongs to the machine
   * and the lower to the human. The winning store is always the one to the
   * right in the respective game direction of the player, i.e., the one with
   * no opponent pit on the other line. Numbers are right aligned in columns
   * of width digits of the maximum number in any pit or store. These columns
   * are horizontally separated by an extra single white space.
   *
   * @return The string representation of the current game status with pits by
   *         number of currently contained seeds.
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
