package org.neociclo.accord.camel.odette;

import java.util.concurrent.TimeUnit;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ShutdownRunningTask;
import org.apache.camel.impl.ScheduledPollConsumer;
import org.apache.camel.spi.ShutdownAware;

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
public class OdetteConsumer extends ScheduledPollConsumer implements
		ShutdownAware {

	private OdetteOperations operations;

	public OdetteConsumer(OdetteEndpoint endpoint, Processor processor,
			OdetteOperations operations) {
		super(endpoint, processor);

		setPollStrategy(new DefaultOdettePollingStrategy());
		setDelay(endpoint.getConfiguration().getDelay());
		setInitialDelay(endpoint.getConfiguration().getInitialDelay());
		setTimeUnit(TimeUnit.SECONDS);
		setUseFixedDelay(true);

		this.operations = operations;
		this.operations
				.addIncomingTaskListener(new OdetteIncomingTaskListener() {
					public void incoming(OftpFile file) {
						processOdetteMessage(new OdetteFileMessage(file));
					}

					public void incoming(DeliveryNotificationInfo notInfo) {
						processOdetteMessage(new OdetteDeliveryMessage(notInfo));
					}
				});
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
	 * @param om
	 */
	private void processOdetteMessage(OdetteMessage<?> om) {
		Exchange e = getEndpoint().createExchange();

		om.bind(e);

		try {
			getProcessor().process(e);
		} catch (Exception e1) {
			getExceptionHandler().handleException(e1);
		}
	}

	@Override
	protected void poll() throws Exception {
		operations.startSession();
	}

}
