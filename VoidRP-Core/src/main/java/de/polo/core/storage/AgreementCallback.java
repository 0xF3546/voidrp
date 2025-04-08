package de.polo.core.storage;

// Define a functional interface for the callback
@FunctionalInterface
public interface AgreementCallback { // Changed to public
    void execute();
}
