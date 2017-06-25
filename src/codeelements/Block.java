package codeelements;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by HoseinGhahremanzadeh on 6/25/2017.
 */
public class Block {
    public long stackEnd = 0;
    public Block parent = null;
    public Map<String, Variable> symbolTable = new HashMap<>();
    public Variable getVariable (String name) {
        Block currentBlock = this;
        while (currentBlock!=null) {
            if (currentBlock.symbolTable.get(name) != null)
                return currentBlock.symbolTable.get(name);
            currentBlock = currentBlock.parent;
        }
        return null;
    }
}
