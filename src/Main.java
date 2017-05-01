import parser.Parser;
import parser.ParserInitializer;

import java.io.*;

/**
 * Created by HoseinGhahremanzadeh on 2/21/2017.
 */
public class Main {
    public static void main(String[] args) {
        File sourceFile = new File("lexer-sample-input.clike");
        FileInputStream source = null;
        try {
            source = new FileInputStream(sourceFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Parser parser = ParserInitializer.createParser("parser.npt", source, System.out);
        parser.parse();
    }
}
