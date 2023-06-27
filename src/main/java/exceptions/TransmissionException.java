package exceptions;

public class TransmissionException extends RuntimeException{

    public TransmissionException(String message) { super(message); }

    public static TransmissionException maxAttempts() {
        return new TransmissionException("Maximum amount of transmission attempts for packet reached, aborting transmission");
    }
}
