package org.neociclo.accord.components;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Field;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.junit.Test;
import org.neociclo.accord.components.oftpcmd.CommandLineOption;
import org.neociclo.accord.components.oftpcmd.OftpCmdOptions;
import org.neociclo.odetteftp.TransferMode;

/**
 * Unit test for simple App.
 */
public class OftpCmdOptionsTest {

	@Test
	public void testOptions() {
		OftpCmdOptions options = new OftpCmdOptions();
		Options cmdOptions = options.getCommandOptions();

		int optionField = 0;
		Field[] fields = OftpCmdOptions.class.getDeclaredFields();
		for (Field field : fields) {
			if (field.getAnnotation(CommandLineOption.class) != null) {
				optionField++;
			}
		}

		assertTrue(optionField == cmdOptions.getOptions().size());
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testCommandLine() throws Exception {
		String[] arguments = { "-oid", "foo", "-verbose", "-server",
				"localhost", "-file", "foobar.ext", "-transferMode", "BOTH",
				"-fileTimestamp", "01321336" };
		OftpCmdOptions options = new OftpCmdOptions();

		CommandLine cmdLine = new PosixParser().parse(options
				.getCommandOptions(), arguments);

		options.fillOptionsValues(cmdLine);

		assertTrue("foo".equals(options.getOid()));
		assertTrue(options.isVerbose());
		assertTrue("localhost".equals(options.getServer()));
		assertTrue(new File("foobar.ext").equals(options.getFile()));
		assertTrue(options.getTransferMode() == TransferMode.BOTH);
		assertTrue(options.getFileTimestamp() != null);
		assertTrue(options.getFileTimestamp().getMonth() == 1);
		assertTrue(options.getFileTimestamp().getDate() == 1);
		assertTrue(options.getFileTimestamp().getHours() == 13);
		assertTrue(options.getFileTimestamp().getMinutes() == 36);
	}

}
