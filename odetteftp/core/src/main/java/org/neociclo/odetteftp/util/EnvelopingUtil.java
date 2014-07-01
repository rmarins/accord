/**
 * The Accord Project, http://accordproject.org
 * Copyright (C) 2005-2013 Rafael Marins, http://rafaelmarins.com
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
 */
package org.neociclo.odetteftp.util;

import static org.neociclo.odetteftp.protocol.CommandExchangeBuffer.DEFAULT_PROTOCOL_CHARSET;
import static org.neociclo.odetteftp.protocol.CommandExchangeBuffer.formatAttribute;
import static org.neociclo.odetteftp.protocol.v20.CommandBuilderVer20.formatDate;
import static org.neociclo.odetteftp.protocol.v20.CommandBuilderVer20.formatTime;
import static org.neociclo.odetteftp.protocol.v20.ReleaseFormatVer20.EERP_V20;
import static org.neociclo.odetteftp.util.SecurityUtil.*;
import static org.neociclo.odetteftp.util.ProtocolUtil.padd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.cms.CMSAlgorithm;
import org.bouncycastle.cms.CMSCompressedDataParser;
import org.bouncycastle.cms.CMSCompressedDataStreamGenerator;
import org.bouncycastle.cms.CMSEnvelopedData;
import org.bouncycastle.cms.CMSEnvelopedDataGenerator;
import org.bouncycastle.cms.CMSEnvelopedDataParser;
import org.bouncycastle.cms.CMSEnvelopedDataStreamGenerator;
import org.bouncycastle.cms.CMSEnvelopedGenerator;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSSignedDataParser;
import org.bouncycastle.cms.CMSSignedDataStreamGenerator;
import org.bouncycastle.cms.CMSSignedGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.DefaultCMSSignatureAlgorithmNameGenerator;
import org.bouncycastle.cms.RecipientId;
import org.bouncycastle.cms.RecipientInformation;
import org.bouncycastle.cms.RecipientInformationStore;
import org.bouncycastle.cms.SignerId;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.bc.BcRSASignerInfoVerifierBuilder;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.cms.jcajce.JceKeyTransEnvelopedRecipient;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.bouncycastle.cms.jcajce.ZlibCompressor;
import org.bouncycastle.cms.jcajce.ZlibExpanderProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;
import org.neociclo.odetteftp.protocol.v20.CipherSuite;
import org.neociclo.odetteftp.protocol.v20.DefaultSignedDeliveryNotification;
import org.neociclo.odetteftp.protocol.v20.SignedDeliveryNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rafael Marins
 */
