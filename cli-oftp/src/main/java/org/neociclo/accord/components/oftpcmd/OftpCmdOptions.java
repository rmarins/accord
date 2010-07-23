package org.neociclo.accord.components.oftpcmd;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Date;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.neociclo.accord.components.oftpcmd.CommandOptionConverter.NullConverter;
import org.neociclo.odetteftp.TransferMode;

public class OftpCmdOptions implements OftpParameters {

	private static final long serialVersionUID = -4585287071879110557L;

	private static final int DEFAULT_BUFFERSIZE = 0;

	private static final int DEFAULT_WINDOWSIZE = 0;

	private static final int DEFAULT_RECORDSIZE = 0;

	private static final int DEFAULT_TIMEOUT = 0;

	private static final TransferMode DEFAULT_TRANSFERMODE = TransferMode.RECEIVER_ONLY;

	@CommandLineOption(required = true)
	private String oid = null;

	@CommandLineOption(required = true)
	private String server = null;

	@CommandLineOption
	private File file = null;

	@CommandLineOption
	private String pass = null;

	@CommandLineOption
	private String originator = null;

	@CommandLineOption
	private String destination = null;

	@CommandLineOption
	private int bufferSize = DEFAULT_BUFFERSIZE;

	@CommandLineOption
	private int windowSize = DEFAULT_WINDOWSIZE;

	@CommandLineOption
	private int recordSize = DEFAULT_RECORDSIZE;

	@CommandLineOption
	private int timeout = DEFAULT_TIMEOUT;

	@CommandLineOption
	private TransferMode transferMode = DEFAULT_TRANSFERMODE;

	@CommandLineOption
	private String cipher = null;

	@CommandLineOption(hasArg = false)
	private boolean signed = false;

	@CommandLineOption(hasArg = false)
	private boolean encripted = false;

	@CommandLineOption(hasArg = false)
	private boolean compressed = false;

	@CommandLineOption(hasArg = false)
	private boolean verbose = false;

	@CommandLineOption(converter = DateCommandOptionConverter.class, hasArgs = 2)
	// first
	private Date fileTimestamp;

	/* (non-Javadoc)
	 * @see org.neociclo.accord.components.oftpcmd.OftpParameters#isVerbose()
	 */
	public boolean isVerbose() {
		return verbose;
	}

	/* (non-Javadoc)
	 * @see org.neociclo.accord.components.oftpcmd.OftpParameters#getOid()
	 */
	public String getOid() {
		return oid;
	}

	/* (non-Javadoc)
	 * @see org.neociclo.accord.components.oftpcmd.OftpParameters#getServer()
	 */
	public String getServer() {
		return server;
	}

	/* (non-Javadoc)
	 * @see org.neociclo.accord.components.oftpcmd.OftpParameters#getFile()
	 */
	public File getFile() {
		return file;
	}

	/* (non-Javadoc)
	 * @see org.neociclo.accord.components.oftpcmd.OftpParameters#getPass()
	 */
	public String getPass() {
		return pass;
	}

	/* (non-Javadoc)
	 * @see org.neociclo.accord.components.oftpcmd.OftpParameters#getOriginator()
	 */
	public String getOriginator() {
		return originator;
	}

	/* (non-Javadoc)
	 * @see org.neociclo.accord.components.oftpcmd.OftpParameters#getDestination()
	 */
	public String getDestination() {
		return destination;
	}

	/* (non-Javadoc)
	 * @see org.neociclo.accord.components.oftpcmd.OftpParameters#getBufferSize()
	 */
	public int getBufferSize() {
		return bufferSize;
	}

	/* (non-Javadoc)
	 * @see org.neociclo.accord.components.oftpcmd.OftpParameters#getWindowSize()
	 */
	public int getWindowSize() {
		return windowSize;
	}

	/* (non-Javadoc)
	 * @see org.neociclo.accord.components.oftpcmd.OftpParameters#getRecordSize()
	 */
	public int getRecordSize() {
		return recordSize;
	}

	/* (non-Javadoc)
	 * @see org.neociclo.accord.components.oftpcmd.OftpParameters#getTimeout()
	 */
	public int getTimeout() {
		return timeout;
	}

	/* (non-Javadoc)
	 * @see org.neociclo.accord.components.oftpcmd.OftpParameters#getTransferMode()
	 */
	public TransferMode getTransferMode() {
		return transferMode;
	}

	/* (non-Javadoc)
	 * @see org.neociclo.accord.components.oftpcmd.OftpParameters#getCipher()
	 */
	public String getCipher() {
		return cipher;
	}

	/* (non-Javadoc)
	 * @see org.neociclo.accord.components.oftpcmd.OftpParameters#isSigned()
	 */
	public boolean isSigned() {
		return signed;
	}

	/* (non-Javadoc)
	 * @see org.neociclo.accord.components.oftpcmd.OftpParameters#isEncripted()
	 */
	public boolean isEncripted() {
		return encripted;
	}

	/* (non-Javadoc)
	 * @see org.neociclo.accord.components.oftpcmd.OftpParameters#isCompressed()
	 */
	public boolean isCompressed() {
		return compressed;
	}

	/* (non-Javadoc)
	 * @see org.neociclo.accord.components.oftpcmd.OftpParameters#getFileTimestamp()
	 */
	public Date getFileTimestamp() {
		return fileTimestamp;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	private Options options;

	public Options getCommandOptions() {
		if (options != null) {
			return options;
		}
		options = new Options();

		iterateFields(new IterationFieldCallback() {
			public void iterate(Field field, CommandLineOption cmdOption) {
				Option option = new Option(field.getName(), cmdOption.hasArg(),
						cmdOption.description());

				option.setType(field.getType());
				options.addOption(option);
			}
		});

		return options;
	}

	public void fillOptionsValues(final CommandLine cmdLine) {
		iterateFields(new IterationFieldCallback() {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public void iterate(Field field, CommandLineOption cmdOption)
					throws Exception {
				String option = cmdOption.name().length() > 0 ? cmdOption
						.name() : field.getName();

				if (cmdLine.hasOption(option) == false) {
					return;
				}

				Object parsedValue = null;

				if (cmdOption.converter() != NullConverter.class) {
					CommandOptionConverter converter = cmdOption.converter()
							.newInstance();
					String[] optionArgs = cmdLine.getOptionValues(option);
					parsedValue = converter.convert(optionArgs);
				} else if (field.getType() == boolean.class
						|| field.getType() == Boolean.class) {
					parsedValue = cmdLine.hasOption(option);
				} else if (field.getType().isEnum()) {
					Class<Enum> enumType = (Class<Enum>) field.getType();
					parsedValue = Enum.valueOf(enumType, cmdLine
							.getOptionValue(option).toUpperCase());
				} else {
					parsedValue = cmdLine.getParsedOptionValue(option);
				}

				field.set(OftpCmdOptions.this, parsedValue);
			}
		});
	}

	private void iterateFields(IterationFieldCallback callback) {
		Class<OftpCmdOptions> clazz = OftpCmdOptions.class;
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			if (field.isAnnotationPresent(CommandLineOption.class) == false) {
				continue;
			}

			field.setAccessible(true);
			try {
				callback.iterate(field, field
						.getAnnotation(CommandLineOption.class));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private interface IterationFieldCallback {
		void iterate(Field field, CommandLineOption cmdOption) throws Exception;
	}

}
