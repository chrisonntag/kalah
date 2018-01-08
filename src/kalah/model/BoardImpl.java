package kalah.model;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import kalah.controller.Node;
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

  // TODO: Check switching to minimal int[][] array.
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
    // TODO: class secret?
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
          // TODO: null is an undeclared state --> better return
          // IllegalStateException
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
    Pit opposingPit = getPit(getOpposingPitNum(targetPitOfLastMove()));
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

  private int getOpposingPitNum(int pit) {
    return (DEFAULT_PITS_PER_PLAYER + 1) * 2 - pit;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Board machineMove() {
    Node root = constructTree(0);
    root.updateScore();

    return root.getMax().getBoard();
  }

  private Node constructTree(int depth) {
    if (depth == level || isGameOver()) {
      return new Node(this.clone(), null, this.calcScore(depth));
    } else {
      Node root = new Node(this, this.calcScore(depth));

      for (Board board : getPossibleGameStates(Player.MACHINE)) {
        root.addChild(
            ((BoardImpl) board).constructTree(depth + 1)
        );
      }
      return root;
    }
  }

  private ArrayList<Board> getPossibleGameStates(Player player) {
    // TODO: is ArrayList useful here?
    ArrayList<Board> gameStates = new ArrayList<>();
    for (int pitNum : getPlayerPits(player)) {
      if (getPit(pitNum).getSeeds() != 0) {
        gameStates.add(this.move(pitNum));
      }
    }

    return gameStates;
  }

  private double calcScore(int depth) {
    // Evaluate seeds in the stores.
    int machineStore = (DEFAULT_PITS_PER_PLAYER + 1) * 2;
    int humanStore = DEFAULT_PITS_PER_PLAYER + 1;

    double scoreS = getSeeds(machineStore) - 1.5 * getSeeds(humanStore);

    // Evaluate number of opposing seeds which can be captured in this move.
    double catchableHumanSeeds = 0; // the ones the machine can catch
    double catchableMachineSeeds = 0; // the ones the human can catch

    // Implicit downcast (TODO?)
    for (int target : getPossibleTargetPits(Player.HUMAN)) {
      // TODO: Check only own pits?
      if (getPit(target).getSeeds() == 0
          && getPit(target).getOwner() == Player.HUMAN) {
        catchableMachineSeeds += getPit(getOpposingPitNum(target)).getSeeds();
      }
    }

    for (int target : getPossibleTargetPits(Player.MACHINE)) {
      // TODO: Check only own pits?
      if (getPit(target).getSeeds() == 0
          && getPit(target).getOwner() == Player.MACHINE) {
        catchableHumanSeeds += getPit(getOpposingPitNum(target)).getSeeds();
      }
    }
    double scoreC = catchableHumanSeeds - 1.5 * catchableMachineSeeds;

    // Evaluate the number of empty pits whose opposing opposite
    // pits contain at least twice the number of seeds as initial
    // per hollow at game start.
    double emptyHumanPits = 0;
    double emptyMachinePits = 0;
    for (int pitNum : getPlayerPits(Player.HUMAN)) {
      if (getPit(pitNum).getSeeds() == 0
          && getPit(getOpposingPitNum(pitNum)).getSeeds()
          >= 2 * getSeedsPerPit()) {
        emptyHumanPits += 1;
      }
    }

    for (int pitNum : getPlayerPits(Player.MACHINE)) {
      if (getPit(pitNum).getSeeds() == 0
          && getPit(getOpposingPitNum(pitNum)).getSeeds()
          >= 2 * getSeedsPerPit()) {
        emptyMachinePits += 1;
      }
    }

    double scoreP = emptyMachinePits - 1.5 * emptyHumanPits;

    // Evaluate if a move leads to a victory immediately.
    double scoreV;
    if (!isGameOver()) {
      scoreV = 0;
    } else {
      if (getWinner() == null) {
        scoreV = 0;
      } else {
        if (getWinner() == Player.HUMAN) {
          return -1.5 * (500.0 / depth);
        } else {
          return 500.0 / depth;
        }
      }
    }

    return 3 * scoreS + scoreC + scoreP + scoreV;
  }

  private ArrayList<Integer> getPlayerPits(Player player) {
    ArrayList<Integer> playerPits = new ArrayList<>();
    int firstPitNum = 1;
    int lastPitNum = getPitsPerPlayer();
    if (player == Player.MACHINE) {
      firstPitNum = getPitsPerPlayer() + 2; // TODO: magic number
      lastPitNum = getPitsPerPlayer() * 2 + 1;
    }

    // TODO: magic number
    for (int i = firstPitNum; i <= lastPitNum; i++) {
      playerPits.add(i);
    }

    return playerPits;
  }

  /**
   * Returns a list of target pits which can be reached in this move.
   *
   * @param player The player which can capture.
   * @return A list of pits
   */
  private ArrayList<Integer> getPossibleTargetPits(Player player) {
    ArrayList<Integer> targetPitNums = new ArrayList<>();

    for (int pitNum : getPlayerPits(player)) {
      Pit sourcePit = getPit(pitNum);
      if (sourcePit.getSeeds() != 0) {
        int targetPitNum = pitNum + sourcePit.getSeeds();
        targetPitNums.add(targetPitNum);
      }
    }

    return targetPitNums;
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
    boolean upperRowPitsEmpty = true;
    boolean lowerRowPitsEmpty = true;

    // TODO: Magic number: Set HUMAN_ROW and MACHINE_ROW
    for (int i = 0; i < 2; i++) {
      for (int j = 0; j <= DEFAULT_PITS_PER_PLAYER; j++) {
        // Check if a players row only contains empty pits.
        if (!this.pits[i][j].isStore() && this.pits[i][j].getSeeds() > 0) {
          if (i == 0) {
            upperRowPitsEmpty = false;
          } else {
            lowerRowPitsEmpty = false;
          }
          break; // Break loop at the first pit with more than one seed.
        }
      }
    }

    // The game ends if either the players or the opponents pits are empty.
    return upperRowPitsEmpty || lowerRowPitsEmpty;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Player getWinner() {
    int upperSeeds = 0;
    int lowerSeeds = 0;

    // TODO: Magic number: Set HUMAN_ROW and MACHINE_ROW
    for (int i = 0; i < 2; i++) {
      for (int j = 0; j <= DEFAULT_PITS_PER_PLAYER; j++) {
        if (i == 0) {
          upperSeeds += this.pits[i][j].getSeeds();
        } else {
          lowerSeeds += this.pits[i][j].getSeeds();
        }
      }
    }

    // TODO: Magic number: Set HUMAN_ROW and MACHINE_ROW
    if (upperSeeds > lowerSeeds) {
      return Player.MACHINE;
    } else if (upperSeeds < lowerSeeds) {
      return Player.HUMAN;
    } else {
      return null;
    }
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
    // TODO: use super.clone()
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
