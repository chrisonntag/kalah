package kalah.controller;

import java.util.ArrayList;
import java.util.List;
import kalah.model.Board;
import kalah.model.players.Player;

public class Node {

  private double score;
  private double localScore;
  private int level;
  private Board board;
  private List<Node> children = new ArrayList<>();

  public Node(Board board, List<Node> children, double score, int level) {
    this.board = board;
    this.score = score;
    this.localScore = score;
    this.children = children;
    this.level = level;
  }

  public Node(Board board, double score, int level) {
    this.board = board;
    this.score = score;
    this.localScore = score;
    this.level = level;
  }

  public List<Node> getChildren() {
    return this.children;
  }

  public void setChildren(List<Node> children) {
    this.children = children;
  }

  public void addChild(Node node) {
    this.children.add(node);
  }

  public double getScore() {
    return score;
  }

  public double getLocalScore() {
    return localScore;
  }

  public void setScore(double score) {
    this.score = score;
  }

  public int getLevel() {
    return level;
  }

  public void setLevel(int level) {
    this.level = level;
  }

  public Board getBoard() {
    return board;
  }

  public void setBoard(Board board) {
    this.board = board;
  }

  /**
   * Update score on the min max tree by adding the highest or lowest score of
   * the children or a node to the local score. If the move by the child node
   * is a human move the lowest is added, the highest otherwise.
   */
  public void updateScore() {
    if (this.getChildren() != null) {
      // Calling this method recursively at the beginning ensures that
      // the total score will be calculated at the leaves first wandering up
      // to the root of the tree.
      this.getChildren().forEach(Node::updateScore);

      if (this.getChildren().size() == 0) {
        return;
      }

      if (this.getBoard().getOpeningPlayer() == Player.MACHINE) {
        this.setScore(this.getMaxChild().getScore() + this.getScore());
      } else if (this.getBoard().getOpeningPlayer() == Player.HUMAN) {
        this.setScore(this.getMinChild().getScore() + this.getScore());
      }
    }
  }

  public Node getMinChild() {
    List<Node> children = getChildren();
    Node minChild = children.get(0);
    for (Node node : children) {
      if (node.getScore() < minChild.getScore()) {
        minChild = node;
      }
    }

    return minChild;
  }

  public Node getMaxChild() {
    List<Node> children = getChildren();
    Node maxChild = children.get(0);
    for (Node node : this.children) {
      if (node.getScore() > maxChild.getScore()) {
        maxChild = node;
      }
    }

    return maxChild;
  }

  @Override
  public String toString() {
    return "Level: " + level + ", Score: " + score;
  }

}
