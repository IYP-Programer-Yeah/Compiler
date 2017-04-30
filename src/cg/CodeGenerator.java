package cg;

import scanner.ScannerWrapper;

import java.io.OutputStream;

public class CodeGenerator {
    ScannerWrapper scanner; // This was my way of informing CG about Constant Values detected by Scanner, you can do whatever you like


    public CodeGenerator(ScannerWrapper scanner) {
        this.scanner = scanner;

    }

    public void Generate(String sem) {

        switch (scanner.getScannerSymbol())
        {
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

    public void FinishCode() {
    }

    public void WriteOutput(OutputStream out) {
    }
}
