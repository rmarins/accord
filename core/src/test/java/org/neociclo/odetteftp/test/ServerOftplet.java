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
package org.neociclo.odetteftp.test;

import static org.neociclo.odetteftp.protocol.EndSessionReason.INVALID_PASSWORD;
import static org.neociclo.odetteftp.protocol.EndSessionReason.RESOURCES_NOT_AVAIABLE;
import static org.neociclo.odetteftp.protocol.EndSessionReason.UNKNOWN_USER_CODE;
import static org.neociclo.odetteftp.protocol.RecordFormat.FIXED;
import static org.neociclo.odetteftp.protocol.RecordFormat.TEXTFILE;
import static org.neociclo.odetteftp.protocol.RecordFormat.UNSTRUCTURED;
import static org.neociclo.odetteftp.protocol.RecordFormat.VARIABLE;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;

import org.neociclo.odetteftp.EntityState;
import org.neociclo.odetteftp.MappingMode;
import org.neociclo.odetteftp.OdetteFtpException;
import org.neociclo.odetteftp.OdetteFtpSession;
import org.neociclo.odetteftp.TransferMode;
import org.neociclo.odetteftp.protocol.AnswerReason;
import org.neociclo.odetteftp.protocol.DeliveryNotificationInfo;
import org.neociclo.odetteftp.protocol.EndSessionException;
import org.neociclo.odetteftp.protocol.FileTransferException;
import org.neociclo.odetteftp.protocol.NegativeResponseReason;
import org.neociclo.odetteftp.protocol.OdetteFtpExchange;
import org.neociclo.odetteftp.protocol.RecordFormat;
import org.neociclo.odetteftp.protocol.VirtualFileInfo;
import org.neociclo.odetteftp.protocol.DeliveryNotificationInfo.EndResponseType;
import org.neociclo.odetteftp.support.AbstractVirtualFile;
import org.neociclo.odetteftp.support.DefaultVirtualFile;
import org.neociclo.odetteftp.support.OftpletAdapter;
import org.neociclo.odetteftp.support.VirtualFile;
import org.neociclo.odetteftp.util.IoUtil;
import org.neociclo.odetteftp.util.SessionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class ServerOftplet extends OftpletAdapter {

    // Inner classes declarations
    // -------------------------------------------------------------------------

    private static class ExchangeTestIterator implements Iterator<OdetteFtpExchange> {

        private Iterator<File> files;

        private File current;

        public ExchangeTestIterator(Iterator<File> exchangeFilesIt) {
            super();
            this.files = exchangeFilesIt;
        }

        public boolean hasNext() {
            return files.hasNext();
        }

        public OdetteFtpExchange next() {

            current = files.next();
            String fileName = current.getName();

            String metadataFileName = getMetadataFileName(fileName);
            File metadataFile = new File(current.getParentFile(), metadataFileName);

            VirtualFileInfo exchangeInfo;

            /* Use exchange info from the exchange metadata file. */
            if (metadataFile.exists()) {
                exchangeInfo = loadVirtualFile(metadataFile);
            }
            /* Use exchange info retrieved from filename & default. */
            else {
                exchangeInfo = new VirtualFileInfo();
                exchangeInfo.setDatasetName(getMailboxFileDatasetName(fileName));
                exchangeInfo.setDateTime(getMailboxFileDateTime(fileName));
                exchangeInfo.setDestination(getMailboxFileOriginator(fileName));
                exchangeInfo.setOriginator(getMailboxFileDestination(fileName));

                // defaults
                exchangeInfo.setRecordSize(0);
                exchangeInfo.setRecordFormat(RecordFormat.UNSTRUCTURED);
                exchangeInfo.setRestartOffset(0);
            }

            /*
             * Open the current file as Virtual File object and set exchange
             * date & time.
             */
            DefaultVirtualFile virtualFile = new DefaultVirtualFile(exchangeInfo.getDatasetName(), current,
                    MappingMode.INPUT, exchangeInfo.getRecordFormat(), exchangeInfo.getRecordSize());

            virtualFile.setDateTime(exchangeInfo.getDateTime());

            /* Prepare the returning exchange expected by the API. */
            OdetteFtpExchange exchange = createExchange(virtualFile, exchangeInfo);

            return exchange;
        }

        public void remove() {
            if (!current.delete()) {
                LOG.error("Cannot delete exchange file: " + current.getAbsolutePath());
            }
            current = null;
        }

    }

    private static class DeliveryNotificationTestIterator implements Iterator<DeliveryNotificationInfo> {

        private static final Logger LOG = LoggerFactory.getLogger(DeliveryNotificationTestIterator.class);

        private Iterator<File> files;

        private File current;

        public DeliveryNotificationTestIterator(Iterator<File> notifFilesIt) {
            super();
            this.files = notifFilesIt;
        }

        public boolean hasNext() {
            return files.hasNext();
        }

        public DeliveryNotificationInfo next() {
            current = files.next();
            DeliveryNotificationInfo notif = loadDeliveryNotification(current);
            return notif;
        }

        public void remove() {
            if (!current.delete()) {
                LOG.error("Cannot delete delivery notification file: " + current.getAbsolutePath());
            }
            current = null;
        }

    }

    // Constants
    // -------------------------------------------------------------------------

    private static final String EXTENSION_NOTIF = "notif";

    private static final String EXTENSION_FILE = "file";

    private static final char MAILBOX_FILENAME_SEPARATOR = '$';

    private static final SimpleDateFormat EXCHANGE_DATE_FORMATTER = new SimpleDateFormat("yyyyMMddHHmmss-SSS");

    private static final String METADATA_COMMENTS = " Accord ODETTE-FTP Library API (cenerated file)";

    private static final Logger LOG = LoggerFactory.getLogger(ServerOftplet.class);

    // Static methods
    // -------------------------------------------------------------------------

    public static String getMetadataFileName(String fileName) {
        return fileName + "-metadata";
    }

    /**
     * 
     * @param data
     *            incoming virtual file info
     * @return 
     *         &lt;timestamp&gt;$&lt;originator&gt;$&lt;destination&gt;$&lt;datasetName
     *         &gt;.file
     */
    public static String getMailboxFileName(VirtualFileInfo data) {
        StringBuffer sb = new StringBuffer();
        sb.append(EXCHANGE_DATE_FORMATTER.format(data.getDateTime())).append(MAILBOX_FILENAME_SEPARATOR);
        sb.append(data.getOriginator()).append(MAILBOX_FILENAME_SEPARATOR);
        sb.append(data.getDestination()).append(MAILBOX_FILENAME_SEPARATOR);
        sb.append(data.getDatasetName()).append('.').append(EXTENSION_FILE);
        return sb.toString().toLowerCase();
    }

    /**
     * 
     * @param data
     *            incoming delivery notification info
     * @return 
     *         &lt;timestamp&gt;$&lt;originator&gt;$&lt;destination&gt;$&lt;datasetName
     *         &gt;.file
     */
    public static String getMailboxFileName(DeliveryNotificationInfo data) {
        StringBuffer sb = new StringBuffer();
        sb.append(EXCHANGE_DATE_FORMATTER.format(data.getDateTime())).append(MAILBOX_FILENAME_SEPARATOR);
        sb.append(data.getOriginator()).append(MAILBOX_FILENAME_SEPARATOR);
        sb.append(data.getDestination()).append(MAILBOX_FILENAME_SEPARATOR);
        sb.append(data.getDatasetName()).append('.').append(EXTENSION_NOTIF);
        return sb.toString().toLowerCase();
    }

    public static Date getMailboxFileDateTime(String fileName) {
        String textDateTime = fileName.substring(0, EXCHANGE_DATE_FORMATTER.toPattern().length());
        try {
            return EXCHANGE_DATE_FORMATTER.parse(textDateTime);
        } catch (ParseException e) {
            LOG.error("Cannot parse file date & time: " + fileName, e);
            return null;
        }
    }

    public static String getMailboxFileOriginator(String fileName) {
        int posStart = fileName.indexOf(MAILBOX_FILENAME_SEPARATOR) + 1;
        int posEnd = fileName.indexOf(MAILBOX_FILENAME_SEPARATOR, posStart);
        return fileName.substring(posStart, posEnd);
    }

    public static String getMailboxFileDestination(String fileName) {
        int posEnd = fileName.lastIndexOf(MAILBOX_FILENAME_SEPARATOR);
        int posStart = fileName.lastIndexOf(MAILBOX_FILENAME_SEPARATOR, posEnd - 1) + 1;
        return fileName.substring(posStart, posEnd);
    }

    public static String getMailboxFileDatasetName(String fileName) {
        int posStart = fileName.lastIndexOf(MAILBOX_FILENAME_SEPARATOR) + 1;
        int posEnd = fileName.lastIndexOf('.');
        return fileName.substring(posStart, posEnd);
    }

    public static VirtualFileInfo loadVirtualFile(File vfdata) {

        Properties info = new Properties();

        FileInputStream inStream = null;
        try {
            inStream = new FileInputStream(vfdata);
            info.load(inStream);
        } catch (FileNotFoundException e) {
            LOG.error("Cannot load Virtual File metadata. File not found: " + vfdata.getAbsolutePath() + ".", e);
            return null;
        } catch (IOException e) {
            LOG.error("Cannot load Virtual File metadata. I/O error on file: " + vfdata.getAbsolutePath() + ".", e);
            return null;
        }

        VirtualFileInfo data = new VirtualFileInfo();

        String textDateTime = info.getProperty("accord.oftp.vf.dateTime");
        try {
            data.setDateTime(EXCHANGE_DATE_FORMATTER.parse(textDateTime));
        } catch (ParseException e) {
            LOG.error("Cannot load Virtual File metadata. Date & Time parse failed: " + textDateTime + " - "
                    + vfdata.getAbsolutePath(), e);
            return null;
        }

        data.setDatasetName(info.getProperty("accord.oftp.vf.datasetName"));
        data.setDestination(info.getProperty("accord.oftp.vf.destination"));
        data.setFileSize(Long.parseLong(info.getProperty("accord.oftp.vf.fileSize")));
        data.setOriginator(info.getProperty("accord.oftp.vf.originator"));
        data.setRecordFormat(RecordFormat.valueOf(info.getProperty("accord.oftp.vf.recordFormat")));
        data.setRestartOffset(Long.parseLong(info.getProperty("accord.oftp.vf.restart")));
        data.setUserData(info.getProperty("accord.oftp.vf.userData"));

        return data;
    }

    public static void storeVirtualFile(File vfdata, VirtualFileInfo data) {

        Properties info = new Properties();
        info.put("accord.oftp.vf.datasetName", data.getDatasetName());
        info.put("accord.oftp.vf.dateTime", EXCHANGE_DATE_FORMATTER.format(data.getDateTime()));
        info.put("accord.oftp.vf.fileSize", Long.toString(data.getSize()));
        info.put("accord.oftp.vf.originator", data.getOriginator());
        info.put("accord.oftp.vf.recordFormat", data.getRecordFormat().name());
        info.put("accord.oftp.vf.destination", data.getDestination());
        info.put("accord.oftp.vf.restart", Long.toString(data.getRestartOffset()));
        info.put("accord.oftp.vf.userData", data.getUserData());

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(vfdata);
            info.store(out, METADATA_COMMENTS);
        } catch (FileNotFoundException e) {
            LOG.error("Cannot store Virtual File metadata. File not found: " + vfdata.getAbsolutePath() + ". " + data,
                    e);
            return;
        } catch (IOException e) {
            LOG.error("Cannot store Virtual File metadata. I/O error on file: " + vfdata.getAbsolutePath() + ". "
                    + data, e);
            return;
        } finally {
            try {
                out.close();
            } catch (Throwable e) {
                // ignore
            }
        }

    }

    public static DeliveryNotificationInfo loadDeliveryNotification(File ackFile) {

        Properties notif = new Properties();

        FileInputStream inStream = null;
        try {
            inStream = new FileInputStream(ackFile);
            notif.load(inStream);
        } catch (FileNotFoundException e) {
            LOG.error("Cannot load delivery notification. File not found: " + ackFile.getAbsolutePath() + ".", e);
            return null;
        } catch (IOException e) {
            LOG.error("Cannot load delivery notification. I/O error on file: " + ackFile.getAbsolutePath() + ".", e);
            return null;
        }

        EndResponseType notifType = EndResponseType.valueOf((String) notif.getProperty("accord.oftp.notif.type"));
        DeliveryNotificationInfo data = new DeliveryNotificationInfo(notifType);

        /* Parse and set date & time. */
        String dt = notif.getProperty("accord.oftp.notif.dateTime");
        try {
            data.setDateTime(EXCHANGE_DATE_FORMATTER.parse(dt));
        } catch (ParseException e) {
            LOG.error("Cannot load delivery notification. Date & Time parse failed: " + dt + " - "
                    + ackFile.getAbsolutePath(), e);
            return null;
        }

        data.setOriginator(notif.getProperty("accord.oftp.notif.originator"));
        data.setDestination(notif.getProperty("accord.oftp.notif.destination"));
        data.setDatasetName(notif.getProperty("accord.oftp.notif.datasetName"));
        data.setUserData(notif.getProperty("accord.oftp.notif.userData"));

        if (notifType == EndResponseType.NEGATIVE_END_RESPONSE) {
            data.setCreator(notif.getProperty("accord.oftp.notif.creator"));
            data.setReason(NegativeResponseReason.valueOf(notif.getProperty("accord.oftp.notif.reason")));
        }

        return data;
    }

    public static void storeDeliveryNotification(File ackFile, DeliveryNotificationInfo data) {

        Properties notif = new Properties();
        notif.put("accord.oftp.notif.type", data.getType().name());
        notif.put("accord.oftp.notif.dateTime", EXCHANGE_DATE_FORMATTER.format(data.getDateTime()));
        notif.put("accord.oftp.notif.originator", data.getOriginator());
        notif.put("accord.oftp.notif.destination", data.getDestination());
        notif.put("accord.oftp.notif.datasetName", data.getDatasetName());
        notif.put("accord.oftp.notif.userData", data.getUserData());

        if (data.getType() == EndResponseType.NEGATIVE_END_RESPONSE) {
            notif.put("accord.oftp.notif.creator", data.getCreator());
            notif.put("accord.oftp.notif.reason", data.getEndSessionReason().name());
        }

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(ackFile);
            notif.store(out, METADATA_COMMENTS);
        } catch (FileNotFoundException e) {
            LOG.error("Cannot store delivery notification. File not found: " + ackFile.getAbsolutePath() + ". " + data,
                    e);
            return;
        } catch (IOException e) {
            LOG.error("Cannot store delivery notification. I/O error on file: " + ackFile.getAbsolutePath() + ". "
                    + data, e);
            return;
        } finally {
            try {
                out.close();
            } catch (Throwable e) {
                // ignore
            }
        }

    }

    protected static OdetteFtpExchange createExchange(DefaultVirtualFile virtualFile, VirtualFileInfo info) {
        OdetteFtpExchange exchange = new OdetteFtpExchange();
        exchange.setVirtualFile(virtualFile);

        exchange.setOriginator(info.getDestination());
        exchange.setDestination(info.getDestination());
        exchange.setUserData(info.getUserData());

        return exchange;
    }

    private static void initUserDir(File userDir) throws IOException {
        File mailboxDir = getMailboxDir(userDir);
        File spoolDir = getSpoolDir(userDir);

        if (!mailboxDir.exists()) {
            mailboxDir.mkdirs();
        }

        if (!spoolDir.exists()) {
            spoolDir.mkdirs();
        }
    }

    private static File getSpoolDir(File userDir) {
        return new File(userDir, "spool");
    }

    private static File getMailboxDir(File userDir) {
        return new File(userDir, "mailbox");
    }

    // Field members
    // -------------------------------------------------------------------------

    private File testDir;
    private MailboxManager mailboxManager;

    private String userCode;
    private File userDir;

    private OdetteFtpSession session;
    private ExchangeHolder currentTransfer;
    private DeliveryNotificationTestIterator deliveryNotificationIt;
    private ExchangeTestIterator exchangeIt;

    private TransferMode transferMode;

    // Constructors
    // -------------------------------------------------------------------------

    public ServerOftplet(File testDir, MailboxManager mailboxManager, TransferMode mode) {
        super();
        this.testDir = testDir;
        this.mailboxManager = mailboxManager;
        this.transferMode = mode;
    }

    // Implementation specific methods
    // -------------------------------------------------------------------------

    private ExchangeHolder openTransfer(String fileName, VirtualFileInfo data) throws OdetteFtpException {

        this.currentTransfer = null;

        // Verify if the file exists in SPOOL dir
        File spoolFile = new File(getSpoolDir(userDir), fileName);

        /* Restart the transfer from last file completed received block. */
        if (spoolFile.exists() && session.isRestartSupported()) {
            this.currentTransfer = restartIncomingTransfer(fileName, data);
        }
        /*
         * Remove transfer from spool. File Transfer does exist in the incoming
         * queue, but restart is not supported. Also delete the receiving
         * physical file from storage spool box (if exists).
         */
        else if (spoolFile.exists() && !session.isRestartSupported()) {
            if (spoolFile.isFile()) {
                IoUtil.delete(spoolFile);
            }
        }

        /* Start a new file transfer. */
        if (currentTransfer == null) {
            currentTransfer = createIncomingTransfer(fileName, data);
        }

        return currentTransfer;
    }

    private ExchangeHolder createIncomingTransfer(String fileName, VirtualFileInfo data) {

        /* Create the virtual file in user mailbox. */
        File incomingFile = new File(getSpoolDir(userDir), fileName);
        DefaultVirtualFile virtualFile = new DefaultVirtualFile(data.getDatasetName(), incomingFile,
                MappingMode.OUTPUT, data.getRecordFormat(), data.getRecordSize());

        /* Return the virtual file transfer holder. */
        ExchangeHolder holder = new ExchangeHolder(virtualFile, data);

        return holder;
    }

    private ExchangeHolder restartIncomingTransfer(String fileName, VirtualFileInfo data) throws FileTransferException {

        /* Pointer to the broken incoming file in SPOOL. */
        File incomingFile = new File(getSpoolDir(userDir), fileName);

        /* Negotiate the lowest restart offset. */
        long restartPosition = data.getRestartOffset();
        long offset = AbstractVirtualFile.getFileSize(incomingFile.length());
        offset = (offset < restartPosition ? offset : restartPosition);

        DefaultVirtualFile vf = new DefaultVirtualFile(data.getDatasetName(), incomingFile, MappingMode.OUTPUT, data
                .getRecordFormat(), data.getRecordSize());

        try {
            if (vf.getRestartOffset() != offset) {
                vf.setRestartOffset(offset);
            }
        } catch (OdetteFtpException e) {
            throw new FileTransferException(AnswerReason.ACCESS_METHOD_FAILURE, true, "Failed to restart on offset: "
                    + offset + ". " + e.getMessage(), e);
        }

        // Return file transfer data holder
        ExchangeHolder holder = new ExchangeHolder(vf, data);

        return holder;
    }

    private void route(File file, String destination, Object data) {

        debug("Routing " + data + "...");

        File destinationUserDir = getUserDir(destination);

        try {
            initUserDir(destinationUserDir);
        } catch (IOException e) {
            LOG.error("Routing failure: " + data, e);
            return;
        }

        File toMailboxDir = getMailboxDir(destinationUserDir);

        try {
            // move the payload file

            IoUtil.move(file, new File(toMailboxDir, file.getName()));

            // move the metadata file
            String fileName = file.getName();
            File metadataFile = new File(file.getParent(), getMetadataFileName(fileName));
            if (metadataFile.exists()) {
                IoUtil.move(metadataFile, new File(toMailboxDir, metadataFile.getName()));
            }
        } catch (IOException e) {
            LOG.error("Routing failure. Cannot move payload or metadata to destination mailbox dir: " + data, e);
            return;
        }
    }

    private void route(File payload, VirtualFileInfo data) {
        route(payload, data.getDestination(), data);
    }

    private void route(File deliveryNotification, DeliveryNotificationInfo data) {
        route(deliveryNotification, data.getDestination(), data);
    }

    // Oftplet implementation (callbacks)
    // -------------------------------------------------------------------------

    @Override
    public void init(OdetteFtpSession session) {
        session.setTransferMode(transferMode);
        this.session = session;
    }

    @Override
    public void destroy() {
        this.session = null;
        this.userCode = null;
        this.userDir = null;
        this.currentTransfer = null;
        this.deliveryNotificationIt = null;
        this.exchangeIt = null;
    }

    @Override
    public void onAuthenticate(String oid, String pwd, String userData) throws OdetteFtpException {
        if (!mailboxManager.existsMailbox(oid)) {
            throw new EndSessionException(UNKNOWN_USER_CODE, "UserCode: " + oid);
        }

        if (!mailboxManager.checkPassword(oid, pwd)) {
            throw new EndSessionException(INVALID_PASSWORD, "UserCode: " + oid + " - Password: " + pwd);
        }

        // authentication succeed
        this.userCode = oid.toUpperCase();
        this.userDir = getUserDir(userCode);

        // initialize user directory structure
        try {
            initUserDir(userDir);
        } catch (IOException e) {
            throw new EndSessionException(RESOURCES_NOT_AVAIABLE, "Cannot init userDir: " + userDir.getAbsolutePath());
        }
    }

    public void onNotification(DeliveryNotificationInfo info) {

        String fileName = getMailboxFileName(info);
        File spoolFile = new File(getSpoolDir(userDir), fileName);

        /* Place the delivery notification into spool directory. */
        if (!spoolFile.exists()) {
            storeDeliveryNotification(spoolFile, info);
        }

        route(spoolFile, info);
    }

    public VirtualFile onReceiveStart(VirtualFileInfo data) throws OdetteFtpException {

        String fileName = getMailboxFileName(data);

        /*
         * Prepare the returning Virtual File wrapped by a file exchange holder
         * to start receiving the transfer. Set as the current file transfer
         * holder.
         */
        currentTransfer = openTransfer(fileName, data);
        DefaultVirtualFile vf = currentTransfer.getVirtualFile();

        /* Save exchange metadata into file system. */
        String metadataFileName = getMetadataFileName(fileName);
        File metadataFile = new File(vf.getFile().getParentFile(), metadataFileName);
        storeVirtualFile(metadataFile, data);

        /* Log begin transfer */
        StringBuffer sb = new StringBuffer("Start File Receive [");
        sb.append(data.getDatasetName()).append(", ").append(data.getDestination());
        sb.append(EXCHANGE_DATE_FORMATTER.format(data.getDateTime())).append("]");

        return vf;
    }

    public boolean onReceiveEnd(long recordCount, long unitCount) throws OdetteFtpException {

        DefaultVirtualFile vf = currentTransfer.getVirtualFile();
        VirtualFileInfo data = currentTransfer.getData();
        currentTransfer = null;

        /* Make end file receive validation. */
        RecordFormat format = vf.getFormat();
        if ((format == FIXED || format == VARIABLE) && recordCount != vf.getRecordCount()) {
            throw new FileTransferException(AnswerReason.INVALID_RECORD_COUNT, true, "Invalid recordCount [Received: "
                    + recordCount + ", Calculated: " + vf.getRecordCount()
                    + "]. The record count should match for FIXED and VARIABLE record format.");
        } else if ((format == UNSTRUCTURED || format == TEXTFILE) && recordCount != 0) {
            throw new FileTransferException(AnswerReason.INVALID_RECORD_COUNT, true, "Invalid recordCount ["
                    + recordCount + "]. For UNSTRUCTURED and TEXTFILE record format the count should be zero.");
        }

        if (vf.getUnitCount() != unitCount) {
            throw new FileTransferException(AnswerReason.INVALID_BYTE_COUNT, true, "Received unitCount is: "
                    + unitCount + " - Local unitCount is: " + vf.getUnitCount());
        }

        /* Route received file. */
        route(vf.getFile(), data);

        /*
         * Change Direction when there are still files or notifications to
         * transmit.
         */
        boolean changeDirection = getTransmitExchanges() != null && getTransmitExchanges().hasNext();
        changeDirection |= getTransmitNotifications() != null && getTransmitNotifications().hasNext();

        /* Log begin transfer */
        StringBuffer sb = new StringBuffer("End File Receive [");
        sb.append(data.getDatasetName()).append(", ").append(data.getDestination());
        sb.append(EXCHANGE_DATE_FORMATTER.format(data.getDateTime())).append("]");

        return changeDirection;
    }

    public void onTransmitStart(OdetteFtpExchange exchange) {

        VirtualFile vf = exchange.getVirtualFile();

        StringBuffer sb = new StringBuffer("Start File Transmission [");
        sb.append(vf.getDatasetName()).append(", ").append(exchange.getOriginator());
        sb.append(EXCHANGE_DATE_FORMATTER.format(vf.getDateTime())).append("]");

        debug(sb.toString());

    }

    public void onTransmitRefusal(AnswerReason reason, String reasonText, boolean retryLater) {
        onTransmitFailure();
    }

    public void onSendFileComplete() {

        OdetteFtpExchange exchange = SessionHelper.getCurrentExchange(session);
        VirtualFile vf = exchange.getVirtualFile();

        StringBuffer sb = new StringBuffer("End File Transmission [");
        sb.append(vf.getDatasetName()).append(", ").append(exchange.getOriginator());
        sb.append(EXCHANGE_DATE_FORMATTER.format(vf.getDateTime())).append("]");

        debug(sb.toString());
    }

    @SuppressWarnings("unchecked")
    public Iterator<DeliveryNotificationInfo> getTransmitNotifications() {
        if (deliveryNotificationIt == null) {
            File[] notifs = getMailboxDir(userDir).listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return EXTENSION_NOTIF.equals(IoUtil.getFilenameExtension(name));
                }
            });

            Iterator<File> notifFiles = Arrays.asList(notifs).iterator();
            deliveryNotificationIt = new DeliveryNotificationTestIterator(notifFiles);
        }
        return deliveryNotificationIt;
    }

    @SuppressWarnings("unchecked")
    public Iterator<OdetteFtpExchange> getTransmitExchanges() {
        if (exchangeIt == null) {
            File[] files = getMailboxDir(userDir).listFiles(new FilenameFilter() { 
                public boolean accept(File dir, String name) {
                    return EXTENSION_FILE.equals(IoUtil.getFilenameExtension(name));
                }
            });
            Iterator<File> exchangeFiles = Arrays.asList(files).iterator();
            exchangeIt = new ExchangeTestIterator(exchangeFiles);
        }
        return exchangeIt;
    }

    @Override
    public void onExceptionCaught(Throwable cause) {

        /*
         * Update and persist the outgoing file transfer restart offset in the
         * client system exchange.
         */
        if (currentTransfer != null && session.getState() == EntityState.SPEAKER) {
            onTransmitFailure();
        }

    }

    protected void onTransmitFailure() {

        if (currentTransfer == null) {
            return;
        }

        DefaultVirtualFile vf = currentTransfer.getVirtualFile();

        String metadataFileName = getMetadataFileName(vf.getFile().getName());
        File metadataFile = new File(vf.getFile().getParentFile(), metadataFileName);

        /* Save restart offset when bytes moved out and metadata exists. */
        long bytesTransfered = session.getOftpTransferedBytes();
        if (bytesTransfered > 0 && metadataFile.exists()) {

            // calculate the transmission break position
            long breakPosition = vf.getRestartOffsetPosition() + bytesTransfered;

            // restore file exchange metadata from filesystem
            VirtualFileInfo data = loadVirtualFile(metadataFile);

            // turn octets break position into blocks unit (restart offset)
            long offset = DefaultVirtualFile.getRestartOffset(breakPosition, vf.getFormat(), vf.getRecordSize());

            // store updated file exchange metadata back into filesystem
            data.setRestartOffset(offset);
            storeVirtualFile(metadataFile, data);

            debug("File Transmission Failed. Exchange metadata updated: " + metadataFile.getAbsolutePath()
                    + " - Restart offset: " + offset);
        }

    }

    protected File getUserDir(String oid) {
        return new File(testDir, oid);
    }

    private void debug(String message) {
        StringBuffer sb = new StringBuffer("ODETTE-FTP SERVER OID(").append(userCode).append(") ");
        sb.append(message);
        LOG.debug(sb.toString());
    }

}
