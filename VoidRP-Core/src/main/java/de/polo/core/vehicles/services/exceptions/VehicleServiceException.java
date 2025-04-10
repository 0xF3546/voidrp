package de.polo.core.vehicles.services.exceptions;

/**
 * Exception thrown when an error occurs during vehicle service operations.
 * This exception is used to encapsulate errors related to vehicle management,
 * such as spawning, deleting, or updating vehicle data.
 *
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class VehicleServiceException extends RuntimeException {

    /**
     * Constructs a new VehicleServiceException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public VehicleServiceException(String message) {
        super(message);
    }

    /**
     * Constructs a new VehicleServiceException with the specified detail message
     * and cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause   the underlying cause of the exception (e.g., SQLException)
     */
    public VehicleServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}