/**
 * The Accord Project, http://accordproject.org
 * Copyright (C) 2005-2013 Rafael Marins, http://rafaelmarins.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neociclo.odetteftp.examples;

import static java.lang.System.out;
import static java.lang.System.err;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
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
			URL res = MainSupport.class.getResource("/odette.properties");
			if (res != null) {
				File file = new File(res.toURI());
				if (file.exists()) {
					fileProperties.load(new FileInputStream(file));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
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
