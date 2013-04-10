/**
 * Neociclo Accord, Open Source B2Bi Middleware
 * Copyright (C) 2005-2010 Neociclo, http://www.neociclo.com
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
package org.neociclo.odetteftp.util;

import static org.neociclo.odetteftp.protocol.v20.SecurityLevel.*;
import static org.neociclo.odetteftp.protocol.v20.FileEnveloping.*;
import static org.neociclo.odetteftp.protocol.v20.FileCompression.*;
import static org.neociclo.odetteftp.protocol.v20.CipherSuite.*;
import static org.neociclo.odetteftp.util.EnvelopingUtil.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;

import org.neociclo.odetteftp.protocol.DefaultDeliveryNotification;
import org.neociclo.odetteftp.protocol.DeliveryNotification;
import org.neociclo.odetteftp.protocol.NegativeResponseReason;
import org.neociclo.odetteftp.protocol.VirtualFile;
import org.neociclo.odetteftp.protocol.DeliveryNotification.EndResponseType;
import org.neociclo.odetteftp.protocol.v20.CipherSuite;
import org.neociclo.odetteftp.protocol.v20.DefaultSignedDeliveryNotification;
import org.neociclo.odetteftp.protocol.v20.EnvelopedVirtualFile;
import org.neociclo.odetteftp.protocol.v20.FileCompression;
import org.neociclo.odetteftp.protocol.v20.FileEnveloping;
import org.neociclo.odetteftp.protocol.v20.SecurityLevel;
import org.neociclo.odetteftp.protocol.v20.SignedDeliveryNotification;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class OdetteFtpSupport {

	public static void createEnvelopedFile(File input, File output, EnvelopedVirtualFile virtualFile)
			throws EnvelopingException {

		createEnvelopedFile(input, output, virtualFile, null, null, null);
	}

	public static void createEnvelopedFile(File input, File output, EnvelopedVirtualFile virtualFile,
			X509Certificate userCert, PrivateKey userPrivateKey, X509Certificate partnerCert)
			throws EnvelopingException {

		SecurityLevel securityLevel = (virtualFile.getSecurityLevel() == null ? NO_SECURITY_SERVICES : virtualFile.getSecurityLevel());
		CipherSuite cipherSel = (virtualFile.getCipherSuite() == null ? NO_CIPHER_SUITE_SELECTION : virtualFile.getCipherSuite());
		FileCompression compressionAlgo = (virtualFile.getCompressionAlgorithm() == null ? NO_COMPRESSION : virtualFile.getCompressionAlgorithm());
		FileEnveloping envelopingFormat = (virtualFile.getEnvelopingFormat() == null ? NO_ENVELOPE : virtualFile.getEnvelopingFormat());

		createEnvelopedFile(input, output, securityLevel, cipherSel, compressionAlgo, envelopingFormat, partnerCert,
				userCert, userPrivateKey);
	}

	public static void createEnvelopedFile(File input, File output, SecurityLevel securityLevel, CipherSuite cipherSel,
			FileCompression compressionAlgo, FileEnveloping envelopingFormat, X509Certificate partnerCert,
			X509Certificate userCert, PrivateKey userPrivateKey) throws EnvelopingException {

		String[] argNames = { "input", "output", "securityLevel", "cipherSel", "compressionAlgo", "envelopingFormat" };
		Object[] argValues = { input, output, securityLevel, cipherSel, compressionAlgo, envelopingFormat };

		// check null pointer exception in mandatory args
		for (int i = 0; i < argValues.length; i++) {
			if (argValues[i] == null) {
				throw new NullPointerException(argNames[i]);
			}
		}

		if (envelopingFormat == NO_ENVELOPE) {
			throw new EnvelopingException("Cannot parse enveloped file. Incompatible parameter: " +
					"envelopingFormat=NO_ENVELOPE.");
		}

		boolean isSigned = (securityLevel == SIGNED || securityLevel == ENCRYPTED_AND_SIGNED);
		boolean isEncrypted = (securityLevel == ENCRYPTED || securityLevel == ENCRYPTED_AND_SIGNED);
		boolean isCompressed = (compressionAlgo != NO_COMPRESSION);

		if (isSigned) {
			if (cipherSel == NO_CIPHER_SUITE_SELECTION) {
				throw new EnvelopingException("Cannot parse enveloped file. No signature algorithm specified " +
						"(cipherSel=NO_CIPHER_SUITE_SELECTION).");
			}

			if (userCert == null) {
				throw new NullPointerException("userCert");
			}

			if (userPrivateKey == null) {
				throw new NullPointerException("userKey");
			}
		}

		if (isEncrypted) {
			if (cipherSel == NO_CIPHER_SUITE_SELECTION) {
				throw new EnvelopingException("Cannot parse enveloped file. No encryption algorithm specified " +
						"(cipherSel=NO_CIPHER_SUITE_SELECTION).");
			}

			if (partnerCert == null) {
				throw new NullPointerException("partnerCert");
			}
		}

		/*
		 * Enchain output streams in the reverse full-cms order. Also prepare a
		 * list to flush & close streams in the correct sequence. 
		 */
		OutputStream outStream = null;
		try {
			outStream = new FileOutputStream(output, false);
		} catch (FileNotFoundException ouputNotFound) {
			throw new EnvelopingException("Failed to create enveloped file. Cannot open output file: " + output,
					ouputNotFound);
		}

		ArrayList<OutputStream> toClose = new ArrayList<OutputStream>();
		toClose.add(outStream);

		try {
			if (isEncrypted) {
				outStream = openEnvelopedDataStreamGenerator(outStream, cipherSel, partnerCert);
				toClose.add(0, outStream);
			}
			if (isCompressed) {
				outStream = openCompressedDataStreamGenerator(outStream);
				toClose.add(0, outStream);
			}
			if (isSigned) {
				outStream = openSignedDataStreamGenerator(outStream, cipherSel, userCert, userPrivateKey);
				toClose.add(0, outStream);
			}
		} catch (Exception e) {
			throw new EnvelopingException("Failed to create enveloped file. Cannot open CMS output processings.", e);
		}

		// copy data from the input
        FileInputStream inStream = null;
		try {

			// TODO shouldn't use a BufferedInputStream?

			inStream = new FileInputStream(input);
	        IoUtil.copyStream(inStream, outStream);
		} catch (FileNotFoundException notFound) {
			throw new EnvelopingException("Failed to create enveloped file. Source file doesn't exist: " + input,
					notFound);
		} catch (IOException e) {
			throw new EnvelopingException("Failed to create enveloped file. Buffer copying error.", e);
		}

        // perform flush & closings
        try {
			inStream.close();
			for (OutputStream stream : toClose) {
				stream.flush();
				stream.close();
			}
		} catch (IOException e) {
			throw new EnvelopingException("Failed to close enveloped output stream: " + output, e);
		}

	}

	public static void parseEnvelopedFile(File input, File output, EnvelopedVirtualFile virtualFile,
			X509Certificate userCert, PrivateKey userPrivateKey, X509Certificate partnerCert)
			throws EnvelopingException {

		parseEnvelopedFile(input, output, virtualFile.getSecurityLevel(), virtualFile.getCipherSuite(),
				virtualFile.getCompressionAlgorithm(), virtualFile.getEnvelopingFormat(), userCert, userPrivateKey,
				partnerCert);

	}

	/**
	 * Maybe output file is generated even when an exception is thrown.
	 *
	 * @param input
	 * @param output
	 * @param securityLevel
	 * @param cipherSel
	 * @param compressionAlgo
	 * @param envelopingFormat
	 * @param userCert
	 * @param userPrivateKey
	 * @param partnerCert
	 * @throws EnvelopingException
	 * @throws SignatureCheckException
	 */
	public static void parseEnvelopedFile(File input, File output, SecurityLevel securityLevel, CipherSuite cipherSel,
			FileCompression compressionAlgo, FileEnveloping envelopingFormat, X509Certificate userCert,
			PrivateKey userPrivateKey, X509Certificate partnerCert) throws EnvelopingException {
		
		String[] argNames = { "input", "output", "securityLevel", "cipherSel", "compressionAlgo", "envelopingFormat" };
		Object[] argValues = { input, output, securityLevel, cipherSel, compressionAlgo, envelopingFormat };

		// check null pointer exception in mandatory args
		for (int i = 0; i < argValues.length; i++) {
			if (argValues[i] == null) {
				throw new NullPointerException(argNames[i]);
			}
		}

		if (envelopingFormat == NO_ENVELOPE) {
			throw new EnvelopingException("Cannot create unenveloped file. Incompatible parameter: " +
					"envelopingFormat=NO_ENVELOPE.");
		}

		boolean isSigned = (securityLevel == SIGNED || securityLevel == ENCRYPTED_AND_SIGNED);
		boolean isEncrypted = (securityLevel == ENCRYPTED || securityLevel == ENCRYPTED_AND_SIGNED);
		boolean isCompressed = (compressionAlgo != NO_COMPRESSION);

		if (isSigned) {
			if (cipherSel == NO_CIPHER_SUITE_SELECTION) {
				throw new EnvelopingException("Cannot create unenveloped file. No signature algorithm specified " +
						"(cipherSel=NO_CIPHER_SUITE_SELECTION).");
			}

			if (partnerCert == null) {
				throw new NullPointerException("partnerCert");
			}
		}

		if (isEncrypted) {
			if (cipherSel == NO_CIPHER_SUITE_SELECTION) {
				throw new EnvelopingException("Cannot create unenveloped file. No encryption algorithm specified " +
						"(cipherSel=NO_CIPHER_SUITE_SELECTION).");
			}

			if (userCert == null) {
				throw new NullPointerException("userCert");
			}

			if (userPrivateKey == null) {
				throw new NullPointerException("userKey");
			}
		}

		/*
		 * Enchain input streams in the reverse full-cms order. Also prepare a
		 * list to close streams in the correct sequence. 
		 */
		InputStream inStream = null;
		try {
			// TODO shouldn't use a BufferedInputStream too?
			inStream = new FileInputStream(input);
		} catch (FileNotFoundException inputNotFound) {
			throw new EnvelopingException("Failed to parse unenveloped file. Cannot open input file: " + input,
					inputNotFound);
		}

		ArrayList<InputStream> toClose = new ArrayList<InputStream>();
		toClose.add(inStream);

		SignatureVerifyResult signatureVerification = new SignatureVerifyResult();

		try {
			if (isEncrypted) {
				inStream = openEnvelopedDataParser(inStream, userCert, userPrivateKey);
				toClose.add(0, inStream);
			}
			if (isCompressed) {
				inStream = openCompressedDataParser(inStream);
				toClose.add(0, inStream);
			}
			if (isSigned) {
				inStream = openSignedDataParser(inStream, partnerCert, signatureVerification);
				toClose.add(0, inStream);
			}
		} catch (Exception e) {
			throw new EnvelopingException("Failed to create unenveloped file. Cannot open CMS input processings.", e);
		}

		// copy data to the output
        FileOutputStream outStream = null;
		try {
			outStream = new FileOutputStream(output);
	        IoUtil.copyStream(inStream, outStream);
		} catch (FileNotFoundException notFound) {
			throw new EnvelopingException("Failed to create unenveloped file. Cannot create output file: " + output,
					notFound);
		} catch (IOException e) {
			throw new EnvelopingException("Failed to create unenveloped file. Buffer copying error.", e);
		}

        // perform flush & closings
        try {
        	outStream.flush();
			outStream.close();
			for (InputStream stream : toClose) {
				stream.close();
			}
		} catch (IOException e) {
			throw new EnvelopingException("Failed to close stream.", e);
		}

		// evaluate the signature verification result
		if (signatureVerification.hasFailed()) {
			throw new SignatureCheckException("An error occurred on signature verify.",
					signatureVerification.getExceptionCaught());
		}
		
	}

	public static DeliveryNotification getReplyDeliveryNotification(VirtualFile incomingVirtualFile) {
		String creator = incomingVirtualFile.getOriginator();
		return getReplyDeliveryNotification(incomingVirtualFile, creator);
	}

	public static DeliveryNotification getReplyDeliveryNotification(VirtualFile incomingVirtualFile, String creator) {
		return getReplyDeliveryNotification(incomingVirtualFile, creator, null, null);
	}

	public static DeliveryNotification getReplyDeliveryNotification(VirtualFile incomingVirtualFile, String creator,
			NegativeResponseReason reason, String negativeReasonText) {
		if (incomingVirtualFile == null) {
			throw new NullPointerException("incomingVirtualFile");
		} else if (incomingVirtualFile instanceof EnvelopedVirtualFile) {
			try {
				return getReplySignedDeliveryNotification((EnvelopedVirtualFile) incomingVirtualFile, creator, reason,
						negativeReasonText, null);
			} catch (Exception e) {
				return getReplySignedDeliveryNotification((EnvelopedVirtualFile) incomingVirtualFile, creator, reason,
						negativeReasonText, null, null);
			}
		} else {
			return replyNormalDeliveryNotification(incomingVirtualFile, creator, reason, negativeReasonText);
		}
	}

	/**
	 * Prepare the reply Signed Delivery Notification. Set automatically the
	 * computed the Virtual File hash.
	 * 
	 * @param incomingVirtualFile
	 * @param creator
	 * @param reason
	 * @param negativeReasonText
	 * @param signature
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 * @throws IOException
	 */
	public static SignedDeliveryNotification getReplySignedDeliveryNotification(
			EnvelopedVirtualFile incomingVirtualFile, String creator, NegativeResponseReason reason,
			String negativeReasonText, byte[] signature) throws NoSuchAlgorithmException, NoSuchProviderException,
			IOException {

		byte[] virtualFileHash = null;

		if (incomingVirtualFile.getFile() != null) {
			String algorithm = EnvelopingUtil.asDigestAlgorithm(incomingVirtualFile.getCipherSuite());
			if (algorithm != null) {
				virtualFileHash = SecurityUtil.computeFileHash(incomingVirtualFile.getFile(), algorithm);
			}
		}

		return getReplySignedDeliveryNotification(incomingVirtualFile, creator, reason, negativeReasonText,
				virtualFileHash, signature);
	}

	public static SignedDeliveryNotification getReplySignedDeliveryNotification(
			EnvelopedVirtualFile incomingVirtualFile, String creator, NegativeResponseReason reason,
			String negativeReasonText, byte[] virtualFileHash, byte[] signature) {
		return replySignedDeliveryNotification(incomingVirtualFile, creator, reason, negativeReasonText,
				virtualFileHash, signature);
	}

	private static DeliveryNotification replyNormalDeliveryNotification(VirtualFile vf, String creator,
			NegativeResponseReason reason, String negativeReasonText) {

		EndResponseType type = (reason == null ? EndResponseType.END_TO_END_RESPONSE
				: EndResponseType.NEGATIVE_END_RESPONSE);

		DefaultDeliveryNotification notif = new DefaultDeliveryNotification(type);
		setNotifBasicInfo(notif, vf, creator, reason, negativeReasonText);

		return notif;
	}

	private static SignedDeliveryNotification replySignedDeliveryNotification(EnvelopedVirtualFile vf, String creator,
			NegativeResponseReason reason, String negativeReasonText, byte[] virtualFileHash, byte[] notifSignature) {

		EndResponseType type = (reason == null ? EndResponseType.END_TO_END_RESPONSE
				: EndResponseType.NEGATIVE_END_RESPONSE);

		DefaultSignedDeliveryNotification notif = new DefaultSignedDeliveryNotification(type);
		setNotifBasicInfo(notif, vf, creator, reason, negativeReasonText);

		notif.setVirtualFileHash(virtualFileHash);
		notif.setNotificationSignature(notifSignature);

		return notif;
	}

	private static void setNotifBasicInfo(DefaultDeliveryNotification notif, VirtualFile vf, String creator,
			NegativeResponseReason reason, String negativeReasonText) {

		notif.setDatasetName(vf.getDatasetName());
		notif.setDateTime(new Date(vf.getDateTime().getTime()));
		notif.setTicker(vf.getTicker());
		notif.setOriginator(vf.getDestination());
		notif.setDestination(vf.getOriginator());
		notif.setUserData(vf.getUserData());

		notif.setCreator(creator);
		notif.setReason(reason);
		notif.setReasonText(negativeReasonText);

	}

	private OdetteFtpSupport() {
	}

}
