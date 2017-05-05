package parser;

import cg.CodeGenerator;
import parser.tree.Tree;
import scanner.ScannerWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Stack;

public class Parser {
	ScannerWrapper scanner;    // *** THIS IS NOT java.util.Scanner ***
	OutputStream os;
	CodeGenerator cg;
	PTBlock[][] parseTable;    // a 2D array of blocks, forming a parse table
	Stack<Integer> parseStack = new Stack<Integer>();
	String[] symbols;
    int symbolInLine = -1;
    int symbolInlineUseCount = 0;
    int errorCount = 0;

    Tree parseTree = new Tree("MAIN");
	/**
	 * Creates a new parser
	 *
	 * @param is         input stream from source text file
	 * @param os         output stream to write the output there (if any)
	 * @param symbols    symbols known by parser (tokens + graph nodes)
	 * @param parseTable all of the actions describing the parser behaviour
	 */
	public Parser(InputStream is, OutputStream os, String[] symbols, PTBlock[][] parseTable) {
		try {
			this.parseTable = parseTable;
			this.symbols = symbols;
			scanner = new ScannerWrapper(is);
			this.os = os;
			cg = new CodeGenerator(scanner, os);
		} catch (Exception e) {
			System.err.println("Parsing Error -> IOException at opening input stream");
		}
	}

	/**
	 * All the parsing operations is here.
	 * operations were defined in .npt file, and now they are loaded into parseTable
	 */
	public void parse() {
		try {
            int tokenID = nextTokenID();
			int currentNode = 0;   // start node
			boolean accepted = false;   // is input accepted by parser?
			while (!accepted) {
				// current token's text
				String tokenText = symbols[tokenID];
				// current block of parse table
				PTBlock ptb = parseTable[currentNode /* the node that parser is there */][tokenID /* the token that parser is receiving at current node */];

				switch (ptb.getAct()) {
					case PTBlock.ActionType.Error: {
                        errorCount++;
                        ArrayList<Integer> errorSolutions = new ArrayList<>();

                        os.write(("Error #" + errorCount + ": Line " + scanner.getScanner().getLine() + " Column " + scanner.getScanner().getColumn() + ": ").getBytes());
                        os.write(("Expected ").getBytes());
                        boolean first = true;
                        for (int i = 0; i < symbols.length; i++) {
                            PTBlock nextBlock = parseTable[currentNode][i];
                            if (nextBlock.getAct() != PTBlock.ActionType.Error && nextBlock.getAct() != PTBlock.ActionType.Goto) {
                                if (!first)
                                    os.write((", ").getBytes());
                                first = false;
                                os.write((symbols[i]).getBytes());
                                errorSolutions.add(i);
                                if (symbols[i].equals("semicolon"))
                                    break;
                            }
                        }
                        os.write((". Found " + tokenText + " instead.\n").getBytes());


                        if (errorSolutions.size() != 0) {
                            symbolInLine = tokenID;
                            tokenID = errorSolutions.get(errorSolutions.size() - 1);
                        }
                    }
                    break;
					case PTBlock.ActionType.Shift: {
                        parseTree.children.add(new Tree(symbols[tokenID]));
                        parseTree.children.getLast().parent = parseTree;
                        cg.doSemantic(ptb.getSem());
						tokenID = nextTokenID();
						currentNode = ptb.getIndex();  // index is pointing to Shift location for next node

					}
					break;

					case PTBlock.ActionType.Goto: {
                        //parseTree.childeren.add(new Tree(symbols[tokenID]));
						cg.doSemantic(ptb.getSem());
						currentNode = ptb.getIndex();  // index is pointing to Goto location for next node
					}
					break;

					case PTBlock.ActionType.PushGoto: {
                        parseTree.children.add(new Tree(""));
                        parseTree.children.getLast().parent = parseTree;
                        parseTree = parseTree.children.getLast();
						parseStack.push(currentNode);
						currentNode = ptb.getIndex();  // index is pointing to Goto location for next node
					}
					break;

					case PTBlock.ActionType.Reduce: {
						if (parseStack.size() == 0) {
							throw new Exception("Compile Error trying to Reduce(Return) at token \"" + tokenText + "\" at line " + scanner.lineNumber + " ; node@" + currentNode);
						}

						int graphToken = ptb.getIndex();    // index is the graphToken to be returned
						int preNode = parseStack.pop();     // last stored node in the parse stack
                        parseTree.head = symbols[graphToken];
                        parseTree = parseTree.parent;
						cg.doSemantic(parseTable[preNode][graphToken].getSem());
						currentNode = parseTable[preNode][graphToken].getIndex(); // index is pointing to Goto location for next node
					}
					break;

					case PTBlock.ActionType.Accept: {
						accepted = true;
					}
					break;

				}
			}
			cg.FinishCode(errorCount);
		} catch (Exception e) {
			//System.err.println(e.getMessage());
            e.printStackTrace();
		}
        try {
            os.write("\n".getBytes());
            char[][] tree = parseTree.draw();
            for (int i=0;i<tree.length; i++) {
                os.write("\n".getBytes());
                for (int j = 0; j < tree[0].length; j++)
                    os.write(tree[i][j]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	private int nextTokenID() throws Exception {
        if (symbolInLine != -1 && symbolInlineUseCount < 4) {
            symbolInlineUseCount++;
            int temp = symbolInLine;
            symbolInLine = -1;
            return temp;
        }
        symbolInLine = -1;
        symbolInlineUseCount = 0;
		String t = null;
		try {
			t = scanner.NextToken();
		} catch (Exception e) {
			os.write(e.getMessage().getBytes());
		}

		int i;

		for (i = 0; i < symbols.length; i++)
			if (symbols[i].equals(t))
				return i;

		throw new Exception("Undefined token: " + t);
	}

	/**
	 * Used to write any needed output after the parsing is done.
	 */
	public void WriteOutput() {
		// this is common that the code generator does it
		cg.WriteOutput(os);
	}
}

