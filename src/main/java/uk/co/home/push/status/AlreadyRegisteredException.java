package uk.co.home.push.status;

public class AlreadyRegisteredException extends IllegalArgumentException {
	private static final long serialVersionUID = 1L;

    public AlreadyRegisteredException(String message) {
        super(message);
    }

}