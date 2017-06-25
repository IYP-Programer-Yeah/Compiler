package cg;

import codeelements.Block;
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

public class CodeGenerator {
    static Stack<Object> objectStack = new Stack<>();
    private OutputStream log;
    private OutputStream bin;
    private String sourceName;
    ScannerWrapper scanner; // This one way of informing CG about tokens detected by Scanner, you can do whatever you prefer

    public CodeGenerator(ScannerWrapper scanner, OutputStream log, OutputStream bin, String sourceName) {
        this.sourceName = sourceName;
        this.scanner = scanner;
        this.log = log;
        this.bin = bin;
        this.sourceName = sourceName;
        includedFiles.add(sourceName);
    }


    private int errorCount = 0;

    private static Set<String> includedFiles = new HashSet<>();
    private static Variable currentVar = new Variable();//newed after every use

    private static Map<String, Record> records = new HashMap<>();
    private static Block currentBlock = new Block();

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
                objectStack.push(integerSign + scanner.getScanner().getToken());
                objectStack.push(scanner.getScanner().getIntegerSize());
            }
                break;
            case "@IntegerPushPos": {
                objectStack.push(scanner.getScanner().getToken());
                objectStack.push(scanner.getScanner().getIntegerSize());
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
                Parser parser = ParserInitializer.createParser("parser.npt", source, log, bin, includeFilePath);
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
                long integerSize = (Long)objectStack.pop();
                String integer = (String)objectStack.pop();
                ((ArrayList<Object>)objectStack.peek()).add(integer);
            }
                break;
            case "@PopBracket": {
                objectStack.pop();
            }
                break;
            case "@PopBracketsPutLabel": {
                objectStack.pop();
                /*todo
                * 1- add code generation for label*/
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

                if (currentBlock.getVariable(name) != null) {
                    try {
                        log.write(("File " + sourceName + ":\n\tCG Error #" + errorCount + ": Line " + scanner.getScanner().getLine() + " Column " + scanner.getScanner().getColumn() + ": ").getBytes());
                        log.write(("Record name already reserved by variable " + currentVar.name + "\n").getBytes());
                        errorCount++;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (records.put(name, new Record(name, vars)) != null) {
                    try {
                        log.write(("File " + sourceName + ":\n\tCG Error #" + errorCount + ": Line " + scanner.getScanner().getLine() + " Column " + scanner.getScanner().getColumn() + ": ").getBytes());
                        log.write(("Redeclaration of record " + currentVar.name + "\n").getBytes());
                        errorCount++;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
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
                }
                currentVar.startAddress = currentBlock.stackEnd;
                currentBlock.stackEnd += currentVar.type.size;
                if (currentBlock.symbolTable.put(currentVar.name, currentVar) != null) {
                    try {
                        log.write(("File " + sourceName + ":\n\tCG Error #" + errorCount + ": Line " + scanner.getScanner().getLine() + " Column " + scanner.getScanner().getColumn() + ": ").getBytes());
                        log.write(("Redeclaration of variable " + currentVar.name + "\n").getBytes());
                        errorCount++;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                currentVar = new Variable();//newed after every use
            }
                break;
            case "@PushBlock": {
                Block temp = currentBlock;
                currentBlock = new Block();
                currentBlock.parent = temp;
                currentBlock.stackEnd = temp.stackEnd;
            }
                break;
            case "@PopBlock": {
                currentBlock = currentBlock.parent;
            }
                break;
            default:
                System.err.println(sem);
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
        try {
            log.write(("File " + sourceName + ":\n\tCompilation finished with " + (this.errorCount + errorCount) + " error(s).\n").getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void WriteOutput(OutputStream bin) {
    }
}
