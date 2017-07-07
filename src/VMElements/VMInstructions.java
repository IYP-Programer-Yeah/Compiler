package VMElements;

import codeelements.Type.*;

import javax.crypto.AEADBadTagException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by HoseinGhahremanzadeh on 7/4/2017.
 */
public class VMInstructions {
    private VMArch architecture;
    private Set<Integer> usedRegisters = new HashSet<>();
    private String warnings, errors;
    private ArrayList<PrimitiveType> typeSuportingArithmatics = new ArrayList<>();
    private int usedLabels = 0;

    private int findNextViableRegister() {
        int result = 0;
        while(!usedRegisters.add(result))
            result++;
        return result;
    }

    private void releaseRegister(int register) {
        usedRegisters.remove(register);
    }

    public VMInstructions (VMArch architecture) {
        this.architecture = architecture;
        typeSuportingArithmatics.add(PrimitiveType.Char);
        typeSuportingArithmatics.add(PrimitiveType.Byte);
        typeSuportingArithmatics.add(PrimitiveType.Int);
        typeSuportingArithmatics.add(PrimitiveType.Long);
        typeSuportingArithmatics.add(PrimitiveType.LongLong);
        typeSuportingArithmatics.add(PrimitiveType.Float);
        typeSuportingArithmatics.add(PrimitiveType.Double);
    }

    public VMGeneratedOperationCode upCast(String srcExpr, ExprType srcExprType, PrimitiveType dstType) {
        VMGeneratedOperationCode generatedOperationCode = new VMGeneratedOperationCode();
        generatedOperationCode.resultType = new ExprType();
        generatedOperationCode.resultType.storageType = srcExprType.storageType;
        generatedOperationCode.code = "";
        generatedOperationCode.result = srcExpr;
        if (srcExprType.type == null && srcExprType.storageType == ExprStorageType.VariableStorage) {
            errors = errors + "Unrecognized type for variable. Returning original expression.\n";
            return generatedOperationCode;
        }
        generatedOperationCode.resultType.type = new ComplexType();
        generatedOperationCode.resultType.type.type = dstType;
        generatedOperationCode.resultType.type.dimensions = new ArrayList<>();
        if (srcExprType.type.type == dstType)
            return generatedOperationCode;
        if (typeSuportingArithmatics.indexOf(srcExprType.type.type) > typeSuportingArithmatics.indexOf(dstType)) {
            errors = errors + "Down cast using up cast, da fuq. Returning original expression.\n";
            return generatedOperationCode;
        }
        if (typeSuportingArithmatics.indexOf(srcExprType.type.type) == -1 || typeSuportingArithmatics.indexOf(dstType) == -1) {
            errors = errors + "Can't cast type " + srcExprType.type.type.name + " to type " + dstType.name + ". Returning original expression.\n";
            return generatedOperationCode;
        }
        if (srcExprType.type.dimensions.size() > 0) {
            errors = errors + "Can't cast array of type " + srcExprType.type.type.name + " to type " + dstType.name + ". Returning original expression.\n";
            return generatedOperationCode;
        }
        int resultLowerRegister = findNextViableRegister();

        generatedOperationCode.resultType.storageType = ExprStorageType.RegisterStorage;

        switch (srcExprType.storageType) {
            case ConstantStorage: {
                generatedOperationCode.result = srcExpr;
                generatedOperationCode.resultType.storageType = ExprStorageType.ConstantStorage;
            }
                break;
            case RegisterStorage: {
                switch (architecture) {
                    case _32Bit: {
                        if (dstType == PrimitiveType.Double) {

                        } else {
                            generatedOperationCode.code = generatedOperationCode.code + "MOV R" + resultLowerRegister + ", " + srcExpr + ", " + (srcExprType.type.type.size / 8) + "\n";
                            if (srcExprType.type.type.size < 32) {
                                if (srcExprType.type.type == PrimitiveType.Byte)
                                    generatedOperationCode.code = generatedOperationCode.code + "EXT R" + resultLowerRegister + ", " + (srcExprType.type.type.size / 8) + ", " + Math.min(dstType.size / 8, 4) + "\n";
                                else if (srcExprType.type.size < dstType.size)
                                    generatedOperationCode.code = generatedOperationCode.code + "EXS R" + resultLowerRegister + ", " + (srcExprType.type.type.size / 8) + ", " + Math.min(dstType.size / 8, 4) + "\n";
                            }

                            if (dstType == PrimitiveType.Float) {
                                generatedOperationCode.code = generatedOperationCode.code + "ITF R" + resultLowerRegister + ", R" + resultLowerRegister + ", 4\n";
                                if (srcExprType.type.type.size > 32) {
                                    int temp = findNextViableRegister();
                                    generatedOperationCode.code = generatedOperationCode.code + "ITF R" + temp + ", R" + generatedOperationCode.higherRegister + ", 4\n";
                                    generatedOperationCode.code = generatedOperationCode.code + "MLF R" + temp + ", R" + temp + ", #4294967296.0\n";
                                    generatedOperationCode.code = generatedOperationCode.code + "ADF R" + resultLowerRegister + ", R" + resultLowerRegister + ", R" + temp + "\n";
                                    generatedOperationCode.code = generatedOperationCode.code + "MOV R" + temp + ", " + srcExpr + ", 4\n";
                                    generatedOperationCode.code = generatedOperationCode.code + "LSR R" + resultLowerRegister + ", #31" + ", 4\n";
                                    generatedOperationCode.code = generatedOperationCode.code + "JIZ VMILabel" + usedLabels + ", R" + resultLowerRegister + ", 4\n";
                                    generatedOperationCode.code = generatedOperationCode.code + "ADF R" + temp + ", R" + temp + ", " + ", #4294967296.0\n";
                                    releaseRegister(temp);
                                    generatedOperationCode.code = generatedOperationCode.code + "LBL VMILabel" + usedLabels +"\n";
                                    usedLabels++;

                                }
                            } else if (dstType == PrimitiveType.Double) {
                                generatedOperationCode.higherRegister = findNextViableRegister();
                                int temp = findNextViableRegister();
                                generatedOperationCode.code = generatedOperationCode.code + "MOV R" + temp + ", " + resultLowerRegister + ", 4\n";
                                generatedOperationCode.code = generatedOperationCode.code + "LSR R" + temp + ", #31" + ", 4\n";
                                generatedOperationCode.code = generatedOperationCode.code + "JIZ VMILabel" + usedLabels + ", R" + resultLowerRegister + ", 4\n";
                                generatedOperationCode.code = generatedOperationCode.code + "LOD R" + generatedOperationCode.higherRegister + ", #-1, 4\n";
                                generatedOperationCode.code = generatedOperationCode.code + "JMP VMILabel" + usedLabels + "\n";
                                generatedOperationCode.code = generatedOperationCode.code + "LBL VMILabel" + usedLabels +"\n";
                                usedLabels++;
                                generatedOperationCode.code = generatedOperationCode.code + "LOD R" + generatedOperationCode.higherRegister + ", #0, 4 + \n";
                                generatedOperationCode.code = generatedOperationCode.code + "LBL VMILabel" + usedLabels +"\n";
                                usedLabels++;
                            }
                        }
                        generatedOperationCode.result = "" + resultLowerRegister;
                    }
                }
            }
        }
        return generatedOperationCode;
    }

