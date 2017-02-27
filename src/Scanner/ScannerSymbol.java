package scanner;

/**
 * Created by HoseinGhahremanzadeh on 2/24/2017.
 */
public enum ScannerSymbol {
    InvalidCharacter, InvalidEscapeSequence, InvalidOctalCharacterLiteral, InvalidHexadecimalCharacterLiteral, InvalidUniversalUnicodeCharacterLiteral,
    StringMissingEndIndicator, StringConstant,
    CharacterTooLong, CharacterMissingEndIndicator,CharacterConstant,
    Comment, CommentBlockEndIndicatorMissing,
    IntegerConstant,
    InvalidToken,
    EOF
}
