package es.upm.api.data.entities;

public class BadCredentialsException extends RuntimeException {
    private static final String DESCRIPTION = "Bad Credentials Exception. ";

    public BadCredentialsException(String detail) {
        super(DESCRIPTION + ". " + detail);
    }

}