    public VMGeneratedOperationCode doAddition(String expr1, String expr2, ExprType exprType1, ExprType exprType2) {
        VMGeneratedOperationCode result = new VMGeneratedOperationCode();
        result.code = "";
        result.result = "";
        result.resultType = new ExprType();
        int resultLowerRegister = findNextViableRegister();
        result.result = "R" + resultLowerRegister;
        result.resultType.type = new ComplexType();
        result.resultType.type.type = PrimitiveType.Byte;
        result.resultType.type.dimensions = new ArrayList<>();
        result.resultType.storageType = ExprStorageType.RegisterStorage;
        if (exprType1.storageType == ExprStorageType.VariableStorage && exprType1.type.type == null || exprType2.storageType == ExprStorageType.VariableStorage && exprType2.type.type == null) {
            errors = errors + "Unrecognized type for variable, code was not generated, passing a random register as the expression result.\n";
            return result;
        }
        if (exprType1.type.dimensions.size() + exprType2.type.dimensions.size() > 0) {
            errors = errors + "Operator '+' is not defined for array types, code was not generated, passing a random register as the expression result.\n";
            return result;
        }
        int expr1TypeIndex = typeSuportingArithmatics.indexOf(exprType1.type.type);
        int expr2TypeIndex = typeSuportingArithmatics.indexOf(exprType2.type.type);
        if (expr2TypeIndex == -1 || expr1TypeIndex == -1) {
            if (expr1TypeIndex == -1)
                errors = errors + "Operator '+' is not defined for types " + exprType1.type.type.name + ", code was not generated, passing a random register as the expression result.\n";
            if (expr2TypeIndex == -1)
                errors = errors + "Operator '+' is not defined for types " + exprType2.type.type.name + ", code was not generated, passing a random register as the expression result.\n";
            return result;
        }
        result.resultType.type.type = typeSuportingArithmatics.get(Math.max(expr1TypeIndex, expr2TypeIndex));
        if (expr1TypeIndex < expr2TypeIndex) {

        }
        else if (expr2TypeIndex < expr1TypeIndex) {

        }
        return result;
    }

    public String getWarnings() {
        String temp = warnings;
        warnings = "";
        return temp;
    }

    public String getErrors() {
        String temp = errors;
        errors = "";
        return temp;
    }
}
