package dev.vansen.singleline;

public enum SingleLineOptions {
    /**
     * Enables debug mode, which will print out debug information.
     */
    DEBUG(false),
    /**
     * Enables uses of the component logger, this will only work on paper servers.
     */
    USE_COMPONENT_LOGGER(false),
    /**
     * Enables using the normal logger instead of the print statement.
     */
    USE_NORMAL_LOGGER_INSTEAD_OF_PRINT(true);

    private boolean value;

    SingleLineOptions(boolean value) {
        this.value = value;
    }

    public boolean enabled() {
        return value;
    }

    public void enabled(boolean value) {
        this.value = value;
    }
}
