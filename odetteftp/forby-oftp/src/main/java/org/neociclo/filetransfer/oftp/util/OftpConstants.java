/**
 * Neociclo Accord, Open Source B2Bi Middleware
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
package org.neociclo.filetransfer.oftp.util;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class OftpConstants {

    public static final String RECORD_FORMAT_PROP = "odette-ftp.recordFormat";

    public static final String RECORD_SIZE_PROP = "odette-ftp.recordSize";

    public static final String RESTART_OFFSET_PROP = "odette-ftp.restartOffset";

    public static final String FILE_SIZE_PROP = "odette-ftp.fileSize";

    public static final String DATASET_NAME_PROP = "odette-ftp.datasetName";

    public static final String DATE_TIME_PROP = "odette-ftp.dateTime";

    public static final String DESTINATION_PROP = "odette-ftp.destination";

    public static final String ORIGINATOR_PROP = "odette-ftp.originator";

    public static final String USER_DATA_PROP = "odette-ftp.userData";

    public static final String FILE_DESCRIPTION_PROP = "odette-ftp.fileDescription";
    
    public static final String SECURITY_LEVEL_PROP = "odette-ftp.securityLevel";

    public static final String SIGNED_NOTIF_REQUEST_PROP = "odette-ftp.signedNotificationRequest";

    public static final String ENVELOPING_FORMAT_PROP = "odette-ftp.envelopingFormat";

    public static final String COMPRESSION_ALGORITHM_PROP = "odette-ftp.compressionAlgorithm";

    public static final String CIPHER_SUITE_PROP = "odette-ftp.cipherSuite";

    public static final String ORIGINAL_FILE_SIZE_PROP = "odette-ftp.originalFileSize";

    public static final String NOTIFICATION_SIGNATURE_PROP = "odette-ftp.notif.signature";

    public static final String NOTIFICATION_FILE_HASH_PROP = "odette-ftp.notif.fileHash";

    public static final String NOTIFICATION_CREATOR_PROP = "odette-ftp.notif.creator";

    public static final String NOTIFICATION_NEGATIVE_REASON_PROP = "odette-ftp.notif.negativeReason";

    public static final String NOTIFICATION_REASON_TEXT_PROP = "odette-ftp.notif.reasonText";

    public static final String NOTIFICATION_TYPE_PROP = "odette-ftp.notif.type";

    public static final String RESTART_OFFSET_RESPONSE_HEADER = "odette-ftp.response.restartOffset";

    private OftpConstants() {
    }

}
