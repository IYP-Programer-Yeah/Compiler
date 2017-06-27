package cg;

import codeelements.Block;
import codeelements.FunctionDcl;
import codeelements.Label;
import codeelements.Type.ComplexType;
import codeelements.Type.PrimitiveType;
import codeelements.Type.Record;
import codeelements.Variable;
import parser.Parser;
import parser.ParserInitializer;
import scanner.CharacterType;
import scanner.ScannerSymbol;
import scanner.ScannerWrapper;

import java.io.*;
import java.util.*;
import java.util.function.Function;

public class CodeGenerator {
    final static boolean debugMode = true;

    static Stack<Object> objectStack = new Stack<>();
    private OutputStream log;
    private OutputStream bin;
    private String sourceName;
    private ScannerWrapper scanner; // This one way of informing CG about tokens detected by Scanner, you can do whatever you prefer

    public CodeGenerator(ScannerWrapper scanner, OutputStream log, OutputStream bin, String sourceName, boolean isMain) {
        this.sourceName = sourceName;
        this.scanner = scanner;
        this.log = log;
        this.bin = bin;
        this.sourceName = sourceName;
        includedFiles.add(sourceName);
        this.isMain = isMain;
    }


    private boolean isMain;

    private static String generatedCode = "";
    private static Stack<String> codeFragments = new Stack<>();

    private int errorCount = 0;




    private static Set<String> includedFiles = new HashSet<>();




    private static Variable currentVar = new Variable();//newed after every use

    private static Map<String, Record> records = new HashMap<>();
    private static Map<String, ArrayList<FunctionDcl>> functions = new HashMap<>();


    private FunctionDcl currentFunction = null;
    private static Block heap = new Block();

    private class PossibleForwardJump {
        String error;
        String placeHolder;
        long blockStackEnd;
        PossibleForwardJump (String placeHolder, String error, long blockStackEnd) {
            this.error = error;
            this.placeHolder = placeHolder;
            this.blockStackEnd = blockStackEnd;
        }
    }
    private LinkedList<PossibleForwardJump> possibleForwardJumps = new LinkedList<>();
    private boolean gotoSecondTry = false;

    private String integerSign = "";

