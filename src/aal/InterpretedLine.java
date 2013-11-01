package aal;

import java.util.ArrayList;
import java.util.List;

public class InterpretedLine {

	public final String source;

	public final String label;
	public final OpCode opCode;
	public final String loc;

	/**
	 * 
	 * @param source Source ACSL Assembly line (up to 3 tokens - no comments)
	 */
	public InterpretedLine(String source) throws AssemblyException {
		this.source = source;
		List<String> tokens = tokenize(source);
		switch(tokens.size()) {
		case 3: 
			label = tokens.get(0);
			opCode = parseOpCode(tokens.get(1), true);
			loc = tokens.get(2);
			break;
		case 2: 
			OpCode op =  parseOpCode(tokens.get(0), false);
			if (op == null) { //Must be label OPCODE
				label = tokens.get(0);
				opCode = parseOpCode(tokens.get(1), true);
				loc = null;
			}
			else { //Must be OPCODE loc
				label = null;
				opCode = op;
				loc = tokens.get(1);
			}
			break;
		default:
			throw new AssemblyException("Invalid line: " + source);
		}
		validateOpCode();
	}

	private void validateOpCode() throws AssemblyException {
		//All opCodes besides END must have a LOC; DC opCodes must have a label
		if (opCode == OpCode.END && loc != null || opCode != OpCode.END && loc == null || opCode == OpCode.DC && label == null)
			throw new AssemblyException("Invalid line: " + source);
	}

	public String toString() {
		return source;
	}
	
	public String toString(String spaces) {
		if (label == null)
			return spaces + opCode + " " + loc;
		if (loc == null)
			return label + spaces.substring(0, spaces.length() - label.length()) + opCode;
		return label + spaces.substring(0, spaces.length() - label.length()) + opCode + " " + loc;
	}

	public static OpCode parseOpCode(String op, boolean throwError) throws AssemblyException {
		OpCode code = OpCode.parseOpCode(op);
		if (code == null && throwError)
			throw new AssemblyException(op + " is not a valid op code!");
		return code;
	}

	public static List<String> tokenize(String source) {
		List<String> tokens = new ArrayList<String>();
		String token = "";
		for (char c : source.toCharArray())
			if (isSpace(c)) {
				if (!token.isEmpty()) {
					tokens.add(token);
					token = "";
				}
			}
			else 
				token += c;
		if (!token.isEmpty())
			tokens.add(token);
		return tokens;
	}

	public static boolean isSpace(char c) {
		return c == ' ' || c == '\t';
	}

}
