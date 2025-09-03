package api.requestOperations;

public class Operation { //classe che contiene gli attirbuti delle operazioni
    private final String operation;

    public Operation(String o) {
        this.operation = o;
    }

    public String getOperation() {
        return this.operation;
    }
}

