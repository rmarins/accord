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

import static org.junit.Assert.*;
import static org.neociclo.odetteftp.util.EnvelopingUtil.*;
import static org.neociclo.odetteftp.util.OftpTestUtil.*;
import static org.neociclo.odetteftp.util.SecurityUtil.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.neociclo.odetteftp.protocol.v20.CipherSuite;

/**
 * @author Rafael Marins
 */
public class EnvelopingUtilTest {

    private static final String TEST_FILE_PATH = "data/TEXTFILE";

    private static final String MY_CERT_PATH = "certificates/o0055partnera-public.cer";

    private static final String MY_KS_PATH = "keystores/o0055partnera.p12";

    private static final String PARTNER_CERT_PATH = "certificates/o0055partnerb-public.cer";

    private static final String PARTNER_KS_PATH = "keystores/o0055partnerb.p12";

    private static final String TEST_KEYSTORE_PASSWORD = "neociclo";

    private static final CipherSuite CIPHER_SELECTION = CipherSuite.TRIPLEDES_RSA_SHA1;

    public static void compress(File input, File output) throws Exception {
        createCompressedData(input, output);
    }

    private static void uncompress(File input, File output) throws Exception {
        createFileFromCompressedData(input, output);
    }

    private static void envelope(File input, File output) throws Exception {

        // load public certificate
        File partnerCertFile = getResourceFile(PARTNER_CERT_PATH);
        X509Certificate partnerCert = (X509Certificate) openCertificate(partnerCertFile);

        createEnvelopedData(input, output, CIPHER_SELECTION, partnerCert);

    }

    private static void unenvelope(File input, File output) throws Exception {

        char[] pwd = TEST_KEYSTORE_PASSWORD.toCharArray();
        File recipientKsFile = getResourceFile(PARTNER_KS_PATH);
        KeyStore recKs = openKeyStore(recipientKsFile, pwd);
        X509Certificate recCert = getCertificateEntry(recKs);
        PrivateKey recKey = getPrivateKey(recKs, pwd);

        createFileFromEnvelopedData(input, output, recCert, recKey);

    }
    
    private static void unenvelope2(File input, File output) throws Exception {
        char[] pwd = TEST_KEYSTORE_PASSWORD.toCharArray();
        File recipientKsFile = getResourceFile(PARTNER_KS_PATH);
        KeyStore recKs = openKeyStore(recipientKsFile, pwd);
        X509Certificate recCert = getCertificateEntry(recKs);
        PrivateKey recKey = getPrivateKey(recKs, pwd);
        InputStream is = openEnvelopedDataParser(FileUtils.openInputStream(input), recCert, recKey);
        FileUtils.copyInputStreamToFile(is, output);
    }

    private static void addSignature(File input, File output) throws Exception {

        // load the keystore and the private-key
        char[] pwd = TEST_KEYSTORE_PASSWORD.toCharArray();
        File myKsFile = getResourceFile(MY_KS_PATH);
        KeyStore myKs = openKeyStore(myKsFile, pwd);
        PrivateKey myKey = getPrivateKey(myKs, pwd);
        X509Certificate myCert = getCertificateEntry(myKs);

        createSignedData(input, output, CIPHER_SELECTION, myCert, myKey);

    }

    private static void removeSignature(File input, File output) throws Exception {
        // load public certificate
        File myCertFile = getResourceFile(MY_CERT_PATH);
        X509Certificate myCert = (X509Certificate) openCertificate(myCertFile);

        createFileFromSignedData(input, output, myCert);
    }

    @Test
    public void testAuthenticationChallengeEnveloping() throws Exception {

        char[] pwd = TEST_KEYSTORE_PASSWORD.toCharArray();
        byte[] challenge = OftpUtil.generateRandomChallenge(OdetteFtpConstants.AUTHENTICATION_CHALLENGE_SIZE);

        // load public certificate
        File pubCert = getResourceFile(PARTNER_CERT_PATH);
        X509Certificate cert = (X509Certificate) openCertificate(pubCert);

        // generate the CMS EnvelopedData (encrypted) using the public
        // certificate
        byte[] encryptedData = createEnvelopedData(challenge, CIPHER_SELECTION, cert);

        // load the keystore and the private-key
        File ksFile = getResourceFile(PARTNER_KS_PATH);
        KeyStore ks = openKeyStore(ksFile, pwd);
        PrivateKey privateKey = getPrivateKey(ks, pwd);
        X509Certificate myCert = getCertificateEntry(ks);

        // unveil the encrypted value from parsing the CMS EnvelopedData using
        // the private-key
        byte[] decrypted = parseEnvelopedData(encryptedData, myCert, privateKey);
        assertTrue("Challenges are not equals.", Arrays.equals(challenge, decrypted));

    }

