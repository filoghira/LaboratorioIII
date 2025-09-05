package api.request;

import api.values.RegisterAndLoginValues;

/**
 * GSON class for login operation
 */
public class LoginOperation extends Operation {
    private final RegisterAndLoginValues values;

    public LoginOperation(String o, RegisterAndLoginValues v) {
        super(o);
        this.values = v;
    }

    public RegisterAndLoginValues getValues() {
        return values;
    }
}
