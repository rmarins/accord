package org.neociclo.odetteftp.examples;

public class MainSupport {

	private static final String DEFAULT_USAGE = "<Example> <arg0> <arg1>";
	private String[] args;
	private String usage;

	public MainSupport(String[] args, int expectedArgs) {
		this(args, expectedArgs, DEFAULT_USAGE);
	}

	public MainSupport(String[] args, int expectedArgs, String usage) {
		this.args = args;
		this.usage = usage;

		if (args.length != expectedArgs) {
			System.err.println("Incorrect number of arguments.");
			System.err.println();

			printUsage();
			System.exit(1);
		}
	}

	public String get(int index) {
		return args[index];
	}

	private void printUsage() {
		System.out.println(usage);
		System.out.println();
	}

}
