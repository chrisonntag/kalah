package kalah.controller;

import java.util.ArrayList;
import java.util.List;
import kalah.model.Board;
import kalah.model.players.Player;

public class Node {

  private double score;
  private int level;
  private Board board;
  private List<Node> children = new ArrayList<>();

  public Node(Board board, List<Node> children, double score) {
    this.board = board;
    this.score = score;
    this.children = children;
  }

  public Node(Board board, double score) {
    this.board = board;
    this.score = score;
  }

  public List<Node> getChildren() {
    return this.children;
  }

  public void setChildren(List<Node> children) {
    this.children = children;
  }

  public double getScore() {
    return score;
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
      this.getChildren().forEach(Node::updateScore);
      if (this.getChildren().size() == 0) {
        return;
      }
      if (this.getBoard().next() == Player.MACHINE) {
        this.setScore(this.getMax().getScore() + this.getScore());
      } else {
        this.setScore(this.getMin().getScore() + this.getScore());
      }
    }
  }

  public Node getMin() {
    Node minChild = children.get(0);
    for (Node node : this.children) {
      if (node.getScore() < minChild.getScore()) {
        minChild = node;
      }
    }

    return minChild;
  }

  public Node getMax() {
    Node maxChild = children.get(0);
    for (Node node : this.children) {
      if (node.getScore() > maxChild.getScore()) {
        maxChild = node;
      }
    }

    return maxChild;
  }

}
