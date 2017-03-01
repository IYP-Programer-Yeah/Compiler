import scanner.ScannerSymbol;
import scanner.Scanner;

import java.io.*;

/**
 * Created by HoseinGhahremanzadeh on 2/21/2017.
 */
public class Main {
    public static void main(String[] args) {
        File sourceFile = new File("CLikeSample.clike");
        FileInputStream source = null;
        try {
            source = new FileInputStream(sourceFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Scanner scanner = new Scanner(new InputStreamReader(source));
        loop: while (true) {
            try {
                switch (scanner.yylex())
                {
                    case InvalidToken:
                        System.out.println("invalid token at line: " + scanner.getLine() + " column: " + scanner.getColumn());
                        break;
                    case EOF:
                        System.out.println("EOF");
                        break loop;
                    case StringConstant:
                        System.out.println("string constant:");
                        System.out.println(scanner.getToken());
                        break;
                    case StringMissingEndIndicator:
                        System.out.println("Missing \" at line: " + scanner.getLine() + " column: " + scanner.getColumn());
                        break;
                    case CharacterConstant:
                        System.out.println("character constant:");
                        System.out.println(scanner.getToken());
                        break;
                    case CharacterMissingEndIndicator:
                        System.out.println("Missing \' at line: " + scanner.getLine() + " column: " + scanner.getColumn());
                        break;
                    case CharacterTooLong:
                        System.out.println("Character too long at line: " + scanner.getLine() + " column: " + scanner.getColumn());
                        break;
                    case InvalidOctalCharacterLiteral:
                        System.out.println("Invalid octal literal at line: " + scanner.getLine() + " column: " + scanner.getColumn());
                        break;
                    case InvalidHexadecimalCharacterLiteral:
                        System.out.println("Invalid hexadecimal literal at line: " + scanner.getLine() + " column: " + scanner.getColumn());
                        break;
                    case InvalidUniversalUnicodeCharacterLiteral:
                        System.out.println("Invalid universal literal at line: " + scanner.getLine() + " column: " + scanner.getColumn());
                        break;
                    case InvalidEscapeSequence:
                        System.out.println("Invalid escape sequence at line: " + scanner.getLine() + " column: " + scanner.getColumn());
                        break;
                    case InvalidCharacter:
                        System.out.println("Invalid character at line: " + scanner.getLine() + " column: " + scanner.getColumn());
                        break;
                    case IntegerConstant:
                        System.out.println("Integer constant: ");
                        System.out.println(scanner.getToken());
                        System.out.println("of size: " + scanner.getIntegerSize());
                        break;
                    case IntegerConstantTooLong:
                        System.out.println("Integer constant too long at line: " + scanner.getLine() + " column: " + scanner.getColumn());
                        break;
                    case InvalidHexadecimalIntegerConstant:
                        System.out.println("Invalid hexadecimal integer constant at line: " + scanner.getLine() + " column: " + scanner.getColumn());
                        break;
                    case FloatConstant:
                        System.out.println("Float constant: ");
                        System.out.println(scanner.getFloatConstant());
                        break;
                    case DoubleConstant:
                        System.out.println("Double constant: ");
                        System.out.println(scanner.getDoubleConstant());
                        break;
                    case Comment:
                        System.out.println("Comment: ");
                        System.out.println(scanner.getToken());
                        break;
                    case CommentBlockEndIndicatorMissing:
                        System.out.println("Missing &% at line: " + scanner.getLine() + " column: " + scanner.getColumn());
                        break;
                    case Identifier:
                        System.out.println("Identifier: ");
                        System.out.println(scanner.getToken());
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
