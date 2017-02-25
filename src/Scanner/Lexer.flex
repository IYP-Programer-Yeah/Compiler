package Scanner;

import Scanner.ScannerSymbol;

%%
%class Scanner

%unicode
%line
%column
%type Scanner.ScannerSymbol
%{
	private String token;
	
	public String getToken() {
		return token;
	}
	
    public int getLine() {
        return yyline;
    }
%}

WhiteSpace = \r|\n|\r\n|\t|" "

%%

<YYINITIAL> {
	{WhiteSpace} {}
	<<EOF>> {return ScannerSymbol.EOF}
}