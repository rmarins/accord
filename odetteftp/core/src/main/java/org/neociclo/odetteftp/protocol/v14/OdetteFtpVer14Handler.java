/**
 * Neociclo Accord, Open Source B2Bi Middleware
 * Copyright (C) 2005-2009 Neociclo, http://www.neociclo.com
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
 *
 * $Id$
 */
package org.neociclo.odetteftp.protocol.v14;

import static org.neociclo.odetteftp.protocol.v14.CommandBuilderVer14.endToEndResponse;
import static org.neociclo.odetteftp.protocol.v14.CommandBuilderVer14.negativeEndResponse;
import static org.neociclo.odetteftp.protocol.v14.CommandBuilderVer14.startFile;
import static org.neociclo.odetteftp.util.CommandFormatConstants.NERPCREA_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.NERPDATE_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.NERPDEST_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.NERPDSN_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.NERPORIG_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.NERPREAS_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.NERPTIME_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.SFIDTIME_FIELD;
import static org.neociclo.odetteftp.util.SessionHelper.getSessionOftplet;

import java.util.Date;

import org.neociclo.odetteftp.OdetteFtpException;
import org.neociclo.odetteftp.OdetteFtpSession;
import org.neociclo.odetteftp.oftplet.Oftplet;
import org.neociclo.odetteftp.oftplet.OftpletListener;
import org.neociclo.odetteftp.protocol.CommandExchangeBuffer;
import org.neociclo.odetteftp.protocol.DefaultDeliveryNotification;
import org.neociclo.odetteftp.protocol.DefaultVirtualFile;
import org.neociclo.odetteftp.protocol.DeliveryNotification;
import org.neociclo.odetteftp.protocol.DeliveryNotification.EndResponseType;
import org.neociclo.odetteftp.protocol.NegativeResponseReason;
import org.neociclo.odetteftp.protocol.VirtualFile;
import org.neociclo.odetteftp.protocol.v13.OdetteFtpVer13Handler;
import org.neociclo.odetteftp.util.ProtocolUtil;
import org.neociclo.odetteftp.util.TimestampTicker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class OdetteFtpVer14Handler extends OdetteFtpVer13Handler {

    private static final Logger LOGGER = LoggerFactory.getLogger(OdetteFtpVer14Handler.class);

    @Override
    protected CommandExchangeBuffer buildStartFileCommand(OdetteFtpSession session, VirtualFile vf) {
        return startFile(vf.getDatasetName(), vf.getDateTime(), vf.getTicker(), vf.getUserData(), vf.getDestination(), vf
                .getOriginator(), vf.getRecordFormat(), vf.getRecordSize(), vf.getSize(), vf.getRestartOffset());
    }

    @Override
    protected VirtualFile normalizeVirtualFile(OdetteFtpSession session, VirtualFile vf) {

    	DefaultVirtualFile normalizedVirtualFile = (DefaultVirtualFile) super.normalizeVirtualFile(session, vf);

    	// set API's generated timestamp counter (ticker) if empty
        Short ticker = vf.getTicker();
        if (ticker == null) {
        	ticker = Short.valueOf((short) TimestampTicker.getInstance().incrementAndGet());
        } else {
    		if (ticker > TimestampTicker.MAX_COUNTER_VALUE) {
    			ticker = 1;
    		}
        }

        normalizedVirtualFile.setTicker(ticker);
    	return normalizedVirtualFile;
    }

    @Override
    protected DefaultVirtualFile buildVirtualFileObject(OdetteFtpSession session, CommandExchangeBuffer sfid)
    		throws OdetteFtpException {

    	DefaultVirtualFile vf = super.buildVirtualFileObject(session, sfid);

    	String fileTime = sfid.getStringAttribute(SFIDTIME_FIELD);
        short ticker = parseTimeTicker(fileTime);

        vf.setTicker(ticker);

    	return vf;
    }

    @Override
    public void negativeEndReponseReceived(OdetteFtpSession session, CommandExchangeBuffer nerp) throws OdetteFtpException {

        /* Parse Negative End Response info. */
        DeliveryNotification notif = buildNegativeEndResponse(nerp);

        /*
         * Indicate the negative end response acknowledgment on the odette-ftp
         * support provider.
         */
        Oftplet oftplet = getSessionOftplet(session);
        OftpletListener oftpletListener = oftplet.getListener();

        if (oftpletListener != null) {

            LOGGER.debug("[{}] NERP received. Invoking the onNotificationReceived() on the Oftplet Listener: {}",
                    session, oftpletListener);

            oftpletListener.onNotificationReceived(notif);

        } else {

            LOGGER.warn("[{}] NERP received. Cannot indicate to the Oftplet Listener - returned null: {}", session,
                    oftplet);
        }

        /* Reply with a Ready to Receive. */
        CommandExchangeBuffer rtr = buildReadyToReceiveCommand();
        session.write(rtr);

    }

    @Override
    protected CommandExchangeBuffer buildDeliveryNotificationCommand(DeliveryNotification notif) {

        if (notif.getType() == EndResponseType.END_TO_END_RESPONSE) {
            return endToEndResponse(notif.getDatasetName(), notif.getDateTime(), notif.getTicker(), notif.getUserData(), notif
                    .getDestination(), notif.getOriginator());
        } else {
            return negativeEndResponse(notif.getDatasetName(), notif.getDateTime(), notif.getTicker(), notif.getDestination(), notif
                    .getOriginator(), notif.getCreator(), notif.getReason());
        }

    }

    @Override
    protected DeliveryNotification buildNegativeEndResponse(CommandExchangeBuffer nerp) throws OdetteFtpException {

        /* Retrieve parameter values from the command exchange buffer. */
        String datasetName = nerp.getStringAttribute(NERPDSN_FIELD);
        String fileDate = nerp.getStringAttribute(NERPDATE_FIELD);
        String fileTime = nerp.getStringAttribute(NERPTIME_FIELD);
        String destination = nerp.getStringAttribute(NERPDEST_FIELD);
        String originator = nerp.getStringAttribute(NERPORIG_FIELD);
        String creator = nerp.getStringAttribute(NERPCREA_FIELD);

        NegativeResponseReason reason = NegativeResponseReason.parse(nerp.getStringAttribute(NERPREAS_FIELD));

        Date fileDateTime = parseDateTime(fileDate, fileTime);
        short ticker = parseTimeTicker(fileTime);

        /* Prepare the File Delivery acknowledgment data object. */
        DefaultDeliveryNotification notif = new DefaultDeliveryNotification(EndResponseType.NEGATIVE_END_RESPONSE);
        notif.setDatasetName(datasetName);
        notif.setDateTime(fileDateTime);
        notif.setTicker(ticker);
        notif.setDestination(destination);
        notif.setOriginator(originator);
        notif.setCreator(creator);
        notif.setReason(reason);

        return notif;
    }

	public short parseTimeTicker(String fileTime) {
		return Short.parseShort(fileTime.substring(6));
	}

	@Override
    public Date parseDateTime(String sdate, String stime) {
        // date format: yyyymmdd
        int year = Integer.parseInt(sdate.substring(0, 4));
        int month = Integer.parseInt(sdate.substring(4, 6));
        int day = Integer.parseInt(sdate.substring(6, 8));

        // time format: hhmmss
        int hour = Integer.parseInt(stime.substring(0, 2));
        int minute = Integer.parseInt(stime.substring(2, 4));
        int second = Integer.parseInt(stime.substring(4, 6));

        return ProtocolUtil.createDate(year, month, day, hour, minute, second, 0);
    }
}
