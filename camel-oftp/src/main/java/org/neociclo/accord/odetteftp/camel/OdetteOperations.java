package org.neociclo.accord.odetteftp.camel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.camel.Exchange;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.component.file.FileOperations;
import org.apache.camel.component.file.GenericFileOperationFailedException;
import org.apache.camel.util.IOHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.neociclo.odetteftp.TransferMode;
import org.neociclo.odetteftp.protocol.OdetteFtpObject;
import org.neociclo.odetteftp.protocol.VirtualFile;
import org.neociclo.odetteftp.service.TcpClient;
import org.neociclo.odetteftp.support.InOutSharedQueueOftpletFactory;
import org.neociclo.odetteftp.support.SessionConfig;

public class OdetteOperations extends FileOperations {

	private static final long TEMP_COPY_BUFFER_SIZE = 128 * 1024;
	protected final transient Log log = LogFactory.getLog(getClass());
	private OdetteEndpoint endpoint;
	private TcpClient client;
	private Queue<OdetteFtpObject> outgoingQueue = new ConcurrentLinkedQueue<OdetteFtpObject>();
	private Queue<OdetteFtpObject> incomingQueue = new ConcurrentLinkedQueue<OdetteFtpObject>();
	private boolean hasOut;
	private boolean hasIn;
	private InOutSharedQueueOftpletFactory factory;
	private Set<String> temporaryFiles = new HashSet<String>();

	public OdetteOperations(OdetteEndpoint odetteEndpoint) {
		this.endpoint = odetteEndpoint;
	}

	public boolean isConnected() {
		if (client != null) {
			return client.isConnected();
		}

		return false;
	}

	/**
	 * <p>
	 * Starts polling files and notifications from Odette server. If Producer
	 * has added any file/notification to be sent, will do during this session.
	 * </p>
	 * 
	 * @throws Exception
	 * 
	 * @throws ClientException
	 */
	public void pollServer() throws Exception {
		if (client == null || !client.isConnected()) {
			initClient();

			final OdetteConfiguration cfg = endpoint.getConfiguration();
			client = new TcpClient(cfg.getHost(), cfg.getPort(), factory);
			client.connect(true);
		}
	}

	private void initClient() {
		final OdetteConfiguration cfg = endpoint.getConfiguration();

		TransferMode identifyTransferMode = identifyTransferMode();

		SessionConfig session = new SessionConfig();
		session.setUserCode(cfg.getOid());
		session.setUserPassword(cfg.getPassword());
		session.setTransferMode(identifyTransferMode);
		session.setDataExchangeBufferSize(cfg.getBufferSize());
		session.setWindowSize(cfg.getWindowSize());

		factory = new InOutSharedQueueOftpletFactory(session, outgoingQueue, null, incomingQueue);
		factory.setEventListener(new InOutOftpletListener(endpoint));
	}

	private TransferMode identifyTransferMode() {
		if (hasIn && hasOut)
			return TransferMode.BOTH;
		else if (hasIn)
			return TransferMode.RECEIVER_ONLY;
		else if (hasOut)
			return TransferMode.SENDER_ONLY;

		return null;
	}

	public void disconnect() {
		synchronized (client) {
			if (client != null && client.isConnected()) {
				try {
					client.awaitDisconnect();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void setHasInQueue() {
		hasIn = true;
	}

	public void setHasOutQueue() {
		hasOut = true;
	}

	public void offer(OdetteFtpObject payload) {
		outgoingQueue.offer(payload);
		offerQueueToClient();
	}

	private void offerQueueToClient() {
		int size = outgoingQueue.size();
		boolean wasEmpty = size == 1;
		if (!hasIn && wasEmpty) {
			Timer providerTimer = new Timer();
			OdetteConfiguration configuration = endpoint.getConfiguration();
			providerTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					try {
						pollServer();
					} catch (Exception e) {
						e.printStackTrace();
						throw new RuntimeCamelException(e);
					}
				}
			}, configuration.getDelay());
		}
	}

	public boolean storeTempFile(File source, File file, Exchange exchange) {
		try {
			writeFileByFile(source, file);
			keepLastModified(exchange, file);
			return true;
		} catch (IOException e) {
			throw new GenericFileOperationFailedException("Cannot store file: " + file, e);
		}
	}

	private void writeFileByFile(File source, File target) throws IOException {
		FileChannel in = new FileInputStream(source).getChannel();
		FileChannel out = null;
		try {
			out = prepareOutputFileChannel(target, out);

			if (log.isTraceEnabled()) {
				log.trace("Using FileChannel to transfer from: " + in + " to: " + out);
			}

			long size = in.size();
			long position = 0;
			while (position < size) {
				position += in.transferTo(position, TEMP_COPY_BUFFER_SIZE, out);
			}
		} finally {
			IOHelper.close(in, source.getName(), log);
			IOHelper.close(out, source.getName(), log);
		}
	}

	private void keepLastModified(Exchange exchange, File file) {
		Long last;
		Date date = exchange.getIn().getHeader(Exchange.FILE_LAST_MODIFIED, Date.class);
		if (date != null) {
			last = date.getTime();
		} else {
			// fallback and try a long
			last = exchange.getIn().getHeader(Exchange.FILE_LAST_MODIFIED, Long.class);
		}
		if (last != null) {
			boolean result = file.setLastModified(last);
			if (log.isTraceEnabled()) {
				log.trace("Keeping last modified timestamp: " + last + " on file: " + file + " with result: " + result);
			}
		}
	}

	/**
	 * Creates and prepares the output file channel. Will position itself in
	 * correct position if eg. it should append or override any existing
	 * content.
	 */
	private FileChannel prepareOutputFileChannel(File target, FileChannel out) throws IOException {
		/*
		 * if (endpoint.getFileExist() == GenericFileExist.Append) { out = new
		 * RandomAccessFile(target, "rw").getChannel(); out =
		 * out.position(out.size()); } else { // will override out = new
		 * FileOutputStream(target).getChannel(); }
		 */
		out = new FileOutputStream(target).getChannel();
		return out;
	}

	public void virtualFileSent(VirtualFile virtualFile) {
		File file = virtualFile.getFile();
		String path = file.getPath();
		if (temporaryFiles.remove(path)) {
			deleteFile(path);
		}
	}

	public void notifyOfTemporaryFile(VirtualFile virtualFile) {
		temporaryFiles.add(virtualFile.getFile().getPath());
	}

}
