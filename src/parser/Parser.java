package parser;

import cg.CodeGenerator;
import scanner.ScannerWrapper;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Stack;

public class Parser {
	ScannerWrapper scanner;    // *** THIS IS NOT java.util.Scanner ***
	OutputStream os;
	CodeGenerator cg;
	PTBlock[][] parseTable;    // a 2D array of blocks, forming a parse table
	Stack<Integer> parseStack = new Stack<Integer>();
	String[] symbols;

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
			cg = new CodeGenerator(scanner);
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
						throw new Exception("Compile Error at token \"" + tokenText + "\" at line " + scanner.lineNumber + " ; node@" + currentNode);
					}

					case PTBlock.ActionType.Shift: {
						cg.doSemantic(ptb.getSem());
						tokenID = nextTokenID();
						currentNode = ptb.getIndex();  // index is pointing to Shift location for next node
					}
					break;

					case PTBlock.ActionType.Goto: {
						cg.doSemantic(ptb.getSem());
						currentNode = ptb.getIndex();  // index is pointing to Goto location for next node
					}
					break;

					case PTBlock.ActionType.PushGoto: {
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
			cg.FinishCode();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	private int nextTokenID() throws Exception {
		String t = null;
		try {
			t = scanner.NextToken();
		} catch (Exception e) {
			e.printStackTrace();
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

