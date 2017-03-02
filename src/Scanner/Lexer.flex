/*todo:
*1- delimeter
*/

package scanner;

import scanner.ScannerSymbol;
import scanner.IntegerSize;
import scanner.RealNumberSize;

%%
%class Scanner

%unicode
%line
%column
%public
%type ScannerSymbol
%{
	private String token;
	private StringBuffer stringConstant = new StringBuffer();
	private int characterSize;
	private int integerSize;
	private float floatConstant;
	private double doubleConstant;
	
	public String getToken() {
		return token;
	}
	
    public int getLine() {
        return yyline + 1;
    }
	
	public int getColumn() {
		return yycolumn + 1;
	}
	
	public int getCharacterSize() {
		return characterSize;
	}
	
	public int getIntegerSize() {
		return integerSize;
	}

	public double getFloatConstant() {
		return floatConstant;
	}
	
	public double getDoubleConstant() {
		return doubleConstant;
	}
	
	private static int CharArrToInt(String ch) {
		int intConst = 0;
		for (int i=0; i<ch.length(); i++) {
			intConst *=256;
			intConst += ch.charAt(i);
		}
		return (char)intConst;
    }
%}

WhiteSpace = \r|\n|\r\n|\t|" "
EOL = \r|\n|\r\n
HexadecimalDigit = [0-9a-fA-F]
OctalDigit = [0-7]
DecimalDigit = [0-9]

/***************************************************************************************/
/***************************************************************************************/
/*****************************String/Character Recognition******************************/
/***************************************************************************************/
/***************************************************************************************/
StringStartIndicator = \"
StringEndIndicator = \"
StringEscapeSequence = \\
StringConstant = !([^]*({StringEndIndicator}|{StringEscapeSequence}|{EOL})[^]*|"")




CharacterStartIndicator = \'
CharacterEndIndicator = \'
CharacterEscapeSequence = \\
CharacterConstant = !([^]*({CharacterEndIndicator}|{CharacterEscapeSequence}|{EOL})[^]*|"")


OctalCharacter = {OctalDigit}{1,3}
InvalidOctalCharacter = {DecimalDigit}{1,3}
HexadecimalCharacter = [x]{HexadecimalDigit}{HexadecimalDigit}?
UniversalUnicodeCharacterValue = ([u]{HexadecimalDigit}{4} | [U]{HexadecimalDigit}{8})
/***************************************************************************************/
/***************************************************************************************/
/*****************************String/Character Recognition******************************/
/***************************************************************************************/
/***************************************************************************************/

/***************************************************************************************/
/***************************************************************************************/
/*********************************Comment Recognition***********************************/
/***************************************************************************************/
/***************************************************************************************/
SingleLineCommentString = !([^]*{EOL}[^]*|"")
SingleLineCommentStartIndicator = "%%"

CommentBlockIndicatorEndCharacter = [%]
CommentBlockIndicatorMidharacter = [&]
CommentBlockIndicatorCharacters = {CommentBlockIndicatorMidharacter}|{CommentBlockIndicatorEndCharacter}
CommentBlockStartIndicator = {CommentBlockIndicatorEndCharacter}{CommentBlockIndicatorMidharacter}
CommnetBlockEndIndicator = {CommentBlockIndicatorMidharacter}{CommentBlockIndicatorEndCharacter}
CommentBlockString = !([^]*({CommentBlockIndicatorMidharacter}|{CommentBlockIndicatorEndCharacter})[^]*|"")
/***************************************************************************************/
/***************************************************************************************/
/*********************************Comment Recognition***********************************/
/***************************************************************************************/
/***************************************************************************************/

DecimalIntegerConstant = {DecimalDigit}+
HexadecimalIntegerConstant = "0x"{HexadecimalDigit}+
InvalidHexadecimalIntegerConstant = "0x"

RealNumber = ({DecimalDigit}*[.]{DecimalDigit}+)|({DecimalDigit}+[.]{DecimalDigit}*)
ScientificRepresentation = ({RealNumber}|{DecimalIntegerConstant})[eE][-+]?{DecimalIntegerConstant}
FloatConstant = ({RealNumber}|{ScientificRepresentation})[fF]
DoubleConstant = ({RealNumber}|{ScientificRepresentation})[dD]?

IdentifierDigitAndLetter = [:jletter::jdigit:]
IdentifierRepeatingBlock = [_]+{IdentifierDigitAndLetter}+
Identifier = ([:jletter:]+{IdentifierDigitAndLetter}*){IdentifierRepeatingBlock}*| {IdentifierRepeatingBlock}+


%state STRING
%state STRING_ESCAPE_SEQUENCE

