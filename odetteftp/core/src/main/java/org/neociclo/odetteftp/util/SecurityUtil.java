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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * @author Rafael Marins
 */
public class SecurityUtil {

    /**
     * BouncyCastle JCE Provider name.
     */
    public static final String BC_PROVIDER = BouncyCastleProvider.PROVIDER_NAME;

    /**
     * Use SHA-1 as hash generation algorithm.
     */
    public static final String DEFAULT_OFTP_HASH_ALGORITHM = "SHA-1";

    private static final int COMPUTING_HASH_BUFFER_SIZE = 4096;

    private static final String JAVA_KEYSTORE_TYPE = "JKS";

    private static final String PKCS12_KEYSTORE_TYPE = "PKCS12";

    private static final String BC_KEYSTORE_TYPE = "BKS";

    private static final String UBER_KEYSTORE_TYPE = "UBER";

    private static final String X509_CERTIFICATE_TYPE = "X.509";

    private static final String[] SUPPORTED_KEYSTORE_TYPES = { JAVA_KEYSTORE_TYPE, PKCS12_KEYSTORE_TYPE,
            BC_KEYSTORE_TYPE, UBER_KEYSTORE_TYPE };

    private static final Map<String, String> EXT_KEYSTORE_TABLE = new HashMap<String, String>();

    private static final Map<String, String> KSTYPE_PROVIDER_TABLE = new HashMap<String, String>();

    static {
        EXT_KEYSTORE_TABLE.put("ks", JAVA_KEYSTORE_TYPE);
        EXT_KEYSTORE_TABLE.put("jks", JAVA_KEYSTORE_TYPE);
        EXT_KEYSTORE_TABLE.put("jceks", JAVA_KEYSTORE_TYPE);
        EXT_KEYSTORE_TABLE.put("p12", PKCS12_KEYSTORE_TYPE);
        EXT_KEYSTORE_TABLE.put("pfx", PKCS12_KEYSTORE_TYPE);
        EXT_KEYSTORE_TABLE.put("bks", BC_KEYSTORE_TYPE);
        EXT_KEYSTORE_TABLE.put("ubr", UBER_KEYSTORE_TYPE);
        KSTYPE_PROVIDER_TABLE.put(BC_KEYSTORE_TYPE, BC_PROVIDER);
        KSTYPE_PROVIDER_TABLE.put(UBER_KEYSTORE_TYPE, BC_PROVIDER);
        KSTYPE_PROVIDER_TABLE.put(PKCS12_KEYSTORE_TYPE, BC_PROVIDER);
    }

    public static KeyStore openKeyStore(File path, char[] password) throws KeyStoreException, NoSuchProviderException,
            NoSuchAlgorithmException, CertificateException, IOException {

        installBouncyCastleProviderIfNecessary();

        String ext = IoUtil.getFilenameExtension(path.getName()).toLowerCase();

        final String type = EXT_KEYSTORE_TABLE.get(ext);

        // first try the extension keystore type mapping
        List<String> typeOrder = Arrays.asList(SUPPORTED_KEYSTORE_TYPES);
        Collections.sort(typeOrder, new Comparator<String>() {
            public int compare(String o1, String o2) {
                if (o1.equals(type))
                    return -1;
                else if (o2.equals(type))
                    return 1;
                else
                    return 0;
            }
        });

        KeyStore ks = null;
        IOException keystoreNotLoaded = null;
        for (String t : typeOrder) {
            String prov = KSTYPE_PROVIDER_TABLE.get(t);

            if (prov == null)
                ks = KeyStore.getInstance(t);
            else
                ks = KeyStore.getInstance(t, prov);

            FileInputStream inStream = null;

            try {
                inStream = new FileInputStream(path);
                ks.load(inStream, password);
            } catch (IOException e) {
                if (keystoreNotLoaded == null)
                    keystoreNotLoaded = e;
                continue;
            } finally {
                try {
                    if (inStream != null)
                        inStream.close();
                } catch (Throwable twb) {
                    // ignore
                }
            }

            keystoreNotLoaded = null;
            break;
        }

        if (keystoreNotLoaded != null)
            throw keystoreNotLoaded;

        return ks;
    }

    public static PrivateKey getPrivateKey(KeyStore ks, char[] password) throws KeyStoreException,
            NoSuchAlgorithmException, UnrecoverableKeyException {

        installBouncyCastleProviderIfNecessary();

        PrivateKey key = null;

        Enumeration<String> aliases = ks.aliases();
        while (aliases.hasMoreElements()) {
            String entry = aliases.nextElement();
            if (ks.isKeyEntry(entry)) {
                key = (PrivateKey) ks.getKey(entry, password);
                break;
            }
        }

        return key;
    }

    public static X509Certificate openCertificate(File path) throws FileNotFoundException, CertificateException,
            NoSuchProviderException {

        installBouncyCastleProviderIfNecessary();

        // load the certificate from file
        X509Certificate cert = null;
        FileInputStream inStream = new FileInputStream(path);

        try {
            CertificateFactory cfact = CertificateFactory.getInstance(X509_CERTIFICATE_TYPE, BC_PROVIDER);
            cert = (X509Certificate) cfact.generateCertificate(inStream);
        } finally {
            try {
                inStream.close();
            } catch (Throwable t) {
                // ignore
            }
        }

        return cert;
    }

    public static void installBouncyCastleProviderIfNecessary() {
        BouncyCastleProvider prov = (BouncyCastleProvider) Security.getProvider(BC_PROVIDER);
        if (prov == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    public static X509Certificate getCertificateEntry(KeyStore ks) throws KeyStoreException {

        installBouncyCastleProviderIfNecessary();

        X509Certificate cert = null;

        String certAlias = ks.aliases().nextElement();
        cert = (X509Certificate) ks.getCertificate(certAlias);

//        Enumeration<String> aliases = ks.aliases();
//        while (aliases.hasMoreElements()) {
//            String entry = aliases.nextElement();
//            if (ks.isCertificateEntry(entry)) {
//                cert = (X509Certificate) ks.getCertificate(entry);
//                break;
//            }
//        }

        return cert;
    }

    public static byte[] computeFileHash(File file, String algorithm) throws NoSuchAlgorithmException,
            NoSuchProviderException, IOException {

        installBouncyCastleProviderIfNecessary();

        MessageDigest hash = MessageDigest.getInstance(algorithm, BC_PROVIDER);

        FileInputStream fis = new FileInputStream(file);
        FileChannel fileChannel = fis.getChannel();
        ByteBuffer buf = ByteBuffer.allocateDirect(COMPUTING_HASH_BUFFER_SIZE);

        while (fileChannel.read(buf) != -1) {
            buf.flip();
            hash.update(buf);
            buf.clear();
        }

        try {
            fis.close();
            fileChannel.close();
        } catch (IOException e) {
            // ignore
        }

        return hash.digest();
    }

}
