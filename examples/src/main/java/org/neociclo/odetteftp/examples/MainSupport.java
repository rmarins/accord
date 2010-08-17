/**
 * Neociclo Accord, Open Source B2B Integration Suite
 * Copyright (C) 2005-2010 Neociclo, http://www.neociclo.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * $Id$
 */
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
	private String[] indexedArgs;

	public MainSupport(Class<?> example, String[] args, String... parameters) {
		this.parameters = parameters;
		this.mapParameters = new HashMap<String, String>();
		this.exampleName = example.getSimpleName();
		this.indexedArgs = new String[parameters.length];

		int index = 0;
		for (String p : parameters) {
			String property = System.getProperty(p, getFileProperty(p));
			if (args.length > index && (property == null || property.trim().length() == 0)) {
				property = args[index];
			}

			if (property == null) {
				break;
			}

			mapParameters.put(p, property);
			indexedArgs[index++] = property;
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
			fileProperties.load(MainSupport.class.getResourceAsStream("/odette.properties"));
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
		out.print("Make sure these properties exist either by setting on ");
		out.println("odette.properties or with -Dparameter=value arguments");
		out.println();
		out.println("You can also specify ALL parameters as plain arguments.");
		System.out.println();
	}

	public String[] args() {
		return indexedArgs;
	}

}
