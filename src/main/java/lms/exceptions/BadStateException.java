package lms.exceptions;

/**
 * Special Runtime exception -- Note that this is a Runtime (unchecked
 * exception) which is unlike our normal `extends Exception` as we want to be
 * able to add this to a method without needing to have it in a method
 * signature.
 */
public class BadStateException extends RuntimeException {

    /**
     * Constructs a new BadStateException with no message.
     */
    public BadStateException() {
    }

    /**
     * Constructs a new BadStateException with the specified error message.
     *
     * @param message A String containing the error message to be associated
     * with this exception.
     */
    public BadStateException(String message) {
        super(message);
    }

    /**
     * Constructs a new BadStateException with the specified detail message and
     * cause.
     *
     * @param message the detail message (which is saved for later retrieval by
     * the getMessage() method)
     * @param cause the cause (which is saved for later retrieval by the
     * getCause() method)
     */
    public BadStateException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new BadStateException with the specified cause.
     *
     * @param cause the cause (which is saved for later retrieval by the
     * getCause() method)
     */
    public BadStateException(Throwable cause) {
        super(cause);
    }

}
