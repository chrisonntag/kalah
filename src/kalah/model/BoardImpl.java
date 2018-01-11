package kalah.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import kalah.controller.Node;
import kalah.model.players.Player;
import kalah.exceptions.IllegalMoveException;

// TODO: work with zero indexed-pits! Check!
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
      int level, Pit[][] pits, int sourcePit, int targetPit) {
    this.openingPlayer = openingPlayer;
    this.level = level;
    DEFAULT_PITS_PER_PLAYER = pitsCount;
    DEFAULT_SEEDS_PER_PIT = seedsCount;
    this.pits = pits;
    this.sourcePitOfLastMove = sourcePit;
    this.targetPitOfLastMove = targetPit;
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
    if (targetPitOfLastMove() > 0 && !isGameOver()) {
      Pit lastPit = getPit(targetPitOfLastMove());

      // Check if the next user must miss a turn.
      if (lastPit.isStore() && lastPit.getOwner() == getOpeningPlayer()) {
        return getOpeningPlayer();
      } else {
        return Player.getOpponent(getOpeningPlayer());
      }
    }

    return Player.NONE;
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
          // TODO: null is an undeclared state
          // TODO: --> better return IllegalStateException
          return null;
        }
      } catch (ArrayIndexOutOfBoundsException e) {
        throw new IllegalArgumentException("Error! The pit is not on the grid!");
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

    sourcePitOfLastMove = pit;
    targetPitOfLastMove = normalizePitNum(pitCount);

    // Check if catching is possible and update the pits.
    Pit targetPit = getPit(targetPitOfLastMove());
    Pit opposingPit = getPit(getOpposingPitNum(targetPitOfLastMove()));
    if (!targetPit.isStore() && targetPit.getOwner() == getOpeningPlayer()
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
    int maxPitNum = (DEFAULT_PITS_PER_PLAYER + 1) * 2;
    if (pit == maxPitNum) {
      return maxPitNum / 2;
    } else if (pit == maxPitNum / 2) {
      return maxPitNum;
    } else {
      return maxPitNum - pit;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Board machineMove() {
    Node root = constructTree(0);
    root.updateScore();
    BoardImpl machineBoard = (BoardImpl) root.getMaxChild().getBoard();

    // TODO: DEBUG statements
    //System.out.println(printTree(root));
    //System.out.println(machineBoard);

    return machineBoard;
  }

  // TODO: remove before production
  private String printTree(Node root) {
    if (root.getChildren() != null) {
      StringBuilder sb = new StringBuilder();
      for (Node node : root.getChildren()) {
        for (int i = 1; i < node.getDepth(); i++) {
          sb.append("  ");
        }
        sb.append(node.getBoard().sourcePitOfLastMove());
        sb.append(": L=");
        sb.append(node.getLocalScore());
        sb.append(", G=");
        sb.append(node.getScore());
        sb.append("\n");

        sb.append(printTree(node));
      }
      return sb.toString();
    }
    return "";
  }

  /**
   * Recursively construct the tree.
   *
   * @param depth The current depth of the tree in this recursion.
   * @return The root node of the tree.
   */
  private Node constructTree(int depth) {
    if (depth == level || isGameOver()) {
      // Create a leave.
      return new Node(this.clone(), null, this.calcScore(depth), depth);
    } else {
      // Create a new sub-tree.
      Node root = new Node(this.clone(), this.calcScore(depth), depth);
      //System.out.println("\t\t\t\tR: " + root.getScore());

      // TODO: Check role of opening and next player.
      List<Board> possibleStates =
          getPossibleGameStates(getOpeningPlayer());
      for (Board board : possibleStates) {
        Node child = ((BoardImpl) board).constructTree(depth + 1);
        root.addChild(child);
        //System.out.println("\tC: " + child.getScore());
      }
      return root;
    }
  }

  private List<Board> getPossibleGameStates(Player player) {
    // TODO: is ArrayList useful here?
    List<Board> gameStates = new ArrayList<>();
    for (int pitNum : getPlayerPits(player)) {
      if (getPit(pitNum).getSeeds() != 0) {
        BoardImpl state = this.clone();
        state.sowSeeds(pitNum);
        // TODO: Check role of opening and next player.
        state.openingPlayer = state.next();
        gameStates.add(state);
      }
    }

    return gameStates;
  }

  private double calcScore(int depth) {
    // TODO: shorten this method.

    // Evaluate seeds in the stores.
    int machineStore = (DEFAULT_PITS_PER_PLAYER + 1) * 2;
    int humanStore = DEFAULT_PITS_PER_PLAYER + 1;

    double scoreS = getSeeds(machineStore) - 1.5 * getSeeds(humanStore);

    // Evaluate number of opposing seeds which can be captured in this move.
    double catchableHumanSeeds = 0; // the ones the machine can catch
    double catchableMachineSeeds = 0; // the ones the human can catch
    Set<Integer> targetPitsHuman = getPossibleTargetPits(Player.HUMAN);
    Set<Integer> targetPitsMachine =
        getPossibleTargetPits(Player.MACHINE);

    // TODO: primitive downcast Integer list to int?
    // Loops through all possible target pits of the human in order to find
    // catchable seeds of the machine.
    for (int target : targetPitsHuman) {
      if (getPit(target).getSeeds() == 0
          && getPit(target).getOwner() == Player.HUMAN
          && !getPit(target).isStore()) {
        catchableMachineSeeds += getPit(getOpposingPitNum(target)).getSeeds();
      }
    }

    // Loops through all possible target pits of the machine in order to find
    // catchable seeds of the human.
    for (int target : targetPitsMachine) {
      if (getPit(target).getSeeds() == 0
          && getPit(target).getOwner() == Player.MACHINE
          && !getPit(target).isStore()) {
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

  private List<Integer> getPlayerPits(Player player) {
    List<Integer> playerPits = new ArrayList<>();
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
   * Returns a set of target pits which can be reached in this move. Due to the
   * property of a set not allowing duplicates the target pits will occur only
   * once even if they can be reached by several different source pits. This
   * ensures that pits won't be counted twice.
   *
   * @param player The player which can capture.
   * @return A set of pits
   */
  private Set<Integer> getPossibleTargetPits(Player player) {
    Set<Integer> targetPitNums = new HashSet<>();

    for (int pitNum : getPlayerPits(player)) {
      Pit sourcePit = getPit(pitNum);
      if (sourcePit.getSeeds() != 0) {
        int targetPitNum = normalizePitNum(pitNum + sourcePit.getSeeds());
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
    if (getSeedsOfPlayer(Player.MACHINE) > getSeedsOfPlayer(Player.HUMAN)) {
      return Player.MACHINE;
    } else if (getSeedsOfPlayer(Player.MACHINE)
        < getSeedsOfPlayer(Player.HUMAN)) {
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
    // TODO: exception for not valid numbers?
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
    // TODO: The number of the move opponent's stores is not possible?
    return this.sourcePitOfLastMove;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int targetPitOfLastMove() {
    // TODO: The number of the move opponent's stores is not possible?
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
        this.DEFAULT_SEEDS_PER_PIT, this.level, newPits, sourcePitOfLastMove(),
        targetPitOfLastMove());
  }

  /**
   * {@inheritDoc}
   */
  /*
  @Override
  public String toString() {
    List<Integer> decimalPlaces = new LinkedList<>();
    StringBuilder upperRow = new StringBuilder();
    StringBuilder lowerRow = new StringBuilder();

    for (int j = 1; j < getPitsPerPlayer() + 1; j++) {
      // Evaluate maximum number of decimal places.
      int maxDecPlaces = 1;

      for (int i = 0; i < this.pits.length; i++) {
        if (i == 1) {
          if (this.pits[i][j - 1].toString().length() > maxDecPlaces) {
            maxDecPlaces = this.pits[i][j - 1].toString().length();
          }
        } else {
          if (this.pits[i][j].toString().length() > maxDecPlaces) {
            maxDecPlaces = this.pits[i][j].toString().length();
          }
        }
      }

      decimalPlaces.add(maxDecPlaces);
    }

    int lowerRowLeftMargin = 0;
    for (int j = 0; j < getPitsPerPlayer() + 1; j++) {
      for (int i = 0; i < this.pits.length; i++) {
        int maxDecPlaces = 0;
        if (j == 0 && i == 0) {
          lowerRowLeftMargin = this.pits[i][j].toString().length() + 1;
        } else if (j != getPitsPerPlayer()) {
          if (j > 0 && i == 0) {
            maxDecPlaces = decimalPlaces.get(j - 1);
          } else {
            maxDecPlaces = decimalPlaces.get(j);
          }
        }
        StringBuilder pit = new StringBuilder();
        int pitLength = this.pits[i][j].toString().length();

        if (pitLength < maxDecPlaces) {
          for (int k = 0; k < maxDecPlaces - pitLength; k++) {
            pit.append(" ");
          }
        }

        pit.append(this.pits[i][j].toString());
        if (j != getPitsPerPlayer()) {
          pit.append(" ");
        }

        if (i == 0) {
          upperRow.append(pit);
        } else {
          lowerRow.append(pit);
        }
      }
    }

    for (int i = 0; i < lowerRowLeftMargin; i++) {
      lowerRow.insert(0, " ");
    }
    upperRow.append("\n");

    return upperRow.toString() + lowerRow.toString();
  }
*/
  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    int maxDecimalNum = 1;
    for (int i = 0; i < this.pits.length; i++) {
      for (int j = 0; j < this.pits[i].length; j++) {
        if (this.pits[i][j].toString().length() > maxDecimalNum) {
          maxDecimalNum = this.pits[i][j].toString().length();
        }
      }
    }
    StringBuilder upperRow = new StringBuilder();
    StringBuilder lowerRow = new StringBuilder();
    int lowerRowMargin = 2;
    for (int i = 0; i < this.pits.length; i++) {
      for (int j = 0; j < this.pits[i].length; j++) {
        if (j == 0 && i == 0) {
          lowerRowMargin = this.pits[i][j].toString().length() + 1;
        }

        String pit = String.format("%" + maxDecimalNum + "d", this.pits[i][j].getSeeds());
        if (i == 0) {
          upperRow.append(pit);
          if (j < getPitsPerPlayer()) {
            upperRow.append(" ");
          }
        } else {
          lowerRow.append(pit);
          if (j < getPitsPerPlayer()) {
            lowerRow.append(" ");
          }
        }
      }
    }

    for (int i = 0; i < lowerRowMargin; i++) {
      lowerRow.insert(0, " ");
    }
    return upperRow.toString().trim() + "\n" + lowerRow.toString();
  }

}
