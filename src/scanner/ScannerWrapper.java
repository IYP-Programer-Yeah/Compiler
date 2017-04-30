package scanner;

import java.io.*;

public class ScannerWrapper {
    private ScannerSymbol scannerSymbol;

    public Scanner getScanner() {
        return analyzer;
    }

    public int lineNumber = 1;
    Scanner analyzer;

    public ScannerWrapper(InputStream is) throws Exception {
        analyzer = new Scanner(new InputStreamReader(is));
    }

    public String NextToken() throws Exception {
        scannerSymbol = analyzer.yylex();
        while (scannerSymbol == ScannerSymbol.Comment)
            scannerSymbol = analyzer.yylex();
        return ScannerSymbol.getString(scannerSymbol);
    }

    public ScannerSymbol getScannerSymbol() {
        return scannerSymbol;
    }
}
