package parser;
// Should not be modified

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class ParserInitializer {

    public static Parser createParser(String nptPath, InputStream is, OutputStream log, OutputStream bin, String sourceName, boolean isMain) {

        String[] symbols = null;
        PTBlock[][] parseTable = null;

        if (!FileExists(nptPath))    // prints error, if any
        {
            return null;
        }

        // loading parse table...
        try {
            int rowSize, colSize;
            String[] tmpArr;
            PTBlock block;

            try {
                java.util.Scanner sc = new java.util.Scanner(new FileInputStream(nptPath));

                tmpArr = sc.nextLine().trim().split(" ");
                rowSize = Integer.parseInt(tmpArr[0]);
                colSize = Integer.parseInt(tmpArr[1]);

                String SL = sc.nextLine();
                // This is the line creates an array of symbols depending on the parse table read.
                symbols = SL.trim().split(" +");

                parseTable = new PTBlock[rowSize][colSize];
                for (int i = 0; sc.hasNext(); i++) {
                    tmpArr = sc.nextLine().trim().split(" ");

                    //PGen generates some unused rows!
                    if (tmpArr.length == 1) {
                        System.err.println("Parser Initializing Warning -> Anomaly in .npt file, skipping one line");
                        continue;
                    }

                    if (tmpArr.length != colSize * 3)
                        throw new Exception("Invalid .npt file");

                    for (int j = 0; j < colSize; j++) {
                        block = new PTBlock();
                        block.setAct(Integer.parseInt((tmpArr[j * 3])));
                        block.setIndex(Integer.parseInt(tmpArr[j * 3 + 1]));
                        block.setSem(tmpArr[j * 3 + 2]);
                        parseTable[i][j] = block;
                    }

                }
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                throw new Exception("Invalid .npt file");
            }

        } catch (Exception ex) {
            System.err.println("Parser Initializing Error -> " + ex.getMessage());
            return null;
        }

        return new Parser(is, log, bin, symbols, parseTable, sourceName, isMain);
    }

    static boolean FileExists(String path) {
        java.io.File f = new java.io.File(path);
        boolean b = f.exists();
        if (!b)
            System.err.println("ERROR: file " + path + " not found.");

        return b;
    }
}
