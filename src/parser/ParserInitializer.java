package parser;
// Should not be modified

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class ParserInitializer {

    public static void start(String nptPath, InputStream is, OutputStream os) {

//        if ( args.length != 2)
//        {
//            System.err.println("Wrong parameters passed.");
//            System.err.println("Use the following format:");
//            System.err.println("java Program inputfilename.L outputfilename.Lm");
//           return;
//        }
//        else
//        {
//	        inputPath = args[0];
//	        outputPath = args[1];
//        }

        String[] symbols = null;
        PTBlock[][] parseTable = null;

        if (!FileExists(nptPath)) {
            System.err.println("File not found: " + nptPath);
            return;
        }

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
                        System.err.println("Anomally in .npt file, skipping one line");
                        continue;
                    }

                    if (tmpArr.length != colSize * 3)
                        throw new Exception("Ivalid line in .npt file");
                    for (int j = 0; j < colSize; j++) {
                        block = new PTBlock();
                        block.setAct(Integer.parseInt((tmpArr[j * 3])));
                        block.setIndex(Integer.parseInt(tmpArr[j * 3 + 1]));
                        block.setSem(tmpArr[j * 3 + 2]);
                        parseTable[i][j] = block;
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception ex) {
            System.err.println("Compile Error -> " + ex.getMessage());
            return;
        }

        Parser parser = new Parser(is, symbols, parseTable);

        try {
            parser.Parse();
        } catch (Exception ex) {
            System.err.println("Compile Error -> " + ex.getMessage());
        }
        parser.WriteOutput(os);
    }

    static boolean FileExists(String path) {
        java.io.File f = new java.io.File(path);
        boolean b = f.exists();
        if (!b)
            System.err.println("ERROR: File not found: {0}" + path);

        return b;
    }
}