    @Test
    public void testFullCmsStreamGenerator() throws Exception {

        File data = getResourceFile(TEST_FILE_PATH);
        File output = new File(getTestDataDir(), data.getName() + ".fullCms");

        // load public certificate
        File partnerCertFile = getResourceFile(PARTNER_CERT_PATH);
        X509Certificate partnerCert = (X509Certificate) openCertificate(partnerCertFile);

        // load the keystore and the private-key
        char[] pwd = TEST_KEYSTORE_PASSWORD.toCharArray();
        File myKsFile = getResourceFile(MY_KS_PATH);
        KeyStore myKs = openKeyStore(myKsFile, pwd);
        PrivateKey myKey = getPrivateKey(myKs, pwd);
        X509Certificate myCert = getCertificateEntry(myKs);

        FileOutputStream outStream = new FileOutputStream(output);

        //
        // Enchain full cms Stream Generators
        //
        
        OutputStream envelopingStream = openEnvelopedDataStreamGenerator(outStream, CIPHER_SELECTION, partnerCert);
        OutputStream compressingStream = openCompressedDataStreamGenerator(envelopingStream);
        OutputStream signingStream = openSignedDataStreamGenerator(compressingStream, CIPHER_SELECTION, myCert, myKey);

        // copy from input file to the stream chain
        FileInputStream dataStream = new FileInputStream(data);
        IoUtil.copyStream(dataStream, signingStream);

        // XXX very important to preserve this order when closing

        signingStream.flush();
        signingStream.close();

        compressingStream.flush();
        compressingStream.close();

        envelopingStream.flush();
        envelopingStream.close();

        outStream.flush();
        outStream.close();

        assertTrue("File doesn't exists: " + output.getAbsolutePath(), output.exists());

        File unenveloped = new File(getTestDataDir(), data.getName() + ".7th");
        File uncompressed = new File(getTestDataDir(), data.getName() + ".8th");
        File unsigned = new File(getTestDataDir(), data.getName() + ".9th");

        unenvelope(output, unenveloped);
        uncompress(unenveloped, uncompressed);
        removeSignature(uncompressed, unsigned);

        byte[] hashOriginal = SecurityUtil.computeFileHash(data, "SHA-1");
        byte[] hashProcessed = SecurityUtil.computeFileHash(unsigned, "SHA-1");

        output.delete();
        unenveloped.delete();
        uncompressed.delete();
        unsigned.delete();

		assertTrue("Original file and the processed (full cms signed/compressed/encrypted and reverse) are not equal.",
				Arrays.equals(hashOriginal, hashProcessed));         

    }

    @Test
    public void testThreeStepsCmsEnveloping() throws Exception {

        // -----------------------------------------------------
        // Compress, Encrypt and Sign
        // -----------------------------------------------------

        File inputData = getResourceFile(TEST_FILE_PATH);

        File compressed = new File(getTestDataDir(), inputData.getName() + ".1st");
        File enveloped = new File(getTestDataDir(), inputData.getName() + ".2nd");
        File signed = new File(getTestDataDir(), inputData.getName() + ".3rd");

        compress(inputData, compressed);
        envelope(compressed, enveloped);
        addSignature(enveloped, signed);

        // -----------------------------------------------------
        // Remove Signature, Decrypt, Uncompress
        // -----------------------------------------------------

        File unsigned = new File(getTestDataDir(), inputData.getName() + ".4th");
        File unenveloped = new File(getTestDataDir(), inputData.getName() + ".5th");
        File uncompressed = new File(getTestDataDir(), inputData.getName() + ".6th");

        removeSignature(signed, unsigned);
        unenvelope(unsigned, unenveloped);
        uncompress(unenveloped, uncompressed);

        compressed.delete();
        enveloped.delete();
        signed.delete();
        unenveloped.delete();
        uncompressed.delete();
        unsigned.delete();

        // -----------------------------------------------------
        // ASSERTION
        // -----------------------------------------------------

    }