%state CHARACTER
%state CHARACTER_ESCAPE_SEQUENCE

%state SINGLE_LINE_COMMENT
%state COMMENT_BLOCK

%%

<YYINITIAL> {
	{WhiteSpace} {}
	
	{StringStartIndicator} {characterSize = 8; stringConstant.setLength(0); yybegin(STRING);}
	u8{StringStartIndicator} {characterSize = 8; stringConstant.setLength(0); yybegin(STRING);}
	u{StringStartIndicator} {characterSize = 16; stringConstant.setLength(0); yybegin(STRING);}
	U{StringStartIndicator} {characterSize = 32; stringConstant.setLength(0); yybegin(STRING);}
	L{StringStartIndicator} {characterSize = 32; stringConstant.setLength(0); yybegin(STRING);}
	
	{CharacterStartIndicator} {characterSize = 8; stringConstant.setLength(0); yybegin(CHARACTER);}
	u8{CharacterStartIndicator} {characterSize = 8; stringConstant.setLength(0); yybegin(CHARACTER);}
	u{CharacterStartIndicator} {characterSize = 16; stringConstant.setLength(0); yybegin(CHARACTER);}
	U{CharacterStartIndicator} {characterSize = 32; stringConstant.setLength(0); yybegin(CHARACTER);}
	L{CharacterStartIndicator} {characterSize = 32; stringConstant.setLength(0); yybegin(CHARACTER);}
	
	{SingleLineCommentStartIndicator} {yybegin(SINGLE_LINE_COMMENT); token = "";}
	{CommentBlockStartIndicator} {yybegin(COMMENT_BLOCK); token = "";}
	
	{FloatConstant} {token = yytext(); floatConstant = Float.parseFloat(token); return ScannerSymbol.FloatConstant;}
	{DoubleConstant} {token = yytext(); doubleConstant = Double.parseDouble(token); return ScannerSymbol.DoubleConstant;}
	
	{HexadecimalIntegerConstant} {
		token = yytext();
		if ((token.length() - 2)*4 > IntegerSize.LongLongSize)
			return ScannerSymbol.IntegerConstantTooLong;
		if ((token.length() - 2)*4 > IntegerSize.LongSize)
			integerSize = IntegerSize.LongLongSize; 
		else if ((token.length() - 2)*4 <= IntegerSize.IntSize) 
			integerSize = IntegerSize.IntSize;
		else 
			integerSize = IntegerSize.LongSize; 
		return ScannerSymbol.IntegerConstant;
	}
	{InvalidHexadecimalIntegerConstant} {
		token = yytext();
		return ScannerSymbol.InvalidHexadecimalIntegerConstant;
	}
	{DecimalIntegerConstant} {
		token = yytext();
		if (token.length() > 20)
			return ScannerSymbol.IntegerConstantTooLong;
		if (token.length() > 19 || Long.parseLong(token) > ((long)1)<<IntegerSize.LongSize)
			integerSize = IntegerSize.LongLongSize; 
		else if (Long.parseLong(token) <= ((long)1)<<IntegerSize.IntSize) 
			integerSize = IntegerSize.IntSize;
		else 
			integerSize = IntegerSize.LongSize; 
		return ScannerSymbol.IntegerConstant;
	}
	"==" {return ScannerSymbol.Equal;}
	"!=" {return ScannerSymbol.NotEqual;}
	">=" {return ScannerSymbol.GreaterEqual;}
	"<=" {return ScannerSymbol.LessEqual;}
	">" {return ScannerSymbol.Greater;}
	"<" {return ScannerSymbol.Less;}
	"=" {return ScannerSymbol.Assignment;}
	"!" {return ScannerSymbol.LogicalNot;}
	"&&" {return ScannerSymbol.LogicalAnd;}
	"||" {return ScannerSymbol.LogicalOr;}
	"~" {return ScannerSymbol.BitwiseNot;}
	"&" {return ScannerSymbol.BitwiseAnd;}
	"|" {return ScannerSymbol.BitwiseOr;}
	"^" {return ScannerSymbol.BitwiseXor;}
	"++" {return ScannerSymbol.Increment;}
	"--" {return ScannerSymbol.Decrement;}
	"+" {return ScannerSymbol.Add;}
	"-" {return ScannerSymbol.Sub;}
	"*" {return ScannerSymbol.Multiplication;}
	"/" {return ScannerSymbol.Division;}
	"%" {return ScannerSymbol.Mode;}
	"." {return ScannerSymbol.Dot;}
	"," {return ScannerSymbol.Comma;}
	":" {return ScannerSymbol.Colon;}
	";" {return ScannerSymbol.SemiColon;}
	"{" {return ScannerSymbol.CurlyBraceOpen;}
	"}" {return ScannerSymbol.CurlyBraceClose;}
	"(" {return ScannerSymbol.ParenthesisOpen;}
	")" {return ScannerSymbol.ParenthesisClose;}
	"[" {return ScannerSymbol.BracketOpen;}
	"]" {return ScannerSymbol.BracketClose;}
	
	"bool" {return ScannerSymbol.Bool;}
	"break" {return ScannerSymbol.Break;}
	"byte" {return ScannerSymbol.Byte;}
	"case" {return ScannerSymbol.Case;}
	"char" {return ScannerSymbol.Char;}
	"char16" {return ScannerSymbol.Char;}
	"char32" {return ScannerSymbol.Char;}
	"const" {return ScannerSymbol.Const;}
	"continue" {return ScannerSymbol.Continue;}
	"default" {return ScannerSymbol.Default;}
	"do" {return ScannerSymbol.Do;}
	"double" {return ScannerSymbol.Double;}
	"else" {return ScannerSymbol.Else;}
	"extern" {return ScannerSymbol.Extern;}
	"false" {return ScannerSymbol.False;}
	"float" {return ScannerSymbol.Float;}
	"for" {return ScannerSymbol.For;}
	"goto" {return ScannerSymbol.Goto;}
	"if" {return ScannerSymbol.If;}
	"include" {return ScannerSymbol.Include;}
	"int" {return ScannerSymbol.Int;}
	"long" {return ScannerSymbol.Long;}
	"return" {return ScannerSymbol.Return;}
	"record" {return ScannerSymbol.Record;}
	"short" {return ScannerSymbol.Short;}
	"sizeof" {return ScannerSymbol.Sizeof;}
	"string" {return ScannerSymbol.String;}
	"switch" {return ScannerSymbol.Switch;}
	"true" {return ScannerSymbol.True;}
	"until" {return ScannerSymbol.Until;}
	"void" {return ScannerSymbol.Void;}
	"wchar_t" {return ScannerSymbol.Wchar;}
	"while" {return ScannerSymbol.While;}
	
	{Identifier} {token = yytext(); return ScannerSymbol.Identifier;}
	
	[^] {return ScannerSymbol.InvalidToken;}
	<<EOF>> {return ScannerSymbol.EOF;}
}

