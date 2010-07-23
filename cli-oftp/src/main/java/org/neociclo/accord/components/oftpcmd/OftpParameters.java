package org.neociclo.accord.components.oftpcmd;

import java.io.File;
import java.util.Date;

import org.neociclo.odetteftp.TransferMode;

public interface OftpParameters {

	boolean isVerbose();

	String getOid();

	String getServer();

	File getFile();

	String getPass();

	String getOriginator();

	String getDestination();

	int getBufferSize();

	int getWindowSize();

	int getRecordSize();

	int getTimeout();

	TransferMode getTransferMode();

	String getCipher();

	boolean isSigned();

	boolean isEncripted();

	boolean isCompressed();

	Date getFileTimestamp();

}