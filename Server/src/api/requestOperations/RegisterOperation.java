package api.requestOperations;

import api.values.RegisterAndLoginValues;

public class RegisterOperation extends Operation { //classe per operazioni register che contiene i valori adatti
    private final RegisterAndLoginValues values;

    public RegisterOperation(String o, RegisterAndLoginValues v) {
        super(o);
        this.values = v;
    }

    public RegisterAndLoginValues getValues() {
        return values;
    }
}