    private ComplexType createType() {
        ArrayList<Object> brackets = (ArrayList<Object>) objectStack.pop();
        String typeName = (String) objectStack.pop();
        ComplexType complexType = new ComplexType();
        switch (typeName) {
            case "bool":
                complexType.type = PrimitiveType.Bool;
                break;
            case "byte":
                complexType.type = PrimitiveType.Byte;
                break;
            case "char":
                complexType.type = PrimitiveType.Char;
                break;
            case "string":
                complexType.type = PrimitiveType.String;
                break;
            case "int":
                complexType.type = PrimitiveType.Int;
                break;
            case "long":
                complexType.type = PrimitiveType.Long;
                break;
            case "long long":
                complexType.type = PrimitiveType.LongLong;
                break;
            case "float":
                complexType.type = PrimitiveType.Float;
                break;
            case "double":
                complexType.type = PrimitiveType.Double;
                break;
            case "void":
                complexType.type = PrimitiveType.Void;
                break;
            default:
                complexType.type = records.get(typeName);
                break;
        }
        if (complexType.type == null) {
            try {
                log.write(("File " + sourceName + ":\n\tCG Error #" + errorCount + ": Line " + scanner.getScanner().getLine() + " Column " + scanner.getScanner().getColumn() + ": ").getBytes());
                log.write(("Undefined type: " + typeName + "\n").getBytes());
                errorCount++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        complexType.dimensions = new ArrayList<>(brackets.size());
        long elementCount = 1L;
        for (int i = 0; i < brackets.size(); i++) {
            complexType.dimensions.add(Long.parseLong((String)brackets.get(i)));
            if (complexType.dimensions.get(i)<0) {
                try {
                    log.write(("File " + sourceName + ":\n\tCG Error #" + errorCount + ": Line " + scanner.getScanner().getLine() + " Column " + scanner.getScanner().getColumn() + ": ").getBytes());
                    log.write(("Negative array dimension\n").getBytes());
                    errorCount++;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            elementCount *= complexType.dimensions.get(i);
        }
        if (complexType.type!=null)
            complexType.size = elementCount * complexType.type.size;
        return complexType;
    }

    private void createVariable() {
        currentVar.name = (String)objectStack.pop();
        currentVar.type = createType();
    }


    public void doSemantic(String sem) {
        switch (sem) {
            case "@NegativeInteger": {
                integerSign = "-";
            }
                break;
            case "@PositiveInteger": {
                integerSign = "";
            }
                break;
            case "@IntegerPush": {
                ComplexType integerType = new ComplexType();
                integerType.dimensions = new ArrayList<>();
                integerType.size = Math.max(scanner.getScanner().getIntegerSize(), 32);
                integerType.type = integerType.size >32 ? PrimitiveType.LongLong : PrimitiveType.Int;
                objectStack.push(integerSign + scanner.getScanner().getToken());
                objectStack.push(integerType);
            }
                break;
            case "@IntegerPushPos": {
                ComplexType integerType = new ComplexType();
                integerType.dimensions = new ArrayList<>();
                integerType.size = Math.max(scanner.getScanner().getIntegerSize(), 32);
                integerType.type = integerType.size >32 ? PrimitiveType.LongLong : PrimitiveType.Int;
                objectStack.push(scanner.getScanner().getToken());
                objectStack.push(integerType);
            }
                break;
            case "@StringPush": {
                objectStack.push(scanner.getScanner().getToken());
                objectStack.push(scanner.getScanner().getCharacterSize());
                objectStack.push(scanner.getScanner().getCharacterType());
            }
                break;
            case "@Include": {
                CharacterType characterType = (CharacterType) objectStack.pop();
                long size = (long)objectStack.pop();
                String includeFilePath = (String)objectStack.pop();

                File sourceFile = new File(includeFilePath);
                FileInputStream source = null;
                try {
                    source = new FileInputStream(sourceFile);
                } catch (FileNotFoundException e) {
                    try {
                        log.write(("File " + sourceName + ":\n\tCG Error #" + errorCount + ": Line " + scanner.getScanner().getLine() + " Column " + scanner.getScanner().getColumn() + ": ").getBytes());
                        log.write(("Include file not found: " + includeFilePath + "\n").getBytes());
                        errorCount++;
                    } catch (Exception exception){

                    }
                    break;
                }
                if (!includedFiles.add(includeFilePath)){
                    try {
                        log.write(("File " + sourceName + ":\n\tCG Error #" + errorCount + ": Line " + scanner.getScanner().getLine() + " Column " + scanner.getScanner().getColumn() + ": ").getBytes());
                        log.write(("File already included, or there is a recursive include: " + includeFilePath + "\n").getBytes());
                        errorCount++;
                    } catch (Exception exception){
                    }
                    break;
                }
                Parser parser = ParserInitializer.createParser("parser.npt", source, log, bin, includeFilePath, false);
                parser.parse();
            }
                break;
            case "@Id": {
                objectStack.push(scanner.getScanner().getToken());
            }
                break;
            case "@VarConst": {
                currentVar.isConst = true;
            }
                break;
            case "@StartVarInit": {
                currentVar.inits = true;
                createVariable();
            }
                break;
            case "@StartVar": {
                currentVar.inits = false;
                createVariable();
            }
                break;
            case "@PushStartBrackets": {
                if (scanner.getScannerSymbol() == ScannerSymbol.Identifier)
                    objectStack.push(scanner.getScanner().getToken());
                else
                    objectStack.push(ScannerSymbol.getString(scanner.getScannerSymbol()));
                objectStack.push(new ArrayList<Object>());
            }
                break;
            case "@PushLongLong": {
                Object brackets = objectStack.pop();
                objectStack.pop();
                objectStack.push("long long");
                objectStack.push(brackets);
            }
                break;
            case "@PushBracket": {
                ComplexType integerType= (ComplexType)objectStack.pop();
                String integer = (String)objectStack.pop();
                ((ArrayList<Object>)objectStack.peek()).add(integer);
            }
                break;
            case "@PopBracket": {
                objectStack.pop();
            }
                break;
            case "@PutLabel": {
                Label label = new Label();
                label.name = (String)objectStack.pop();
                label.blockStackEnd = currentFunction.currentBlock.stackEnd;
                currentFunction.labels.put(label.name, label);
                currentFunction.bodyCode = currentFunction.bodyCode + "LBL " + label.name + (debugMode ? "\t\t //label code" : "") + "\n";
            }
                break;
            case "@Goto": {
                String label = null;
                if (!gotoSecondTry)
                    label = (String)objectStack.pop();
                if (currentFunction.labels.get(label) == null && !gotoSecondTry) {
                    possibleForwardJumps.add(new PossibleForwardJump("###Goto"+label,
                            "File " + sourceName + ":\n\tCG Error #" + errorCount + ": Line " + scanner.getScanner().getLine() + " Column " + scanner.getScanner().getColumn() + ": " + "Undefined label " + label + "\n",
                            currentFunction.currentBlock.stackEnd));
                    currentFunction.bodyCode = currentFunction.bodyCode + "###Goto"+label;
                    break;
                }

                if (gotoSecondTry) {
                    PossibleForwardJump possibleForwardJump = possibleForwardJumps.get(0);
                    label = possibleForwardJump.placeHolder.replace("###Goto", "");
                    if (currentFunction.labels.get(label) == null) {
                        try {
                            log.write(possibleForwardJump.error.getBytes());
                            errorCount++;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        currentFunction.bodyCode = currentFunction.bodyCode.replaceAll(possibleForwardJumps.remove(0).placeHolder, "");
                        return;
                    }
                }

                String gotoCode = "";

                long stackSizeDifference = (gotoSecondTry ? possibleForwardJumps.get(0).blockStackEnd :currentFunction.currentBlock.stackEnd) - currentFunction.labels.get(label).blockStackEnd;
                if (stackSizeDifference > 0)
                    gotoCode = gotoCode + "POP " + stackSizeDifference + (debugMode ? "\t\t //popping extra stack space for the goto destination block" : "") + "\n";
                if (stackSizeDifference < 0)
                    gotoCode = gotoCode + "PSH " + (-stackSizeDifference) + (debugMode ? "\t\t //pushing extra stack space for the goto destination block" : "") + "\n";
                gotoCode = gotoCode + "JMP " + label + (debugMode ? "\t\t //jump to goto destination" : "") + "\n";


                if (gotoSecondTry) {
                    currentFunction.bodyCode = currentFunction.bodyCode.replaceAll(possibleForwardJumps.remove(0).placeHolder, gotoCode);
                    return;
                } else
                    currentFunction.bodyCode = currentFunction.bodyCode + gotoCode;

            }
                break;
            case "@RecordStart": {
                objectStack.push(new ArrayList<Variable>());
            }
                break;
            case "@AddVarToRecord": {
                ((ArrayList<Variable>)objectStack.peek()).add(currentVar);
                currentVar = new Variable();//newed after every use
            }
                break;
            case "@RecordEnd": {
                ArrayList<Variable> vars = (ArrayList<Variable>)objectStack.pop();
                String name = (String)objectStack.pop();

                if (heap.getVariable(name) != null) {
                    try {
                        log.write(("File " + sourceName + ":\n\tCG Error #" + errorCount + ": Line " + scanner.getScanner().getLine() + " Column " + scanner.getScanner().getColumn() + ": ").getBytes());
                        log.write(("Record name already reserved by variable " + name + "\n").getBytes());
                        errorCount++;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }

                if (records.get(name) != null) {
                    try {
                        log.write(("File " + sourceName + ":\n\tCG Error #" + errorCount + ": Line " + scanner.getScanner().getLine() + " Column " + scanner.getScanner().getColumn() + ": ").getBytes());
                        log.write(("Redeclaration of record " + name + "\n").getBytes());
                        errorCount++;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }

                if (functions.get(name)!=null) {
                    try {
                        log.write(("File " + sourceName + ":\n\tCG Error #" + errorCount + ": Line " + scanner.getScanner().getLine() + " Column " + scanner.getScanner().getColumn() + ": ").getBytes());
                        log.write(("Record name already reserved by function " + name + "\n").getBytes());
                        errorCount++;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                records.put(name, new Record(name, vars));
            }
                break;
            case "@PushToSymbolTable": {
                if (records.get(currentVar.name)!=null) {
                    try {
                        log.write(("File " + sourceName + ":\n\tCG Error #" + errorCount + ": Line " + scanner.getScanner().getLine() + " Column " + scanner.getScanner().getColumn() + ": ").getBytes());
                        log.write(("Variable name already reserved by record " + currentVar.name + "\n").getBytes());
                        errorCount++;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                if (functions.get(currentVar.name)!=null) {
                    try {
                        log.write(("File " + sourceName + ":\n\tCG Error #" + errorCount + ": Line " + scanner.getScanner().getLine() + " Column " + scanner.getScanner().getColumn() + ": ").getBytes());
                        log.write(("Variable name already reserved by function " + currentVar.name + "\n").getBytes());
                        errorCount++;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                Block currentBlock;
                if (currentFunction!=null)
                    currentBlock = currentFunction.currentBlock;
                else
                    currentBlock = heap;
                if (currentBlock.symbolTable.get(currentVar.name) != null) {
                    try {
                        log.write(("File " + sourceName + ":\n\tCG Error #" + errorCount + ": Line " + scanner.getScanner().getLine() + " Column " + scanner.getScanner().getColumn() + ": ").getBytes());
                        log.write(("Redeclaration of variable " + currentVar.name + "\n").getBytes());
                        errorCount++;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                currentVar.startAddress = currentBlock.stackEnd;
                currentBlock.stackEnd += currentVar.type.size;
                currentBlock.symbolTable.put(currentVar.name, currentVar);
                if (currentFunction != null && currentVar.type != null)
                    currentFunction.bodyCode = currentFunction.bodyCode + "PSH " + currentVar.type.size + (debugMode ? "\t\t //pushing space for variable " + currentVar.name : "") + "\n";
                currentVar = new Variable();//newed after every use
            }
                break;
            case "@PushBlock": {
                Block temp = currentFunction.currentBlock;
                currentFunction.currentBlock = new Block();
                currentFunction.currentBlock.parent = temp;
                currentFunction.currentBlock.stackEnd = temp.stackEnd;
            }
                break;
            case "@PopBlock": {
                if (currentFunction.currentBlock.breakLabel != null) {
                    objectStack.push(currentFunction.currentBlock.breakLabel);
                    doSemantic("@PutLabel");
                }
                if (currentFunction.currentBlock.parent != null && currentFunction.currentBlock.parent.continueLabel != null){
                    objectStack.push(currentFunction.currentBlock.parent.continueLabel);
                    doSemantic("@PutLabel");
                }
                if ((currentFunction.currentBlock.stackEnd - currentFunction.currentBlock.parent.stackEnd) != 0)
                    currentFunction.bodyCode = currentFunction.bodyCode + "POP " + (currentFunction.currentBlock.stackEnd - currentFunction.currentBlock.parent.stackEnd) + (debugMode ? "\t\t //popping the local variables at the end of block" : "") + "\n";
                currentFunction.currentBlock = currentFunction.currentBlock.parent;
            }
                break;
            case "@FuncExternDcl": {
                currentFunction = new FunctionDcl();
                currentFunction.isExtern = true;
                currentFunction.name = (String)objectStack.pop();
                currentFunction.returnType = createType();
                ArrayList<FunctionDcl> externOnly = new ArrayList<>();
                externOnly.add(currentFunction);
                if (records.get(currentFunction.name)!=null) {
                    try {
                        log.write(("File " + sourceName + ":\n\tCG Error #" + errorCount + ": Line " + scanner.getScanner().getLine() + " Column " + scanner.getScanner().getColumn() + ": ").getBytes());
                        log.write(("Function name already reserved by record " + currentVar.name + "\n").getBytes());
                        errorCount++;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    currentFunction = null;
                    break;
                }
                if (heap.symbolTable.get(currentFunction.name)!=null) {
                    try {
                        log.write(("File " + sourceName + ":\n\tCG Error #" + errorCount + ": Line " + scanner.getScanner().getLine() + " Column " + scanner.getScanner().getColumn() + ": ").getBytes());
                        log.write(("Function name already reserved by variable " + currentVar.name + "\n").getBytes());
                        errorCount++;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    currentFunction = null;
                    break;
                }
                if (functions.get(currentFunction.name)!= null) {
                    try {
                        log.write(( "File " + sourceName + ":\n\tCG Error #" + errorCount + ": Line " + scanner.getScanner().getLine() + " Column " + scanner.getScanner().getColumn() + ": ").getBytes());
                        log.write(("Redeclaration of function " + currentFunction.name + "\n").getBytes());
                        errorCount++;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    currentFunction = null;
                    break;
                }
                functions.put(currentFunction.name, externOnly);
                currentFunction = null;
            }
                break;
            case "@CreateFunc": {
                currentFunction = new FunctionDcl();
                currentFunction.name = (String)objectStack.pop();
                currentFunction.returnType = createType();
            }
                break;
            case "@AddArgument": {
                Variable var = new Variable();
                var.name = (String)objectStack.pop();
                var.type = createType();
                var.inits = false;
                var.isConst = false;
                if (currentFunction.arguments.size() == 0)
                    var.startAddress = 0;
                else
                    var.startAddress = currentFunction.arguments.get(currentFunction.arguments.size() - 1).startAddress + currentFunction.arguments.get(currentFunction.arguments.size() - 1).type.size;
                currentFunction.arguments.add(var);
            }
                break;
            case "@CompleteFuncDcl": {
                FunctionDcl temp = currentFunction;
                if (functions.get(currentFunction.name)!= null && functions.get(currentFunction.name).contains(currentFunction)) {
                    currentFunction = functions.get(currentFunction.name).get(functions.get(currentFunction.name).indexOf(currentFunction));
                    currentFunction.bodyCode = temp.bodyCode + (debugMode ? "\t\t //function body finished\n" : "");
                    currentFunction.currentBlock = temp.currentBlock;
                    currentFunction.labels = temp.labels;
                }
                if (currentFunction.isComplete || currentFunction.isExtern) {
                    try {
                        log.write(( "File " + sourceName + ":\n\tCG Error #" + errorCount + ": Line " + scanner.getScanner().getLine() + " Column " + scanner.getScanner().getColumn() + ": ").getBytes());
                        log.write(("Redeclaration of function " + currentFunction.name + "\n").getBytes());
                        errorCount++;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    currentFunction = null;
                    break;
                }
                if (records.get(currentFunction.name)!=null) {
                    try {
                        log.write(("File " + sourceName + ":\n\tCG Error #" + errorCount + ": Line " + scanner.getScanner().getLine() + " Column " + scanner.getScanner().getColumn() + ": ").getBytes());
                        log.write(("Function name already reserved by record " + currentVar.name + "\n").getBytes());
                        errorCount++;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    currentFunction = null;
                    break;
                }
                if (heap.symbolTable.get(currentFunction.name)!=null) {
                    try {
                        log.write(("File " + sourceName + ":\n\tCG Error #" + errorCount + ": Line " + scanner.getScanner().getLine() + " Column " + scanner.getScanner().getColumn() + ": ").getBytes());
                        log.write(("Function name already reserved by variable " + currentVar.name + "\n").getBytes());
                        errorCount++;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    currentFunction = null;
                    break;
                }
                currentFunction.isComplete = true;
                if (functions.get(currentFunction.name) == null)
                    functions.put(currentFunction.name, new ArrayList<>());

                gotoSecondTry = true;
                while (possibleForwardJumps.size()!=0)
                    doSemantic("@Goto");

                gotoSecondTry = false;

                functions.get(currentFunction.name).add(currentFunction);
                generatedCode = generatedCode + "LBL " + currentFunction.name + System.identityHashCode(currentFunction) + (debugMode ? "\t\t //function label" : "") + "\n" + currentFunction.bodyCode;
                currentFunction = null;
            }
                break;
            case "@IncompleteFuncDcl": {
                if (functions.get(currentFunction.name)!= null && functions.get(currentFunction.name).contains(currentFunction)) {
                    currentFunction = null;
                    break;
                }
                if (currentFunction.isExtern) {
                    try {
                        log.write(( "File " + sourceName + ":\n\tCG Error #" + errorCount + ": Line " + scanner.getScanner().getLine() + " Column " + scanner.getScanner().getColumn() + ": ").getBytes());
                        log.write(("Redeclaration of function " + currentFunction.name + "\n").getBytes());
                        errorCount++;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    currentFunction = null;
                    break;
                }
                if (records.get(currentFunction.name)!=null) {
                    try {
                        log.write(("File " + sourceName + ":\n\tCG Error #" + errorCount + ": Line " + scanner.getScanner().getLine() + " Column " + scanner.getScanner().getColumn() + ": ").getBytes());
                        log.write(("Function name already reserved by record " + currentFunction.name + "\n").getBytes());
                        errorCount++;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    currentFunction = null;
                    break;
                }
                if (heap.symbolTable.get(currentFunction.name)!=null) {
                    try {
                        log.write(("File " + sourceName + ":\n\tCG Error #" + errorCount + ": Line " + scanner.getScanner().getLine() + " Column " + scanner.getScanner().getColumn() + ": ").getBytes());
                        log.write(("Function name already reserved by variable " + currentFunction.name + "\n").getBytes());
                        errorCount++;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    currentFunction = null;
                    break;
                }
                functions.put(currentFunction.name, new ArrayList<>());
                functions.get(currentFunction.name).add(currentFunction);
                currentFunction = null;
            }
                break;
            case "@IncompleteFuncSignDcl": {
                if (functions.get(currentFunction.name)!= null && functions.get(currentFunction.name).contains(currentFunction))
                    break;
                if (currentFunction.isExtern)
                    break;
                if (records.get(currentFunction.name)!=null)
                    break;
                if (heap.symbolTable.get(currentFunction.name)!=null)
                    break;
                functions.put(currentFunction.name, new ArrayList<>());
                functions.get(currentFunction.name).add(currentFunction);
            }
                break;
            case "@PutBreakLabel": {
                currentFunction.currentBlock.breakLabel = "BreakLabel" + System.identityHashCode(currentFunction.currentBlock);
            }
                break;
            case "@PutContinueLabel": {
                currentFunction.currentBlock.continueLabel = "ContinueLabel" + System.identityHashCode(currentFunction.currentBlock);
            }
                break;
            case "@PutBreakJMP": {
                objectStack.push(currentFunction.currentBlock.getBreakLabel());
                doSemantic("@Goto");
            }
                break;
            case "@PutContinueJMP": {
                objectStack.push(currentFunction.currentBlock.getContinueLabel());
                doSemantic("@Goto");
            }
            break;
            default: {
                if (sem.contains(";")) {
                    sem = sem.replace("@","");
                    String[] microSems = sem.split("[;]");
                    for (String microSem : microSems)
                        doSemantic("@"+microSem);
                    break;
                }
                System.err.println(sem);
            }
                break;
        }


        switch (scanner.getScannerSymbol()) {
            case InvalidToken:
                System.out.println("invalid token at line: " + scanner.getScanner().getLine() + " column: " + scanner.getScanner().getColumn());
                break;
            case EOF:
                System.out.println("EOF");
                break;
            case StringConstant:
                System.out.println("string constant:");
                System.out.println(scanner.getScanner().getToken());
                break;
            case StringMissingEndIndicator:
                System.out.println("Missing \" at line: " + scanner.getScanner().getLine() + " column: " + scanner.getScanner().getColumn());
                break;
            case CharacterConstant:
                System.out.println("character constant:");
                System.out.println(scanner.getScanner().getToken());
                break;
            case CharacterMissingEndIndicator:
                System.out.println("Missing \' at line: " + scanner.getScanner().getLine() + " column: " + scanner.getScanner().getColumn());
                break;
            case CharacterTooLong:
                System.out.println("Character too long at line: " + scanner.getScanner().getLine() + " column: " + scanner.getScanner().getColumn());
                break;
            case InvalidOctalCharacterLiteral:
                System.out.println("Invalid octal literal at line: " + scanner.getScanner().getLine() + " column: " + scanner.getScanner().getColumn());
                break;
            case InvalidHexadecimalCharacterLiteral:
                System.out.println("Invalid hexadecimal literal at line: " + scanner.getScanner().getLine() + " column: " + scanner.getScanner().getColumn());
                break;
            case InvalidUniversalUnicodeCharacterLiteral:
                System.out.println("Invalid universal literal at line: " + scanner.getScanner().getLine() + " column: " + scanner.getScanner().getColumn());
                break;
            case InvalidEscapeSequence:
                System.out.println("Invalid escape sequence at line: " + scanner.getScanner().getLine() + " column: " + scanner.getScanner().getColumn());
                break;
            case InvalidCharacter:
                System.out.println("Invalid character at line: " + scanner.getScanner().getLine() + " column: " + scanner.getScanner().getColumn());
                break;
            case IntegerConstant:
                System.out.println("Integer constant: ");
                System.out.println(scanner.getScanner().getToken());
                System.out.println("of size: " + scanner.getScanner().getIntegerSize());
                break;
            case IntegerConstantTooLong:
                System.out.println("Integer constant too long at line: " + scanner.getScanner().getLine() + " column: " + scanner.getScanner().getColumn());
                break;
            case InvalidHexadecimalIntegerConstant:
                System.out.println("Invalid hexadecimal integer constant at line: " + scanner.getScanner().getLine() + " column: " + scanner.getScanner().getColumn());
                break;
            case FloatConstant:
                System.out.println("Float constant: ");
                System.out.println(scanner.getScanner().getFloatConstant());
                break;
            case DoubleConstant:
                System.out.println("Double constant: ");
                System.out.println(scanner.getScanner().getDoubleConstant());
                break;
            case Comment:
                System.out.println("Comment: ");
                System.out.println(scanner.getScanner().getToken());
                break;
            case CommentBlockEndIndicatorMissing:
                System.out.println("Missing &% at line: " + scanner.getScanner().getLine() + " column: " + scanner.getScanner().getColumn());
                break;
            case Identifier:
                System.out.println("Identifier: ");
                System.out.println(scanner.getScanner().getToken());
                break;
            case Equal:
                System.out.println("Equal");
                break;
            case NotEqual:
                System.out.println("NotEqual");
                break;
            case GreaterEqual:
                System.out.println("GreaterEqual");
                break;
            case LessEqual:
                System.out.println("LessEqual");
                break;
            case Greater:
                System.out.println("Greater");
                break;
            case Less:
                System.out.println("Less");
                break;
            case Assignment:
                System.out.println("Assignment");
                break;
            case LogicalNot:
                System.out.println("LogicalNot");
                break;
            case LogicalAnd:
                System.out.println("LogicalAnd");
                break;
            case LogicalOr:
                System.out.println("LogicalOr");
                break;
            case BitwiseNot:
                System.out.println("BitwiseNot");
                break;
            case BitwiseAnd:
                System.out.println("BitwiseAnd");
                break;
            case BitwiseOr:
                System.out.println("BitwiseOr");
                break;
            case BitwiseXor:
                System.out.println("BitwiseXor");
                break;
            case Increment:
                System.out.println("Increment");
                break;
            case Decrement:
                System.out.println("Decrement");
                break;
            case Add:
                System.out.println("Add");
                break;
            case Sub:
                System.out.println("Sub");
                break;
            case Multiplication:
                System.out.println("Multiplication");
                break;
            case Division:
                System.out.println("Division");
                break;
            case Mode:
                System.out.println("Mode");
                break;
            case Dot:
                System.out.println("Dot");
                break;
            case Comma:
                System.out.println("Comma");
                break;
            case Colon:
                System.out.println("Colon");
                break;
            case SemiColon:
                System.out.println("SemiColon");
                break;
            case CurlyBraceOpen:
                System.out.println("CurlyBraceOpen");
                break;
            case CurlyBraceClose:
                System.out.println("CurlyBraceClose");
                break;
            case ParenthesisOpen:
                System.out.println("ParenthesisOpen");
                break;
            case ParenthesisClose:
                System.out.println("ParenthesisClose");
                break;
            case BracketOpen:
                System.out.println("BracketOpen");
                break;
            case BracketClose:
                System.out.println("BracketClose");
                break;
            case Bool:
                System.out.println("bool");
                break;
            case Break:
                System.out.println("break");
                break;
            case Byte:
                System.out.println("byte");
                break;
            case Case:
                System.out.println("case");
                break;
            case Char:
                System.out.println("char");
                break;
            case Char16:
                System.out.println("char16_t");
                break;
            case Char32:
                System.out.println("char32_t");
                break;
            case Const:
                System.out.println("const");
                break;
            case Continue:
                System.out.println("continue");
                break;
            case Default:
                System.out.println("default");
                break;
            case Do:
                System.out.println("do");
                break;
            case Double:
                System.out.println("double");
                break;
            case Else:
                System.out.println("else");
                break;
            case Extern:
                System.out.println("extern");
                break;
            case False:
                System.out.println("false");
                break;
            case Float:
                System.out.println("float");
                break;
            case For:
                System.out.println("for");
                break;
            case Goto:
                System.out.println("goto");
                break;
            case If:
                System.out.println("if");
                break;
            case Include:
                System.out.println("include");
                break;
            case Int:
                System.out.println("int");
                break;
            case Long:
                System.out.println("long");
                break;
            case Return:
                System.out.println("return");
                break;
            case Record:
                System.out.println("record");
                break;
            case Short:
                System.out.println("short");
                break;
            case Sizeof:
                System.out.println("sizeof");
                break;
            case String:
                System.out.println("string");
                break;
            case Switch:
                System.out.println("switch");
                break;
            case True:
                System.out.println("true");
                break;
            case Until:
                System.out.println("until");
                break;
            case Void:
                System.out.println("void");
                break;
            case Wchar:
                System.out.println("wchar_t");
                break;
            case While:
                System.out.println("while");
                break;
        }

        if (sem.equals("NoSem"))
            return;
    }

    public void FinishCode(int errorCount) {
        if (isMain) {
            FunctionDcl entryPoint = new FunctionDcl();
            entryPoint.returnType = new ComplexType();
            entryPoint.returnType.type = PrimitiveType.Int;
            entryPoint.returnType.dimensions = new ArrayList<>();
            entryPoint.returnType.size = PrimitiveType.Int.size;
            entryPoint.name = "start";

            ArrayList<FunctionDcl> startFunctions = functions.get("start");

            if (!startFunctions.contains(entryPoint)) {
                try {
                    log.write(("File " + sourceName + ":\n\tCG Error #" + errorCount + ": Line " + scanner.getScanner().getLine() + " Column " + scanner.getScanner().getColumn() + ": ").getBytes());
                    log.write(("No entry point found\n").getBytes());
                    errorCount++;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                entryPoint = startFunctions.get(startFunctions.indexOf(entryPoint));
                if (entryPoint.isComplete || entryPoint.isExtern) {
                    entryPoint.isUsed = true;
                    String entryPointCode ="PSH " + Long.toString(entryPoint.returnType.size / 8) + (debugMode ? "\t\t //push the space for return value" : "") + "\n" +
                        "LNK " + entryPoint.name + (entryPoint.isExtern ? "" : "" + System.identityHashCode(entryPoint)) + (debugMode ? "\t\t //call the start function" : "") + "\n" +
                            "POP R0," + (entryPoint.returnType.size / 8) + (debugMode ? "\t\t //popping the return value" : "") + "\n" +
                                "TRM R0" + (debugMode ? "\t\t //terminate the program by the return value of the start function" : "") + "\n";

                    generatedCode = entryPointCode + generatedCode;

                } else {
                    try {
                        log.write(("File " + sourceName + ":\n\tCG Error #" + errorCount + ": Line " + scanner.getScanner().getLine() + " Column " + scanner.getScanner().getColumn() + ": ").getBytes());
                        log.write(("No entry point found\n").getBytes());
                        errorCount++;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                bin.write(generatedCode.getBytes());
                bin.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            log.write(("File " + sourceName + ":\n\tCompilation finished with " + (this.errorCount + errorCount) + " error(s).\n").getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void WriteOutput(OutputStream bin) {
    }
}
