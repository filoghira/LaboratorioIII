package api.request;

/**
 * Parent class for every possible operation
 */
public class Operation {
    private final String operation;

    public Operation(String o) {
        this.operation = o;
    }

    public String getOperation() {
        return this.operation;
    }
}

