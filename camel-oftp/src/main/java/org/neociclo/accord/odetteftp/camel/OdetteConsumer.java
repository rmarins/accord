package org.neociclo.accord.odetteftp.camel;

import java.io.File;
import java.util.Queue;

import org.apache.camel.BatchConsumer;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ShutdownRunningTask;
import org.apache.camel.component.file.FileConsumer;
import org.apache.camel.component.file.GenericFile;
import org.apache.camel.impl.ScheduledPollConsumer;
import org.apache.camel.spi.ShutdownAware;
import org.neociclo.odetteftp.protocol.VirtualFile;

/**
 * <p>
 * The Odette FTP Poll Consumer will connect to an Odette server and consume
 * incoming files or delivery notifications. If the Odette FTP Producer
 * associated to the same endpoint receives files or notifications to be sent,
 * they will during the same session
 * </p>
 * 
 * @author bruno
 * 
 */
public class OdetteConsumer extends ScheduledPollConsumer implements BatchConsumer, ShutdownAware {

	private OdetteOperations operations;

	public OdetteConsumer(OdetteEndpoint endpoint, Processor processor, OdetteOperations operations) {
		super(endpoint, processor);

		setPollStrategy(new DefaultOdettePollingStrategy());
		setDelay(endpoint.getConfiguration().getDelay());
		setInitialDelay(endpoint.getConfiguration().getInitialDelay());
		setUseFixedDelay(true);

		this.operations = operations;
	}

	public boolean deferShutdown(ShutdownRunningTask shutdownRunningTask) {
		return false;
	}

	public int getPendingExchangesSize() {
		return 0;
	}

	/**
	 * <p>
	 * Consumes data from the Odette server
	 * </p>
	 * 
	 * @param incomingFile
	 * 
	 * @param om
	 */
	public void processOdetteMessage(VirtualFile incomingFile) {
		OdetteEndpoint odetteEndpoint = (OdetteEndpoint) getEndpoint();

		OdetteConfiguration configuration = ((OdetteEndpoint) getEndpoint()).getConfiguration();
		String absolutePath = configuration.getTmpDir().getAbsolutePath();
		GenericFile<File> file = FileConsumer.asGenericFile(absolutePath, incomingFile.getFile());

		try {
			Exchange e = odetteEndpoint.createExchange(file);
			odetteEndpoint.configureMessage(file, incomingFile, e.getIn());
			getProcessor().process(e);
		} catch (Exception e1) {
			e1.printStackTrace();
			getExceptionHandler().handleException(e1);
		}
	}

	@Override
	protected void poll() throws Exception {
		try {
			operations.pollServer();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isBatchAllowed() {
		return false;
	}

	public void processBatch(Queue<Object> arg0) throws Exception {
	}

	public void setMaxMessagesPerPoll(int arg0) {
	}

}
