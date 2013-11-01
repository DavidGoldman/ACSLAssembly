package aal;

import java.io.File;
import java.io.IOException;

public class Driver {

	public static void main(String[] args) {
		try {
			Interpreter ip = new Interpreter(new File("Factorial.txt"));
			ip.interpret();
		} catch (AssemblyException | IOException e) {
			e.printStackTrace();
		}
	}

}
