package codeelements.Type;

import java.util.ArrayList;

/**
 * Created by HoseinGhahremanzadeh on 6/24/2017.
 */
public class ComplexType {
    public long size;
    public TypeParent type;
    public ArrayList<Long> dimensions;

    @Override
    public boolean equals(Object obj) {
        ComplexType input = ((ComplexType)obj);
        if (dimensions.size() != (input.dimensions.size()))
            return false;
        for (int i = 0; i<dimensions.size(); i++)
            if (dimensions.get(i) != input.dimensions.get(i))
                return false;
        return type.name.equals(input.type.name);
    }
}
