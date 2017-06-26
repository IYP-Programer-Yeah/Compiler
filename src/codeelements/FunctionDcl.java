package codeelements;

import codeelements.Type.ComplexType;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by HoseinGhahremanzadeh on 6/25/2017.
 */
public class FunctionDcl {
    public String name;
    public boolean isExtern = false;
    public boolean isUsed = false;
    public boolean isComplete = false;
    public ComplexType returnType;
    public ArrayList<Variable> arguments = new ArrayList<>();
    public Block currentBlock = new Block();
    public Map<String, Label> labels = new HashMap<>();
    public String bodyCode = "";

    @Override
    public boolean equals(Object obj) {
        FunctionDcl input = (FunctionDcl)obj;
        if (!name.equals(input.name))
            return false;
        if (isExtern || input.isExtern)
            return true;
        if (arguments.size()!=input.arguments.size())
            return false;
        for (int i=0;i<arguments.size();i++)
            if (!arguments.get(i).type.equals(input.arguments.get(i).type))
                return false;
        return true;
    }
}
