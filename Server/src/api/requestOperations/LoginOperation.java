package api.requestOperations;

import api.values.RegisterAndLoginValues;

public class LoginOperation extends Operation { //classe per le operazioni login
    private final RegisterAndLoginValues values;

    public LoginOperation(String o, RegisterAndLoginValues v) {
        super(o);
        this.values = v;
    }

    public RegisterAndLoginValues getValues() {
        return values;
    }
}
