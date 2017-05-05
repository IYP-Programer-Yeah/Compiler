import parser.Parser;
import parser.ParserInitializer;

import java.io.*;

/**
 * Created by HoseinGhahremanzadeh on 2/21/2017.
 */
public class Main {
    public static void main(String[] args) {
        File sourceFile = new File("lexer-sample-input.clike");
        File logFile = new File("Log.txt");
        FileInputStream source = null;
        try {
            source = new FileInputStream(sourceFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        FileOutputStream log = null;
        try {
            log = new FileOutputStream(logFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Parser parser = ParserInitializer.createParser("parser.npt", source, log);
        parser.parse();
    }
}
