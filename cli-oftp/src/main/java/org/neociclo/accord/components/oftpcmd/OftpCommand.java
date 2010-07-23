package org.neociclo.accord.components.oftpcmd;

import java.io.PrintWriter;
import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

/**
 * 
 */
public class OftpCommand {

	private OftpCmdOptions options = new OftpCmdOptions();
	private String[] args;

	public void command(String... args) {
		assignArguments(args);
		printHeaderAndVersion();
		printHelpIfNeeded();
		parseCommandLineOptions();
		doCommand();
	}

	private void doCommand() {
		OftpProcess process = new OftpProcess(options);
		process.start();
	}

	private void assignArguments(String... args) {
		this.args = Arrays.copyOf(args, args.length);
	}

	private void parseCommandLineOptions() {
		try {
			CommandLineParser parser = new PosixParser();
			CommandLine cmdLine = parser.parse(options.getCommandOptions(),
					args);

			options.fillOptionsValues(cmdLine);
		} catch (ParseException e) {
			System.err.println();
			System.err.println("** " + e.getMessage());
			System.exit(1);
		}
	}

	private void printHelpIfNeeded() {
		if (args == null || args.length == 0 || "-h".equals(args[0])) {
			// automatically generate the help statement
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("aoc", options.getCommandOptions());
			System.exit(0);
		}
	}

	private void printHeaderAndVersion() {
		PrintWriter ps = new PrintWriter(System.out);
		ps.println();
		ps.println("  Accord OFTP Client v1.0");
		ps.println();
		ps.flush();
	}

}
