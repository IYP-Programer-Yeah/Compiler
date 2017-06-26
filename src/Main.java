import parser.Parser;
import parser.ParserInitializer;

import java.io.*;

/**
 * Created by HoseinGhahremanzadeh on 2/21/2017.
 */
public class Main {
    public static void main(String[] args) {

        String logDir = "Log.txt";
        String binDir = "Out.bin";
        String sourceDir = "lexer-sample-input.clike";
        for (int i=0;i<args.length;i++) {
            if (args[i].toLowerCase().equals("-log") && i < args.length - 1)
                logDir = args[i + 1];
            if (args[i].toLowerCase().equals("-src") && i < args.length - 1)
                sourceDir = args[i + 1];
            if (args[i].toLowerCase().equals("-bin") && i < args.length - 1)
                binDir = args[i + 1];
        }

        File sourceFile = new File(sourceDir);
        File logFile = new File(logDir);
        File binFile = new File(binDir);
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
        FileOutputStream bin = null;
        try {
            bin = new FileOutputStream(binFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Parser parser = ParserInitializer.createParser("parser.npt", source, log, bin, sourceDir, true);
        parser.parse();
    }
}
