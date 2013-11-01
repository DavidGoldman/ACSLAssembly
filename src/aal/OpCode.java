package aal;

public enum OpCode {
	LOAD,
	STORE,
	ADD,
	SUB,
	MULT,
	DIV,
	BE,
	BG,
	BL,
	BU,
	END,
	READ,
	PRINT,
	DC;
	
	public static OpCode parseOpCode(String str) {
		for (OpCode op : values())
			if (op.toString().equalsIgnoreCase(str))
				return op;
		return null;
	}
	
	public static boolean isBranch(OpCode code) {
		return code == BE || code == BG || code == BL || code == BU;
	}
}
