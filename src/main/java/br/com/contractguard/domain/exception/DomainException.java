package br.com.contractguard.domain.exception;

/**
 * Base exception for all domain rule violations in ContractGuard.
 *
 * <p>Thrown when an invariant defined by the domain model is violated (e.g., a required
 * field is null or blank, or a business rule check fails).
 *
 * <p>This is an unchecked exception — callers are not forced to catch it, but the
 * presentation layer must handle it and map it to an appropriate HTTP response (400/422).
 */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