    @Test
    public void testStreamVerifyAndRemoveSignature() throws Exception {

        //
        // Add Signature
        //

        File payloadFile = getResourceFile(TEST_FILE_PATH);

        String prefix = payloadFile.getName() + "-";

        File signedFile = File.createTempFile(prefix, ".signed", getTestDataDir());

        // generate a test keystore on the fly
        KeyStore ks = OnTheFlyHelper.createCredentials();
        PrivateKey pk = getPrivateKey(ks, OnTheFlyHelper.KEY_PASSWD);
        X509Certificate cert = getCertificateEntry(ks);

        // create the signed output file
        createSignedData(payloadFile, signedFile, CIPHER_SELECTION, cert, pk);

        //
        // Open Signature Stream Parser
        //

        SignatureVerifyResult signatureCheck = new SignatureVerifyResult();

        FileInputStream signedData = new FileInputStream(signedFile);
        InputStream unsignedData = openSignedDataParser(signedData, cert, signatureCheck);

        // create an ouput file and copy the unsigned data stream
        File outputFile = File.createTempFile(prefix, ".unsigned", getTestDataDir());
        FileOutputStream outputData = new FileOutputStream(outputFile);

        IoUtil.copyStream(unsignedData, outputData);

        unsignedData.close();
        signedData.close();
        outputData.close();

        //
        // Compare original Payload and resulting Output File
        //

        byte[] hash1 = SecurityUtil.computeFileHash(payloadFile, "MD5");
        byte[] hash2 = SecurityUtil.computeFileHash(outputFile, "MD5");

        signedFile.delete();	// remove files after all
        outputFile.delete();

        assertTrue("Invalid signature removed file: " + outputFile, Arrays.equals(hash1, hash2));

        //
        // Make sure the signature were correctly verified
        //

        assertTrue("Signature check failed: " + signatureCheck.getExceptionCaught(), signatureCheck.hasSucceed());

    }

    @Test
    public void testCompressedData() throws Exception {
        File data = getResourceFile(TEST_FILE_PATH);
        File output = new File(getTestDataDir(), data.getName() + ".compressedData");
        createCompressedData(data, output);

        File data2nd = new File(getTestDataDir(), data.getName() + ".2nd");
        createFileFromCompressedData(output, data2nd);

        byte[] hash1 = SecurityUtil.computeFileHash(data, "MD5");
        byte[] hash2 = SecurityUtil.computeFileHash(data2nd, "MD5");

        output.delete();
        data2nd.delete();

        assertTrue("Original file and decompressed data file content are not equal.", Arrays.equals(hash1, hash2));
    }
    
    
    
    @Test
    public void testOpenEnvelopedDataParser() throws Exception {
        File inputData = getResourceFile(TEST_FILE_PATH);
        File enveloped = new File(getTestDataDir(), inputData.getName() + ".2nd");
        File unenveloped = new File(getTestDataDir(), inputData.getName() + ".5th");

        envelope(inputData, enveloped);
        unenvelope2(enveloped, unenveloped);
        
        byte[] hash1 = SecurityUtil.computeFileHash(inputData, "MD5");
        byte[] hash2 = SecurityUtil.computeFileHash(unenveloped, "MD5");
        assertTrue("Original file and unenveloped data file content are not equal.", Arrays.equals(hash1, hash2));
        
        enveloped.delete();
        unenveloped.delete();
    }
    
    @Test
    public void testCreateSignedData() throws Exception {
        File data = getResourceFile(TEST_FILE_PATH);
        byte[] dataBytes = FileUtils.readFileToByteArray(data);

        // generate a test keystore on the fly
        KeyStore ks = OnTheFlyHelper.createCredentials();
        PrivateKey key = getPrivateKey(ks, OnTheFlyHelper.KEY_PASSWD);
        X509Certificate cert = getCertificateEntry(ks);

        byte[] signedData = createSignedData(
                dataBytes, 
                CIPHER_SELECTION,
                cert,
                key);

        SignatureVerifyResult svr = new SignatureVerifyResult();
        byte[] outdata = parseSignedData(signedData, cert, svr);

        assertEquals("Original file and unenveloped data file content are not equal.", 
                DigestUtils.md5Hex(dataBytes),
                DigestUtils.md5Hex(outdata));
    }
    
    
}
