package api.request;

import api.values.RegisterAndLoginValues;

/**
 * GSON class for register operation
 */
public class RegisterOperation extends Operation {
    private final RegisterAndLoginValues values;

    public RegisterOperation(String o, RegisterAndLoginValues v) {
        super(o);
        this.values = v;
    }

    public RegisterAndLoginValues getValues() {
        return values;
    }
}