/***************************************************************************************/
/***************************************************************************************/
/*************************************String States*************************************/
/***************************************************************************************/
/***************************************************************************************/
<STRING> {
	{StringConstant} {stringConstant.append(yytext());}
	{StringEscapeSequence} {yybegin(STRING_ESCAPE_SEQUENCE);}
	{StringEndIndicator}{StringEndIndicator} {}
	{StringEndIndicator} {token = stringConstant.toString(); yybegin(YYINITIAL); return ScannerSymbol.StringConstant;}
	[^] {yybegin(YYINITIAL); return ScannerSymbol.StringMissingEndIndicator;}
	<<EOF>> {stringConstant.append((char)0); yybegin(YYINITIAL); return ScannerSymbol.StringMissingEndIndicator;}
}

<STRING_ESCAPE_SEQUENCE> {
	{StringEndIndicator} {stringConstant.append(yytext()); yybegin(STRING);}
	{StringEscapeSequence} {stringConstant.append(yytext()); yybegin(STRING);}
	['?] {stringConstant.append(yytext()); yybegin(STRING);}
	[n] {stringConstant.append("\n"); yybegin(STRING);}
	[t] {stringConstant.append("\t"); yybegin(STRING);}
	[v] {stringConstant.append((char)11); yybegin(STRING);}
	[b] {stringConstant.append("\b"); yybegin(STRING);}
	[r] {stringConstant.append("\r"); yybegin(STRING);}
	[f] {stringConstant.append("\f"); yybegin(STRING);}
	[a] {stringConstant.append((char)7); yybegin(STRING);}
	{EOL} {yybegin(STRING);}
	{OctalCharacter} {int tempCharacter = Integer.parseInt(yytext(),8); yybegin(STRING); if (tempCharacter < 256) stringConstant.append((char)tempCharacter); else return ScannerSymbol.InvalidOctalCharacterLiteral;}
	{HexadecimalCharacter} {stringConstant.append((char)Integer.parseInt(yytext().substring(1),16)); yybegin(STRING);}
	{UniversalUnicodeCharacterValue} {int num = Integer.parseInt(yytext().substring(1),16); yybegin(STRING); if(characterSize > 8 && ((long)num) >= ((long)1)<<characterSize) return ScannerSymbol.InvalidCharacter; stringConstant.append((char)num);}
	{InvalidOctalCharacter} {yybegin(STRING); return ScannerSymbol.InvalidOctalCharacterLiteral;}
	[x] {yybegin(STRING); return ScannerSymbol.InvalidHexadecimalCharacterLiteral;}
	[uU] {yybegin(STRING); return ScannerSymbol.InvalidUniversalUnicodeCharacterLiteral;}
	[^] {yybegin(STRING); return ScannerSymbol.InvalidEscapeSequence;}
}

/***************************************************************************************/
/***************************************************************************************/
/*************************************String States*************************************/
/***************************************************************************************/
/***************************************************************************************/


/***************************************************************************************/
/***************************************************************************************/
/***********************************Character States************************************/
/***************************************************************************************/
/***************************************************************************************/
<CHARACTER> {
	{CharacterConstant} {stringConstant.append(yytext());}
	{CharacterEscapeSequence} {yybegin(CHARACTER_ESCAPE_SEQUENCE);}
	{CharacterEndIndicator} {token = stringConstant.toString(); yybegin(YYINITIAL); if (token.length()>4)return ScannerSymbol.CharacterTooLong; if (token.length()==1) return ScannerSymbol.CharacterConstant; else {token = ""+CharArrToInt(token); integerSize = IntegerSize.IntSize; return ScannerSymbol.IntegerConstant;}}
	[^] {yybegin(YYINITIAL); return ScannerSymbol.CharacterMissingEndIndicator;}
	<<EOF>> {yybegin(YYINITIAL); return ScannerSymbol.CharacterMissingEndIndicator;}
}

<CHARACTER_ESCAPE_SEQUENCE> {
	{CharacterEndIndicator} {stringConstant.append(yytext()); yybegin(CHARACTER);}
	{CharacterEscapeSequence} {stringConstant.append(yytext()); yybegin(CHARACTER);}
	['?] {stringConstant.append(yytext()); yybegin(CHARACTER);}
	[n] {stringConstant.append("\n"); yybegin(CHARACTER);}
	[t] {stringConstant.append("\t"); yybegin(CHARACTER);}
	[v] {stringConstant.append((char)11); yybegin(CHARACTER);}
	[b] {stringConstant.append("\b"); yybegin(CHARACTER);}
	[r] {stringConstant.append("\r"); yybegin(CHARACTER);}
	[f] {stringConstant.append("\f"); yybegin(CHARACTER);}
	[a] {stringConstant.append((char)7); yybegin(CHARACTER);}
	{EOL} {yybegin(CHARACTER);}
	{OctalCharacter} {int tempCharacter = Integer.parseInt(yytext(),8); yybegin(CHARACTER); if (tempCharacter < 256) stringConstant.append((char)tempCharacter); else return ScannerSymbol.InvalidOctalCharacterLiteral;}
	{HexadecimalCharacter} {stringConstant.append((char)Integer.parseInt(yytext().substring(1),16)); yybegin(CHARACTER);}
	{UniversalUnicodeCharacterValue} {int num = Integer.parseInt(yytext().substring(1),16); yybegin(CHARACTER); if(characterSize > 8 && ((long)num) >= ((long)1)<<characterSize) return ScannerSymbol.InvalidCharacter; stringConstant.append((char)num);}
	{InvalidOctalCharacter} {yybegin(CHARACTER); return ScannerSymbol.InvalidOctalCharacterLiteral;}
	[x] {yybegin(CHARACTER); return ScannerSymbol.InvalidHexadecimalCharacterLiteral;}
	[uU] {yybegin(CHARACTER); return ScannerSymbol.InvalidUniversalUnicodeCharacterLiteral;}
	[^] {yybegin(CHARACTER); return ScannerSymbol.InvalidEscapeSequence;}
}

/***************************************************************************************/
/***************************************************************************************/
/***********************************Character States************************************/
/***************************************************************************************/
/***************************************************************************************/

<SINGLE_LINE_COMMENT> {
	{SingleLineCommentString} {token = yytext();}
	{EOL} {yybegin(YYINITIAL); return ScannerSymbol.Comment;}
	<<EOF>> {yybegin(YYINITIAL); return ScannerSymbol.Comment;}
}

<COMMENT_BLOCK> {
	{CommentBlockString} {token += yytext();}
	{CommnetBlockEndIndicator} {yybegin(YYINITIAL); return ScannerSymbol.Comment;}
	{CommentBlockIndicatorCharacters} {token += yytext();}
	<<EOF>> {yybegin(YYINITIAL); return ScannerSymbol.CommentBlockEndIndicatorMissing;}
}