package es.upm.api.services.exceptions;

public class InternalServerException extends RuntimeException {
    private static final String DESCRIPTION = "Internal Server Exception";

    public InternalServerException(String detail) {
        super(DESCRIPTION + ". " + detail);
    }

}
