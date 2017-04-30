package parser;

import cg.CodeGenerator;
import scanner.ScannerWrapper;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Stack;

public class Parser {
	ScannerWrapper scannerWrapper;
	CodeGenerator cg;
	PTBlock[][] parseTable;
	Stack<Integer> parseStack = new Stack<Integer>();
	String[] symbols;

	public Parser(InputStream is, String[] symbols, PTBlock[][] parseTable) {
		try {
			this.parseTable = parseTable;
			this.symbols = symbols;

			scannerWrapper = new ScannerWrapper(is);
			cg = new CodeGenerator(scannerWrapper);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int LineNumber() {
		return scannerWrapper.lineNumber; // Or any other name you used in your Scanner
	}

	public void Parse() {
		try {
			int tokenId = nextTokenID();
			int curNode = 0;
			boolean notAccepted = true;
			while (notAccepted) {
				String token = symbols[tokenId];
				PTBlock ptb = parseTable[curNode][tokenId];
				switch (ptb.getAct()) {
					case PTBlock.ActionType.Error: {
						throw new Exception(String.format("Compile Error (" + token + ") at line " + scannerWrapper.lineNumber + " @ " + curNode));
					}
					case PTBlock.ActionType.Shift: {
						cg.Generate(ptb.getSem());
						tokenId = nextTokenID();
						curNode = ptb.getIndex();
					}
					break;

					case PTBlock.ActionType.PushGoto: {
						parseStack.push(curNode);
						curNode = ptb.getIndex();
					}
					break;

					case PTBlock.ActionType.Reduce: {
						if (parseStack.size() == 0) {
							throw new Exception(String.format("Compile Error (" + token + ") at line " + scannerWrapper.lineNumber + " @ " + curNode));
						}

						curNode = parseStack.pop();
						ptb = parseTable[curNode][ptb.getIndex()];
						cg.Generate(ptb.getSem());
						curNode = ptb.getIndex();
					}
					break;

					case PTBlock.ActionType.Accept: {
						notAccepted = false;
					}
					break;

				}
			}
			cg.FinishCode();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	int nextTokenID() {
		String t = "";
		try {
			t = scannerWrapper.NextToken();
		} catch (Exception e) {
			e.printStackTrace();
		}

		int i;

		for (i = 0; i < symbols.length; i++)
			if (symbols[i].equals(t))
				return i;
		(new Exception("Undefined token: " + t)).printStackTrace();
		return 0;
	}

	public void WriteOutput(OutputStream os) {
		cg.WriteOutput(os);
	}
}


