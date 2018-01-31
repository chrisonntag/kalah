package kalah.exceptions;

/**
 * This RuntimeException is being thrown only if the game is already over,
 * or it is not the human's turn.
 */
public class IllegalMoveException extends RuntimeException {

    private static final long serialVersionUID = 5257933270451043028L;

    /**
     * Instantiates a {@link IllegalMoveException}.
     *
     * @param msg The exception message.
     */
    public IllegalMoveException(String msg) {
        super(msg);
    }

}
