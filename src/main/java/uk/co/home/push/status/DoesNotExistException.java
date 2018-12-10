package uk.co.home.push.status;

public class DoesNotExistException extends IllegalArgumentException {
	private static final long serialVersionUID = 1L;

	public DoesNotExistException(String message) {
        super(message);
    }

}