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
    EOF
}
