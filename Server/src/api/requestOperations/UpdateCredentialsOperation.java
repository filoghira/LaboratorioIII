package api.requestOperations;

import api.values.UpdateCredentialsValues;

/**
 * GSON class for updateCredentials operation
 */
public class UpdateCredentialsOperation extends Operation {
    private final UpdateCredentialsValues values;

    public UpdateCredentialsOperation(String o, UpdateCredentialsValues v) {
        super(o);
        this.values = v;
    }

    public UpdateCredentialsValues getValues() {
        return values;
    }
}
