package VMElements;

import codeelements.Type.ComplexType;
import codeelements.Type.ExprStorageType;
import codeelements.Type.PrimitiveType;

import java.util.ArrayList;

/**
 * Created by HoseinGhahremanzadeh on 7/4/2017.
 */
public class VM {
    private final VMArch architecture;
    private final VMInstructions instructions;
    ComplexType AddressType = new ComplexType();
    public VM(VMArch architecture) {
        this.architecture = architecture;
        instructions = new VMInstructions(architecture);
        switch (architecture) {
            case _32Bit:
                AddressType.dimensions = new ArrayList<>();
                AddressType.type = PrimitiveType.Int;
                AddressType.size = AddressType.type.size;
                break;
            default:
                break;
        }
    }

    public ComplexType getAddressType() {
        return AddressType;

    }
    public VMInstructions getInstructions() {
        return instructions;
    }
}
