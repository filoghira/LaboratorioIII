package api.operations;

import api.values.UpdateCredentialsValues;

public class UpdateCredentialsOperation extends Operation { //classe per operazioni updateCredentials
    private final UpdateCredentialsValues values;

    public UpdateCredentialsOperation(String o, UpdateCredentialsValues v) {
        super(o);
        this.values = v;
    }

    public UpdateCredentialsValues getValues() {
        return values;
    }
}
