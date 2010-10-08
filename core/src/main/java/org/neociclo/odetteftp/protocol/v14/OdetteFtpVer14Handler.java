/**
 * Neociclo Accord, Open Source B2Bi Middleware
 * Copyright (C) 2005-2009 Neociclo, http://www.neociclo.com
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
package org.neociclo.odetteftp.protocol.v14;

import static org.neociclo.odetteftp.protocol.v14.CommandBuilderVer14.*;
import static org.neociclo.odetteftp.util.CommandFormatConstants.*;
import static org.neociclo.odetteftp.util.SessionHelper.*;

import java.util.Date;

import org.neociclo.odetteftp.OdetteFtpException;
import org.neociclo.odetteftp.OdetteFtpSession;
import org.neociclo.odetteftp.oftplet.Oftplet;
import org.neociclo.odetteftp.oftplet.OftpletListener;
import org.neociclo.odetteftp.protocol.CommandExchangeBuffer;
import org.neociclo.odetteftp.protocol.DefaultDeliveryNotification;
import org.neociclo.odetteftp.protocol.DeliveryNotification;
import org.neociclo.odetteftp.protocol.VirtualFile;
import org.neociclo.odetteftp.protocol.NegativeResponseReason;
import org.neociclo.odetteftp.protocol.DeliveryNotification.EndResponseType;
import org.neociclo.odetteftp.protocol.v13.OdetteFtpVer13Handler;
import org.neociclo.odetteftp.util.ProtocolUtil;
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
        return startFile(vf.getDatasetName(), vf.getDateTime(), vf.getUserData(), vf.getDestination(), vf
                .getOriginator(), vf.getRecordFormat(), vf.getRecordSize(), vf.getSize(), vf.getRestartOffset());
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
            return endToEndResponse(notif.getDatasetName(), notif.getDateTime(), notif.getUserData(), notif
                    .getDestination(), notif.getOriginator());
        } else {
            return negativeEndResponse(notif.getDatasetName(), notif.getDateTime(), notif.getDestination(), notif
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

        /* Prepare the File Delivery acknowledgment data object. */
        DefaultDeliveryNotification notif = new DefaultDeliveryNotification(EndResponseType.NEGATIVE_END_RESPONSE);
        notif.setDatasetName(datasetName);
        notif.setDateTime(fileDateTime);
        notif.setDestination(destination);
        notif.setOriginator(originator);
        notif.setCreator(creator);
        notif.setReason(reason);

        return notif;
    }

    @Override
    public Date parseDateTime(String sdate, String stime) {
        // date format: yyyymmdd
        int year = Integer.parseInt(sdate.substring(0, 4));
        int month = Integer.parseInt(sdate.substring(4, 6));
        int day = Integer.parseInt(sdate.substring(6, 8));

        // time format: hhmmssSSSS
        int hour = Integer.parseInt(stime.substring(0, 2));
        int minute = Integer.parseInt(stime.substring(2, 4));
        int second = Integer.parseInt(stime.substring(4, 6));
        int millis = Integer.parseInt(stime.substring(6, 10));

        return ProtocolUtil.createDate(year, month, day, hour, minute, second, millis);
    }
}
