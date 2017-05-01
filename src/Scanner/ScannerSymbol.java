package scanner;

/**
 * Created by HoseinGhahremanzadeh on 2/24/2017.
 */
public enum ScannerSymbol {
    InvalidCharacter, InvalidEscapeSequence, InvalidOctalCharacterLiteral, InvalidHexadecimalCharacterLiteral, InvalidUniversalUnicodeCharacterLiteral,
    StringMissingEndIndicator, StringConstant,
    CharacterTooLong, CharacterMissingEndIndicator,CharacterConstant,
    Comment, CommentBlockEndIndicatorMissing,
    InvalidHexadecimalIntegerConstant, IntegerConstantTooLong, IntegerConstant,
    FloatConstant,
    DoubleConstant,
    Identifier,
    Equal, NotEqual, GreaterEqual, LessEqual, Greater, Less, Assignment, LogicalNot, LogicalAnd, LogicalOr, BitwiseNot, BitwiseAnd, BitwiseOr, BitwiseXor, Increment, Decrement, Add, Sub, Multiplication, Division, Mode, Dot, Comma, Colon, SemiColon, CurlyBraceOpen, CurlyBraceClose, ParenthesisOpen, ParenthesisClose, BracketOpen, BracketClose,
    Bool, Break, Byte, Case, Char, Char16, Char32, Const, Continue, Default, Do, Double, Else, Extern, False, Float, For, Goto, If,Include,Int, Long, Return, Record, Short, Sizeof, String, Switch, True, Until, Void, Wchar, While,
    InvalidToken,
    EOF;

    public static String getString(ScannerSymbol token) {
        switch (token) {
            case InvalidToken:
                throw new RuntimeException("InvalidToken");
            case EOF:
                return "$";
            case StringConstant:
                return "string_literal";
            case StringMissingEndIndicator:
                throw new RuntimeException("StringMissingEndIndicator");
            case CharacterConstant:
                return "character_literal";
            case CharacterMissingEndIndicator:
                throw new RuntimeException("CharacterMissingEndIndicator");
            case CharacterTooLong:
                throw new RuntimeException("CharacterTooLong");
            case InvalidOctalCharacterLiteral:
                throw new RuntimeException("InvalidOctalCharacterLiteral");
            case InvalidHexadecimalCharacterLiteral:
                throw new RuntimeException("InvalidHexadecimalCharacterLiteral");
            case InvalidUniversalUnicodeCharacterLiteral:
                throw new RuntimeException("InvalidUniversalUnicodeCharacterLiteral");
            case InvalidEscapeSequence:
                throw new RuntimeException("InvalidEscapeSequence");
            case InvalidCharacter:
                throw new RuntimeException("InvalidCharacter");
            case IntegerConstant:
                return "integer_literal";
            case IntegerConstantTooLong:
                throw new RuntimeException("IntegerConstantTooLong");
            case InvalidHexadecimalIntegerConstant:
                throw new RuntimeException("InvalidHexadecimalIntegerConstant");
            case FloatConstant:
                return "float_literal";
            case DoubleConstant:
                return "double_literal";
            case Comment:
                return "comment";
            case CommentBlockEndIndicatorMissing:
                throw new RuntimeException("CommentBlockEndIndicatorMissing");
            case Identifier:
                return "id";
            case Equal:
                return "equal";
            case NotEqual:
                return "notequal";
            case GreaterEqual:
                return "greaterequal";
            case LessEqual:
                return "lessequal";
            case Greater:
                return "greater";
            case Less:
                return "less";
            case Assignment:
                return "assignment";
            case LogicalNot:
                return "logicalnot";
            case LogicalAnd:
                return "logicaland";
            case LogicalOr:
                return "logicalor";
            case BitwiseNot:
                return "bitwisenot";
            case BitwiseAnd:
                return "bitwiseand";
            case BitwiseOr:
                return "bitwiseor";
            case BitwiseXor:
                return "bitwisexor";
            case Increment:
                return "increment";
            case Decrement:
                return "decrement";
            case Add:
                return "add";
            case Sub:
                return "sub";
            case Multiplication:
                return "multiplication";
            case Division:
                return "division";
            case Mode:
                return "mode";
            case Dot:
                return "dot";
            case Comma:
                return "comma";
            case Colon:
                return "colon";
            case SemiColon:
                return "semicolon";
            case CurlyBraceOpen:
                return "curlybraceopen";
            case CurlyBraceClose:
                return "curlybraceclose";
            case ParenthesisOpen:
                return "parenthesisopen";
            case ParenthesisClose:
                return "parenthesisclose";
            case BracketOpen:
                return "bracketopen";
            case BracketClose:
                return "bracketclose";
            case Bool:
                return "bool";
            case Break:
                return "break";
            case Byte:
                return "byte";
            case Case:
                return "case";
            case Char:
                return "char";
            case Char16:
                return "char";
            case Char32:
                return "char";
            case Const:
                return "const";
            case Continue:
                return "continue";
            case Default:
                return "default";
            case Do:
                return "do";
            case Double:
                return "double";
            case Else:
                return "else";
            case Extern:
                return "extern";
            case False:
                return "false";
            case Float:
                return "float";
            case For:
                return "for";
            case Goto:
                return "goto";
            case If:
                return "if";
            case Include:
                return "include";
            case Int:
                return "int";
            case Long:
                return "long";
            case Return:
                return "return";
            case Record:
                return "record";
            case Short:
                return "short";
            case Sizeof:
                return "sizeof";
            case String:
                return "string";
            case Switch:
                return "switch";
            case True:
                return "true";
            case Until:
                return "until";
            case Void:
                return "void";
            case Wchar:
                return "char";
            case While:
                return "while";
        }
        return null;
    }
}
