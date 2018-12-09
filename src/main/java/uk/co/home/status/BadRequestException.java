package uk.co.home.status;

public class BadRequestException extends IllegalArgumentException {
	private static final long serialVersionUID = 1L;

    public BadRequestException(String message) {
        super(message);
    }

}