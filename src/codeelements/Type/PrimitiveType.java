package codeelements.Type;

import scanner.IntegerSize;
import scanner.RealNumberSize;

/**
 * Created by HoseinGhahremanzadeh on 6/24/2017.
 */
public class PrimitiveType extends TypeParent {
    public static final PrimitiveType  Int = new PrimitiveType("int", IntegerSize.IntSize),
                                Long = new PrimitiveType("long", IntegerSize.LongSize),
                                LongLong = new PrimitiveType("long long", IntegerSize.LongLongSize),
                                Float = new PrimitiveType("float", RealNumberSize.FloatSize),
                                Double = new PrimitiveType("double", RealNumberSize.DoubleSize),
                                Char = new PrimitiveType("char", 8L),
                                String = new PrimitiveType("String", 64L),
                                Byte = new PrimitiveType("byte", 8L),
                                Bool = new PrimitiveType("bool", 8L),
                                Void = new PrimitiveType("void", 0L);
    private PrimitiveType (String name, long size) {
        super(name, size);
    }
}
