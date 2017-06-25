package codeelements.Type;

import codeelements.Type.TypeParent;
import codeelements.Variable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by HoseinGhahremanzadeh on 6/24/2017.
 */
public class Record extends TypeParent {
    public Map<String, Variable> fields = new HashMap<>();
    public Record(String name, ArrayList<Variable> fields) {
        super(name, 0);
        for (Variable var : fields) {
            var.startAddress = size;
            size+=var.type.size;
        }
    }
}
