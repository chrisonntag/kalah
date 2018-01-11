package kalah.exceptions;

/**
 * This RuntimeException is being thrown only if the game is already over,
 * or it is not the human's turn.
 */
public class IllegalMoveException extends RuntimeException {

    /**
     * Instantiates a {@link IllegalMoveException}.
     *
     * @param msg The exception message.
     */
    public IllegalMoveException(String msg) {
        super(msg);
    }

}
