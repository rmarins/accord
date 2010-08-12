package org.neociclo.odetteftp.examples;

import static java.lang.System.out;
import static java.lang.System.err;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class MainSupport {

	private String[] parameters;
	private Map<String, String> mapParameters;
	private Properties fileProperties;
	private String exampleName;

	public MainSupport(Class<?> example, String... parameters) {
		this(example.getName(), parameters);
	}

	public MainSupport(String exampleName, String... parameters) {
		this.parameters = parameters;
		this.mapParameters = new HashMap<String, String>();
		this.exampleName = exampleName;

		for (String p : parameters) {
			String property = System.getProperty(p, getFileProperty(p));

			if (property == null) {
				break;
			}

			mapParameters.put(p, property);
		}

		if (parameters.length != mapParameters.keySet().size()) {
			err.println("Incorrect number of arguments.");
			printUsage();
			System.exit(1);
		}
	}

	private String getFileProperty(String p) {
		loadFileProperties();

		return fileProperties.getProperty(p);
	}

	private void loadFileProperties() {
		if (fileProperties != null) {
			return;
		}

		fileProperties = new Properties();
		try {
			fileProperties.load(MainSupport.class
					.getResourceAsStream("/odette.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String get(String property) {
		return mapParameters.get(property);
	}

	public String get(int index) {
		String p = parameters[index];
		return get(p);
	}

	private void printUsage() {
		out.println();
		out.println("Example: " + exampleName);
		StringBuilder sb = new StringBuilder();
		sb.append("This example must be run with the following properties: \n");
		for (String p : parameters) {
			sb.append("  - ").append(p).append('\n');
		}

		out.println(sb);
		out.print("Make sure these properties exist either by setting on");
		out.println("odette.properties or with -Dparameter=value arguments");
		System.out.println();
	}

}
