package es.upm.api.exceptions;

public class BadCredentialsException extends RuntimeException {
    private static final String DESCRIPTION = "Bad Credentials Exception. ";

    public BadCredentialsException(String detail) {
        super(DESCRIPTION + ". " + detail);
    }

}