public class EnvelopingUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnvelopingUtil.class);

    /**
     * Generate an EnvelopedData object by encrypting the content using the
     * partner's public certificate with the specified CipherSuite.
     * 
     * @param content
     *            the data to be encrypted
     * @param cipherSel
     *            ODETTE-FTP like cipher suite selection
     * @param cert
     *            partner's public certificate used to produce encrypted data
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws CMSException
     * @throws IOException
     */
    public static byte[] createEnvelopedData(byte[] content, CipherSuite cipherSel, X509Certificate cert)
            throws NoSuchAlgorithmException, NoSuchProviderException, CMSException, IOException, CertificateEncodingException, OperatorCreationException {

        installBouncyCastleProviderIfNecessary();

        // set up the generator
        CMSEnvelopedDataGenerator gen = new CMSEnvelopedDataGenerator();

        // gen.addKeyTransRecipient(cert);
        gen.addRecipientInfoGenerator(
                new JceKeyTransRecipientInfoGenerator(cert)
                .setProvider(BC_PROVIDER));

        // create the enveloped-data object
        CMSEnvelopedData enveloped = gen.generate(
                new CMSProcessableByteArray(content), 
                new JceCMSContentEncryptorBuilder(asCMSAlgorithm(cipherSel))
                .setProvider(BC_PROVIDER)
                .build());

        return enveloped.getEncoded();
    }

    public static void createEnvelopedData(InputStream dataStream, OutputStream outStream, CipherSuite cipherSel,
            X509Certificate cert) throws NoSuchAlgorithmException, NoSuchProviderException, CMSException, IOException, CertificateEncodingException, OperatorCreationException {

        OutputStream enveloped = openEnvelopedDataStreamGenerator(outStream, cipherSel, cert);
        IoUtil.copyStream(dataStream, enveloped);

    }

    public static OutputStream openEnvelopedDataStreamGenerator(OutputStream outStream, CipherSuite cipherSel,
            X509Certificate cert) throws NoSuchAlgorithmException, NoSuchProviderException, CMSException, IOException, CertificateEncodingException, OperatorCreationException {

        installBouncyCastleProviderIfNecessary();

        // set up the generator
        CMSEnvelopedDataStreamGenerator gen = new CMSEnvelopedDataStreamGenerator();

        gen.addRecipientInfoGenerator(new JceKeyTransRecipientInfoGenerator(cert).setProvider(BC_PROVIDER));

        // create the enveloped-data stream
        OutputStream enveloped = gen.open(
                outStream, 
                new JceCMSContentEncryptorBuilder(asCMSAlgorithm(cipherSel))
                .setProvider(BC_PROVIDER).build());

        return enveloped;
    }

	/**
	 * 
	 * @param cryptData
	 *            InputStream of encapsulated encrypted data
	 * @param cert
	 *            user secure certificate used to match the recipient identifier
	 * @param key
	 *            user private key used to decrypt the encapsulated data
	 * @return InputStream the original data stream (decrypted)
	 * @throws CMSException
	 * @throws IOException
	 * @throws NoSuchProviderException
	 */
	public static InputStream openEnvelopedDataParser(InputStream cryptData, X509Certificate cert, PrivateKey key)
			throws CMSException, IOException, NoSuchProviderException {

        installBouncyCastleProviderIfNecessary();

        // set up the parser
        CMSEnvelopedDataParser ep = new CMSEnvelopedDataParser(cryptData);

        // TODO validate the receiving enveloped-data against supported
        // algorithms

        // look for our recipient identifier
        RecipientId recId = new org.bouncycastle.cms.KeyTransRecipientId( 
                new X500Name(cert.getIssuerX500Principal().getName()),
                cert.getSerialNumber());

        RecipientInformationStore recipients = ep.getRecipientInfos();
        RecipientInformation recipient = recipients.get(recId);

        if (recipient != null) {
            // return the decrypting parser InputStream
            InputStream parserStream = recipient.getContentStream(
                    new JceKeyTransEnvelopedRecipient(key)
                    .setProvider(BC_PROVIDER))
                    .getContentStream();
            return parserStream;
        }

        // TODO raise a kind of invalid certificate exception instead of null
        // or recipient not found

        return null;

    }

	/**
	 * 
	 * @param compressedData
	 *            InputStream of encapsulated compressed data
	 * @return InputStream the original (uncompressed) readable data stream
	 * @throws CMSException
	 */
	public static InputStream openCompressedDataParser(InputStream compressedData) throws CMSException {

		// set up the parser and retrieve the original data stream
		CMSCompressedDataParser cp = new CMSCompressedDataParser(compressedData);
		InputStream contentStream = cp.getContent(new ZlibExpanderProvider()).getContentStream();

		return contentStream;
	}

	public static InputStream openSignedDataParser(InputStream sigData, final X509Certificate checkCert)
			throws CMSException, OperatorCreationException {
		return openSignedDataParser(sigData, checkCert, null);
	}

	public static InputStream openSignedDataParser(InputStream sigData, final X509Certificate checkCert,
			final SignatureVerifyResult checkResult) throws CMSException, OperatorCreationException {

        installBouncyCastleProviderIfNecessary();

        // set up the parser
        final CMSSignedDataParser sp = new CMSSignedDataParser(
                new JcaDigestCalculatorProviderBuilder().setProvider("BC").build(),
                sigData);

		// TODO what to do? the validity of the certificate isn't verified here

        //
        // Perform signature verification.
        //
        // Create a runnable block which is executed after the returned
        // input stream is completely read (end of stream is reached). This is
        // strictly important, because we are in a streaming mode the order of
        // the operations is important.
        // 

        final Runnable signatureChecker = new Runnable() {
			public void run() {
				try {
					SignerInformationStore signers = sp.getSignerInfos();

					// lookup signer by matching with the given certificate

					SignerId sigId = new SignerId( 
					        new X500Name(checkCert.getIssuerX500Principal().getName()),
					        checkCert.getSerialNumber());

			        SignerInformation signer = signers.get(sigId);

			        // perform signature verification
			        if (signer != null) {

			        	//
						// verify that the signature is correct and that it was generated
						// when the certificate was current
						//
			            if (signer.verify(
			                    new BcRSASignerInfoVerifierBuilder(
			                            new DefaultCMSSignatureAlgorithmNameGenerator(),
			                            new DefaultSignatureAlgorithmIdentifierFinder(),
			                            new DefaultDigestAlgorithmIdentifierFinder(), 
			                            new BcDigestCalculatorProvider())
			                    .build(new JcaX509CertificateHolder(checkCert)))) {
							// signature verified
							if (checkResult != null) {
								checkResult.setSuccess();
							}
						} else {
							// signature failed!!!
							if (checkResult != null) {
								checkResult.setFailure();
							}
						}
			        	
			        } else {

			        	// signer not found
						if (checkResult != null) {
							checkResult.setError(new Exception("Provided check certificate doesn't match."));
						}
			        }

				} catch (Exception e) {
					if (checkResult != null) {
						checkResult.setError(e);
					}
				}

			}
		};

		//
		// Return content stream from the encapsulated data.
		//
		// A simple input stream is returned, where readable bytes represents
		// the original content data (without signatures) from the encapsulated
		// signed envelope. But a wrapping InputStream is created to execute the
		// signature verification after the buffer is completely read.
		//
        
        final InputStream contentStream =  sp.getSignedContent().getContentStream();

        InputStream endOfStreamSignatureCheckInputStream = new InputStream() {

        	/**
        	 * Used to avoid running the signature checker above multiple times. 
        	 */
			private boolean alreadyReachedEof = false;

			@Override
			public int read() throws IOException {
				int b = contentStream.read();
				if (b == -1 && !alreadyReachedEof ) {
					alreadyReachedEof = true;
					signatureChecker.run();
				}
				return b;
			}
		};

		return endOfStreamSignatureCheckInputStream;

	}

    public static void createSignedData(File data, File output, CipherSuite cipherSuite, X509Certificate cert, PrivateKey key)
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, CMSException, IOException, CertificateEncodingException, OperatorCreationException {

        // open compressed data stream
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(output);
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("Failed to create SignedData. Cannot open output signing data file: "
                    + output.getAbsolutePath() + ". " + e.getMessage());
        }

        // generate compressed data stream
        FileInputStream dataStream = null;
        try {
            dataStream = new FileInputStream(data);
            createSignedData(dataStream, cipherSuite, outStream, cert, key);
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("Failed to create SignedData. Cannot to open input file to sign data: "
                    + data.getAbsolutePath() + ". " + e.getMessage());
        } finally {
            try {
                dataStream.close();
            } catch (Throwable t) {
                // ignore
            }
            try {
                outStream.close();
            } catch (Throwable t) {
                // ignore
            }
        }

    }

    public static void createSignedData(InputStream dataStream, CipherSuite cipherSuite, OutputStream outStream, X509Certificate cert,
            PrivateKey key) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException,
            CMSException, IOException, CertificateEncodingException, OperatorCreationException {

        OutputStream signed = openSignedDataStreamGenerator(outStream, cipherSuite, cert, key);
        IoUtil.copyStream(dataStream, signed);

    }

    public static OutputStream openSignedDataStreamGenerator(OutputStream outStream, CipherSuite cipherSuite, X509Certificate cert, PrivateKey key)
            throws NoSuchAlgorithmException, NoSuchProviderException, CMSException, IOException, InvalidKeyException, CertificateEncodingException, OperatorCreationException {

        installBouncyCastleProviderIfNecessary();

        // set up the generator
        CMSSignedDataStreamGenerator gen = new CMSSignedDataStreamGenerator();
        configureGenerator(gen, cipherSuite, cert, key);
        
        // create the signed-data stream
        OutputStream signed = gen.open(outStream, true);

        return signed;
    }

    private static void configureGenerator(CMSSignedGenerator gen,
            CipherSuite cipherSuite, X509Certificate cert, PrivateKey key)
            throws CertificateEncodingException, CMSException,
            OperatorCreationException {
        List<X509Certificate> certList = new ArrayList<X509Certificate>();
        certList.add(cert);
        Store certStore = new JcaCertStore(certList);
        gen.addCertificates(certStore);
        
        ContentSigner signer = new JcaContentSignerBuilder(asSignatureAlgorithm(cipherSuite)).setProvider(BC_PROVIDER).build(key);
        gen.addSignerInfoGenerator(
                new JcaSignerInfoGeneratorBuilder(
                     new JcaDigestCalculatorProviderBuilder().setProvider(BC_PROVIDER).build())
                     .build(signer, cert));
    }

    public static void createEnvelopedData(File data, File output, CipherSuite cipherSel, X509Certificate cert)
            throws NoSuchAlgorithmException, NoSuchProviderException, CMSException, IOException, CertificateEncodingException, OperatorCreationException {

        // open compressed data stream
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(output);
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("Failed to create EnvelopedData. Cannot open output enveloping data file: "
                    + output.getAbsolutePath() + ". " + e.getMessage());
        }

        // generate compressed data stream
        FileInputStream dataStream = null;
        try {
            dataStream = new FileInputStream(data);
            createEnvelopedData(dataStream, outStream, cipherSel, cert);
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException(
                    "Failed to create EnvelopedData. Cannot to open input file to envelope data: "
                            + data.getAbsolutePath() + ". " + e.getMessage());
        } finally {
            try {
                dataStream.close();
            } catch (Throwable t) {
                // ignore
            }
            try {
                outStream.close();
            } catch (Throwable t) {
                // ignore
            }
        }

    }

    public static void createEnvelopedData(String dataPath, String outputPath, CipherSuite cipherSel,
            X509Certificate cert) throws NoSuchAlgorithmException, NoSuchProviderException, CMSException, IOException, CertificateEncodingException, OperatorCreationException {

        // create input and output file objects
        File input = new File(dataPath);
        File output = new File(outputPath);

        // generate enveloped data stream
        createEnvelopedData(input, output, cipherSel, cert);
    }

    /**
     * Generate a SignedData object using SHA-1 digest.
     * 
     * @param content
     *            the data to be signed
     * @param cipherSuite
     * @param cert
     *            private certificate used in conjunction with private key
     * @param key
     *            private key used to produce the signed-data object
     * @return the encoded signed-data object
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws CMSException
     * @throws IOException
     * @throws CertificateEncodingException 
     * @throws OperatorCreationException 
     */
    public static byte[] createSignedData(byte[] content, CipherSuite cipherSuite, X509Certificate cert, PrivateKey key)
            throws NoSuchAlgorithmException, NoSuchProviderException, CMSException, IOException, CertificateEncodingException, OperatorCreationException {

        installBouncyCastleProviderIfNecessary();

        // set up the generator
        CMSSignedDataGenerator gen = new CMSSignedDataGenerator();

        configureGenerator(gen, cipherSuite, cert, key);

        // create the signed-data object
        CMSTypedData data = new CMSProcessableByteArray(content);
        CMSSignedData signed = gen.generate(data, true);

        return signed.getEncoded();
    }

    /**
     * Return null if certificate's recipientId could not be found within the
     * encoded envelope - typically when using a bad certificate to decrypt the
     * authentication challenge encrypted using other public certificate.
     * 
     * @param encoded
     * @param cert
     * @param key
     * @return
     * @throws NoSuchProviderException
     * @throws CMSException
     * @throws IOException
     */
    public static byte[] parseEnvelopedData(byte[] encoded, X509Certificate cert, PrivateKey key)
            throws NoSuchProviderException, CMSException, IOException {

        installBouncyCastleProviderIfNecessary();

        CMSEnvelopedData enveloped = new CMSEnvelopedData(encoded);

        // TODO validate the receiving enveloped-data against supported
        // algorithms

        // look for our recipient identifier
        RecipientId recId = new org.bouncycastle.cms.KeyTransRecipientId( 
                new X500Name(cert.getIssuerX500Principal().getName()),
                cert.getSerialNumber());

        RecipientInformationStore recipients = enveloped.getRecipientInfos();
        RecipientInformation recipient = recipients.get(recId);

        if (recipient != null) {
            // decrypt the data
            return recipient.getContent(
                    new JceKeyTransEnvelopedRecipient(key)
                    .setProvider(BC_PROVIDER));
        } else {
            return null;
        }

    }

    public static void parseEnvelopedDataContentStream(InputStream envelopedStream, OutputStream outStream,
            X509Certificate cert, PrivateKey key) throws NoSuchProviderException, CMSException, IOException {

        installBouncyCastleProviderIfNecessary();

        // use the CMS parser to decrypt the EnvelopedData
        CMSEnvelopedDataParser parser = new CMSEnvelopedDataParser(envelopedStream);

        // TODO validate the receiving enveloped-data against supported
        // algorithms

        // look for our recipient identifier
        RecipientId recId = new org.bouncycastle.cms.KeyTransRecipientId( 
                new X500Name(cert.getIssuerX500Principal().getName()),
                cert.getSerialNumber());

        RecipientInformationStore recipients = parser.getRecipientInfos();
        RecipientInformation recipient = recipients.get(recId);

        if (recipient != null) {
            // decrypt the data
            InputStream unenveloped = recipient.getContentStream(
                    new JceKeyTransEnvelopedRecipient(key).setProvider(BC_PROVIDER))
                    .getContentStream();
            IoUtil.copyStream(unenveloped, outStream);
        }

    }

    /**
     * Retrieve the signed content from a SignedData object. Signature MUST BE
     * VERIFIED apart since it's the original data without the signature
     * information.
     * 
     * @param encoded
     *            the SignedData object
     * @return the original data from signed content
     * @throws CMSException
     */
    public static byte[] parseSignedData(byte[] encoded) throws CMSException {

        installBouncyCastleProviderIfNecessary();

        CMSSignedData signed = new CMSSignedData(encoded);

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            signed.getSignedContent().write(bout);
        } catch (IOException e) {
            // ignore in a hope it won't happen with ByteArrayOutputStream
            LOGGER.error("parseSignedData() - Failed to retrieve SignedData content.", e);
            return null;
        }
        byte[] content = bout.toByteArray();

        return content;
    }

    public static byte[] parseSignedData(byte[] encoded, X509Certificate checkCert, SignatureVerifyResult checkResult) throws CMSException, IOException, OperatorCreationException {

    	installBouncyCastleProviderIfNecessary();

    	ByteArrayInputStream sigData = new ByteArrayInputStream(encoded);
    	InputStream contentStream = openSignedDataParser(sigData, checkCert, checkResult);

    	ByteArrayOutputStream content = new ByteArrayOutputStream();
    	IoUtil.copyStream(contentStream, content);

    	return content.toByteArray();
    }

    public static void parseSignedDataContentStream(InputStream signedStream, OutputStream outStream,
            X509Certificate cert) throws CMSException, IOException, OperatorCreationException {

        installBouncyCastleProviderIfNecessary();

        // use the CMS parser to unwrap signature from the SignedData
        CMSSignedDataParser parser = new CMSSignedDataParser(
                new JcaDigestCalculatorProviderBuilder().setProvider("BC").build(),
                signedStream);

        // TODO do verify the signature

        InputStream contentStream = parser.getSignedContent().getContentStream();
        IoUtil.copyStream(contentStream, outStream);

    }

    public static void createCompressedData(String dataPath, String outputPath) throws IOException {

        // create input and output file objects
        File input = new File(dataPath);
        File output = new File(outputPath);

        // generate compressed data stream
        createCompressedData(input, output);
    }

    public static void createCompressedData(File data, File output) throws IOException {

        // open compressed data stream
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(output);
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException(
                    "Failed to create CompressedData. Cannot open output compressed data file: "
                            + output.getAbsolutePath() + ". " + e.getMessage());
        }

        // generate compressed data stream
        FileInputStream dataStream = null;
        try {
            dataStream = new FileInputStream(data);
            createCompressedData(dataStream, outStream);
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException(
                    "Failed to create CompressedData. Cannot to open input file to generated compressed data: "
                            + data.getAbsolutePath() + ". " + e.getMessage());
        } finally {
            try {
                dataStream.close();
            } catch (Throwable t) {
                // ignore
            }
            try {
                outStream.close();
            } catch (Throwable t) {
                // ignore
            }
        }

    }

    public static void createCompressedData(InputStream dataStream, OutputStream outStream) throws IOException {

        OutputStream compressed = openCompressedDataStreamGenerator(outStream);
        IoUtil.copyStream(dataStream, compressed);

    }

    public static OutputStream openCompressedDataStreamGenerator(OutputStream outStream) throws IOException {

        CMSCompressedDataStreamGenerator gen = new CMSCompressedDataStreamGenerator();

        OutputStream compressed = gen.open(outStream, new ZlibCompressor());
        return compressed;

    }

    public static void createFileFromCompressedData(String compressedDataPath, String outputPath) throws CMSException,
            IOException {

        // create input and output file objects
        File input = new File(compressedDataPath);
        File output = new File(outputPath);

        // generate compressed data stream
        createFileFromCompressedData(input, output);
    }

    public static void createFileFromCompressedData(File compressedData, File output) throws CMSException, IOException {

        // open compressed data input stream
        FileInputStream in = null;
        try {
            in = new FileInputStream(compressedData);
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException(
                    "Failed to create file from CompressedData. Cannot open output compressed data file: "
                            + compressedData.getAbsolutePath() + ". " + e.getMessage());
        }

        // create data file from CompressedData
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(output);
            parseCompressedDataContentStream(in, outStream);
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("Failed to create file from CompressedData. Cannot open output file: "
                    + output.getAbsolutePath() + ". " + e.getMessage());
        } finally {
            try {
                in.close();
            } catch (Throwable t) {
                // ignore
            }
            try {
                outStream.close();
            } catch (Throwable t) {
                // ignore
            }
        }

    }

    public static void createFileFromEnvelopedData(String envelopedDataPath, String outputPath, X509Certificate cert,
            PrivateKey key) throws NoSuchProviderException, CMSException, IOException {

        // create input and output file objects
        File input = new File(envelopedDataPath);
        File output = new File(outputPath);

        // generate compressed data stream
        createFileFromEnvelopedData(input, output, cert, key);

    }

    public static void createFileFromEnvelopedData(File envelopedData, File output, X509Certificate cert, PrivateKey key)
            throws NoSuchProviderException, CMSException, IOException {

        // open compressed data input stream
        FileInputStream in = null;
        try {
            in = new FileInputStream(envelopedData);
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException(
                    "Failed to create file from EnvelopedData. Cannot open output enveloped data file: "
                            + envelopedData.getAbsolutePath() + ". " + e.getMessage());
        }

        // create data file from CompressedData
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(output);
            parseEnvelopedDataContentStream(in, outStream, cert, key);
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("Failed to create file from EnvelopedData. Cannot open output file: "
                    + output.getAbsolutePath() + ". " + e.getMessage());
        } finally {
            try {
                in.close();
            } catch (Throwable t) {
                // ignore
            }
            try {
                outStream.close();
            } catch (Throwable t) {
                // ignore
            }
        }

    }

    public static void parseCompressedDataContentStream(InputStream compressedData, OutputStream outStream)
            throws CMSException, IOException {

        // use the CMS parser to uncompress the CompressedData
        CMSCompressedDataParser cp = new CMSCompressedDataParser(compressedData);
        InputStream uncompressed = cp.getContent(new ZlibExpanderProvider()).getContentStream();

        IoUtil.copyStream(uncompressed, outStream);

    }

    public static void createFileFromSignedData(File signedData, File output, X509Certificate cert)
            throws CMSException, IOException, OperatorCreationException {

        // open compressed data input stream
        FileInputStream in = null;
        try {
            in = new FileInputStream(signedData);
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException(
                    "Failed to create file from SignedData. Cannot open output signed data file: "
                            + signedData.getAbsolutePath() + ". " + e.getMessage());
        }

        // create data file from CompressedData
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(output);
            parseSignedDataContentStream(in, outStream, cert);
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("Failed to create file from SignedData. Cannot open output file: "
                    + output.getAbsolutePath() + ". " + e.getMessage());
        } finally {
            try {
                in.close();
            } catch (Throwable t) {
                // ignore
            }
            try {
                outStream.close();
            } catch (Throwable t) {
                // ignore
            }
        }

    }

    public static void addNotifSignature(DefaultSignedDeliveryNotification notif, CipherSuite cipherSuite,
            X509Certificate userCert, PrivateKey userPrivateKey) throws NoSuchAlgorithmException, NoSuchProviderException, IOException, CMSException, CertificateEncodingException, OperatorCreationException {

        if (notif == null) throw new NullPointerException("notif");
        if (notif.getDatasetName() == null) throw new IllegalArgumentException("Delivery Notification object's DatasetName is null.");
        if (notif.getDateTime() == null) throw new IllegalArgumentException("Delivery Notification object's DateTime is null.");
        if (notif.getDestination() == null) throw new IllegalArgumentException("Delivery Notification object's Destination is null.");
        if (notif.getOriginator() == null) throw new IllegalArgumentException("Delivery Notification object's Originator is null.");
        if (notif.getCreator() == null) throw new IllegalArgumentException("Delivery Notification object's Creator is null.");
        if (notif.getVirtualFileHash() == null) throw new IllegalArgumentException("Delivery Notification object's Virtual File Hash is null.");
        if (cipherSuite == null) throw new NullPointerException("cipherSuite");
        if (userCert == null) throw new NullPointerException("userCert");
        if (userPrivateKey == null) throw new NullPointerException("userPrivateKey");

        // prepare the signing data
        byte[] data = getNotifSigningData(notif);

        // perform signing and set into the acknowledge object
        byte[] signature = createSignedData(data, cipherSuite, userCert, userPrivateKey);
        notif.setNotificationSignature(signature);

    }

    /**
     * Prepare the data buffer for signing from the acknowledge object.
     * 
     * @param info
     * @return
     * @throws UnsupportedEncodingException
     */
    public static byte[] getNotifSigningData(SignedDeliveryNotification info)
            throws UnsupportedEncodingException {

        // use EERP_V20 fields as properties are the same for NERP formatting

    	short ticker = info.getTicker();
		String timeWithCounter = formatTime(info.getDateTime()) + padd(Short.toString(ticker), 4, true, '0');

        StringBuffer sb = new StringBuffer();
        sb.append(formatAttribute(EERP_V20.getField("EERPDSN"), info.getDatasetName()));
        sb.append(formatDate(info.getDateTime()));
        sb.append(timeWithCounter);
        sb.append(formatAttribute(EERP_V20.getField("EERPDEST"), info.getDestination()));
        sb.append(formatAttribute(EERP_V20.getField("EERPORIG"), info.getOriginator()));

        byte[] text = sb.toString().getBytes(DEFAULT_PROTOCOL_CHARSET);
        sb = null;

        byte[] hash = info.getVirtualFileHash();

        if (text == null || hash == null)
            return null;

        byte[] data = new byte[text.length + hash.length];
        System.arraycopy(text, 0, data, 0, text.length);
        System.arraycopy(hash, 0, data, text.length, hash.length);

        return data;
    }

    public static String asEncryptionAlgorithm(CipherSuite cipherSuite) {
        if (cipherSuite == CipherSuite.TRIPLEDES_RSA_SHA1)
            return CMSEnvelopedGenerator.DES_EDE3_CBC;
        else if (cipherSuite == CipherSuite.AES_RSA_SHA1)
            return CMSEnvelopedGenerator.AES256_CBC;
        else
            return null;
    }

    public static String asDigestAlgorithm(CipherSuite cs) {
        if (cs == CipherSuite.TRIPLEDES_RSA_SHA1 || cs == CipherSuite.AES_RSA_SHA1) {
            return CMSSignedGenerator.DIGEST_SHA1;
        } else {
            return null;
        }
    }
    
    public static ASN1ObjectIdentifier asCMSAlgorithm(CipherSuite cipherSuite) {
        if (cipherSuite == CipherSuite.TRIPLEDES_RSA_SHA1)
            return CMSAlgorithm.DES_EDE3_CBC;
        else if (cipherSuite == CipherSuite.AES_RSA_SHA1)
            return CMSAlgorithm.AES256_CBC;
        else
            return null;
    }
    
    private static String asSignatureAlgorithm(CipherSuite cipherSuite) {
        if (cipherSuite == CipherSuite.TRIPLEDES_RSA_SHA1 || cipherSuite == CipherSuite.AES_RSA_SHA1) {
            return "SHA1withRSA";
        } else {
            return null;
        }
    }


}
