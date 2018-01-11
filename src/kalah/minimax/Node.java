package kalah.minimax;

import java.util.ArrayList;
import java.util.List;
import kalah.model.Board;
import kalah.model.players.Player;

/**
 * Represents a node in a tree with up to n children. The {@link Node} itself
 * holds a {@link #board}, it's calculated {@link #localScore} and a
 * main {@link #score} which is the sum of it's maximum or minimum child
 * and the {@link #localScore}.
 */
public class Node {

    private double score;
    private double localScore;
    private int depth;
    private Board board;
    private List<Node> children = new ArrayList<>();

    /**
     * Instantiates a new {@link Node} object.
     *
     * @param board The game board object.
     * @param children A list of children which will be set for this node.
     * @param score The calculated score for this board.
     * @param depth The current depth of the node in the tree.
     */
    public Node(Board board, List<Node> children, double score, int depth) {
        // TODO: is this constructor needed?
        this.board = board;
        this.score = score;
        this.localScore = score;
        this.children = children;
        this.depth = depth;
    }

    /**
     * Instantiates a new {@link Node} object.
     *
     * @param board The game board object.
     * @param score The calculated score for this board.
     * @param depth The current depth of the node in the tree.
     */
    public Node(Board board, double score, int depth) {
        this.board = board;
        this.score = score;
        this.localScore = score;
        this.depth = depth;
    }

    /**
     * Gets this nodes children.
     *
     * @return A list of this nodes children.
     */
    public List<Node> getChildren() {
        return children;
    }

    /**
     * Adds a single {@link Node} to this nodes children.
     *
     * @param node The node which should be set as a child.
     */
    public void addChild(Node node) {
        children.add(node);
    }

    /**
     * Gets this nodes total {@link #score}, which is the sum of the
     * {@link #localScore} and the score of the maximum/minimum child of
     * this node.
     *
     * @return The total score of this node.
     */
    public double getScore() {
        return score;
    }

    /**
     * Sets the total {@link #score}, which is the sum of the
     * {@link #localScore} and the score of the maximum/minimum child of
     * this node.
     *
     * @param score The score value which should be set.
     */
    public void setScore(double score) {
        this.score = score;
    }

    /**
     * Gets the {@link #localScore} of this node as calculated in the Board
     * implementation.
     *
     * @return The local score of this node.
     */
    public double getLocalScore() {
        return localScore;
    }

    /**
     * Gets the {@link #depth} of the node in the whole tree.
     *
     * @return The depth of the node.
     */
    public int getDepth() {
        return depth;
    }

    /**
     * Gets the {@link #board} object of this node.
     *
     * @return The Board object.
     */
    public Board getBoard() {
        return board;
    }

    /**
     * Update the total {@link #score} of this node by adding the maximum or
     * minimum score of it's children to the {@link #localScore} depending on
     * which player is the current player in the child node. If the move on a
     * children's node is a human move the minimum is added, otherwise
     * the maximum.
     */
    public void updateScore() {
        if (getChildren() != null) {
            // Calling this method recursively at the beginning ensures that
            // the total score will be calculated at the leaves first wandering
            // up to the root of the tree.
            getChildren().forEach(Node::updateScore);

            if (getChildren().size() == 0) {
                return;
            }

            if (getBoard().getOpeningPlayer() == Player.MACHINE) {
                setScore(getMaxChild().getScore() + getScore());
            } else if (getBoard().getOpeningPlayer() == Player.HUMAN) {
                setScore(getMinChild().getScore() + getScore());
            }
        }
    }

    /**
     * Checks each of this nodes children for the minimum, depending on it's
     * total {@link #score}.
     *
     * @return The minimum child's node.
     */
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

    /**
     * Checks each of this nodes children for the maximum, depending on it's
     * total {@link #score}.
     *
     * @return The maximum child's node.
     */
    public Node getMaxChild() {
        List<Node> children = getChildren();
        Node maxChild = children.get(0);
        for (Node node : children) {
            if (node.getScore() > maxChild.getScore()) {
                maxChild = node;
            }
        }

        return maxChild;
    }

}
