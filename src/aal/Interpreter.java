package aal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

/**
 * An interpreter for ACSL Assembly Language (AAL).
 * 
 * @see <a href="http://www.apcomputerscience.com/cst/topic_descriptions/assembly.pdf">Assembly specifications</a>
 *
 * TODO Add an interactive/trace mode?
 *
 */
public class Interpreter {

	/**
	 * All operations are performed % 1,000,000 as per the AAL specifications.
	 */
	public static final int MAX = 1000000;

	private final HashMap<String, Integer> branches = new HashMap<String, Integer>();
	private final HashMap<String, Integer> vars = new HashMap<String, Integer>();
	private final List<InterpretedLine> lines;
	private final Scanner scan = new Scanner(System.in);

	private int acc;
	private int index;
	private boolean verbose;
	private String spaces; 

	public Interpreter(String source) throws AssemblyException {
		this.lines = new ArrayList<InterpretedLine>();
		try(Scanner scan = new Scanner(source)) {
			while (scan.hasNextLine())
				lines.add(new InterpretedLine(scan.nextLine()));
		}
		verifyBranches();
	}

	public Interpreter(File file) throws AssemblyException, IOException {
		this.lines = new ArrayList<InterpretedLine>();
		try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
			while (reader.ready())
				lines.add(new InterpretedLine(reader.readLine()));
		}
		verifyBranches();
	}

	public Interpreter(List<String> source) throws AssemblyException {
		this.lines = new ArrayList<InterpretedLine>();
		for (int i = 0; i < source.size(); ++i)
			lines.add(new InterpretedLine(source.get(i)));
		verifyBranches();
	}

	public void interpret() throws AssemblyException {
		interpret(false);
	}

	public void interpret(boolean verbose) throws AssemblyException {
		vars.clear();
		this.verbose = verbose;
		index = 0;
		acc = 0;
		execute();
	}

	private void execute() throws AssemblyException {
		while(index < lines.size()) {
			InterpretedLine line = lines.get(index);
			//Align everything based on line # and max label length
			if (verbose)
				System.out.print((index+1) + ": " + line.toString(spaces.substring((index+1)/10)) + " ");
			switch(line.opCode) {
			case LOAD: load(line.loc); break;
			case STORE: store(line.loc, acc); break;
			case ADD: add(line.loc); break;
			case SUB: sub(line.loc); break;
			case MULT: mult(line.loc); break;
			case DIV: div(line.loc); break;

			case BE: be(line.loc); break;
			case BG: bg(line.loc); break;
			case BL: bl(line.loc); break;
			case BU: bu(line.loc); break;

			case READ: read(line.loc); break;
			case PRINT: print(line.loc); break;
			case DC: store(line.label, parseInt(line.loc)); break;
			case END: System.out.println(); return;
			}
			++index;
		}
	}

	private void load(String loc) throws AssemblyException {
		acc = get(loc);
		if (verbose)
			System.out.println("(ACC = " + acc + ")");
	}

	private void store(String loc, int val) {
		vars.put(loc, val);
		if (verbose)
			System.out.println("(" + loc + " = " + val + ")");
	}

	private void add(String loc) throws AssemblyException {
		acc = (acc + get(loc)) % MAX;
		if (verbose)
			System.out.println("(ACC = " + acc + ")");
	}

	private void sub(String loc) throws AssemblyException {
		acc = (acc - get(loc)) % MAX;
		if (verbose)
			System.out.println("(ACC = " + acc + ")");
	}

	private void mult(String loc) throws AssemblyException {
		acc = (acc * get(loc)) % MAX;
		if (verbose)
			System.out.println("(ACC = " + acc + ")");
	}

	private void div(String loc) throws AssemblyException {
		acc /= get(loc);
		if (verbose)
			System.out.println("(ACC = " + acc + ")");
	}

	private void be(String label) {
		if (verbose)
			System.out.println("(Branch? " + (acc == 0 ? "YES" : "NO") + ")");
		if (acc == 0)
			index = branches.get(label) - 1;
	}

	private void bg(String label) {
		if (verbose)
			System.out.println("(Branch? " + (acc > 0 ? "YES" : "NO") + ")");
		if (acc > 0)
			index = branches.get(label) - 1;
	}

	private void bl(String label) {
		if (verbose)
			System.out.println("(Branch? " + (acc < 0 ? "YES" : "NO") + ")");
		if (acc < 0)
			index = branches.get(label) - 1;
	}

	private void bu(String label) {
		if (verbose)
			System.out.println("(Branch? YES)");
		index = branches.get(label) - 1;
	}

	private void print(String loc) throws AssemblyException {
		int val = get(loc);
		if(verbose)
			System.out.println("(" + loc + " = " + val + ")");
		System.out.println(val);
	}

	private int get(String loc) throws AssemblyException {
		if (loc.charAt(0) == '=')
			return parseInt(loc.substring(1));
		Integer integer = vars.get(loc);
		if (integer == null)
			throw new AssemblyException("Undefined loc " + loc + debug());
		return integer;
	}

	private int parseInt(String str) throws AssemblyException {
		try {
			return Integer.parseInt(str) % MAX;
		}
		catch(NumberFormatException e) {
			throw new AssemblyException(str + " is not a valid number!" + debug());
		}
	}

	private String debug() {
		return "@" + index + ": " + lines.get(index);
	}

	private void read(String loc) {
		//Print out the assembly line instead of an input request line?
		if (!verbose)
			System.out.print((index+1) + ": " + lines.get(index).toString(spaces.substring((index+1)/10)) + " ");
		vars.put(loc, scan.nextInt() % MAX);
	}

	public void print() {
		for (int i = 0; i < lines.size(); ++i)
			System.out.println((i+1) + ": " + lines.get(i).toString(spaces.substring((i+1)/10)));
	}

	private void verifyBranches() throws AssemblyException {
		int maxLabelLength = 0;
		for (int i = 0; i < lines.size(); ++i) {
			String label = lines.get(i).label;
			if (label != null) {
				if (label.length() > maxLabelLength)
					maxLabelLength = label.length();
				if (branches.containsKey(label))
					throw new AssemblyException("Duplicate label " + label);
				if (lines.get(i).opCode != OpCode.DC)
					branches.put(label, i);
			}
		}
		for (InterpretedLine l : lines)
			if (OpCode.isBranch(l.opCode) && !branches.containsKey(l.loc))
				throw new AssemblyException("Undefined branch " + l.loc);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < maxLabelLength + 3; ++i)
			sb.append(' ');
		spaces = sb.toString();
	}
}
