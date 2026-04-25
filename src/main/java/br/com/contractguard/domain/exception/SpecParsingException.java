package br.com.contractguard.domain.exception;

/**
 * Exception thrown when an OpenAPI specification cannot be parsed.
 */
public class SpecParsingException extends DomainException {

    public SpecParsingException(String message) {
        super(message);
    }

    public SpecParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
