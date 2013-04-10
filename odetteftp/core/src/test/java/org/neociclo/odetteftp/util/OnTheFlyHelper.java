/**
 * Neociclo Accord, Open Source B2B Integration Suite
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

import static org.neociclo.odetteftp.util.SecurityUtil.*;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.security.auth.x500.X500Principal;
import javax.security.auth.x500.X500PrivateCredential;

import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.x509.X509V1CertificateGenerator;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.AuthorityKeyIdentifierStructure;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class OnTheFlyHelper {

	private OnTheFlyHelper() {
	}

	public static char[] KEY_PASSWD = "keyPassword".toCharArray();

	public static String ROOT_ALIAS = "root";
	public static String INTERMEDIATE_ALIAS = "intermediate";
	public static String END_ENTITY_ALIAS = "end";

	private static final int VALIDITY_PERIOD = 7 * 24 * 60 * 60 * 1000; // one
																		// week

	/**
	 * Create a KeyStore containing the a private credential with certificate
	 * chain and a trust anchor.
	 */
	public static KeyStore createCredentials() throws Exception {

		KeyStore store = KeyStore.getInstance("JKS");

		store.load(null, null);

		X500PrivateCredential rootCredential = createRootCredential();
		X500PrivateCredential interCredential = createIntermediateCredential(rootCredential.getPrivateKey(),
				rootCredential.getCertificate());
		X500PrivateCredential endCredential = createEndEntityCredential(interCredential.getPrivateKey(),
				interCredential.getCertificate());

		store.setCertificateEntry(rootCredential.getAlias(), rootCredential.getCertificate());
		store.setKeyEntry(endCredential.getAlias(), endCredential.getPrivateKey(), KEY_PASSWD, new Certificate[] {
				endCredential.getCertificate(), interCredential.getCertificate(), rootCredential.getCertificate() });

		return store;
	}

	public static X500PrivateCredential createRootCredential() throws Exception {
		KeyPair rootPair = generateRSAKeyPair();
		X509Certificate rootCert = generateRootCert(rootPair);

		return new X500PrivateCredential(rootCert, rootPair.getPrivate(), ROOT_ALIAS);
	}

	public static X500PrivateCredential createIntermediateCredential(PrivateKey caKey, X509Certificate caCert)
			throws Exception {
		KeyPair interPair = generateRSAKeyPair();
		X509Certificate interCert = generateIntermediateCert(interPair.getPublic(), caKey, caCert);

		return new X500PrivateCredential(interCert, interPair.getPrivate(), INTERMEDIATE_ALIAS);
	}

	public static X500PrivateCredential createEndEntityCredential(PrivateKey caKey, X509Certificate caCert)
			throws Exception {
		KeyPair endPair = generateRSAKeyPair();
		X509Certificate endCert = generateEndEntityCert(endPair.getPublic(), caKey, caCert);

		return new X500PrivateCredential(endCert, endPair.getPrivate(), END_ENTITY_ALIAS);
	}

	public static X509Certificate generateRootCert(KeyPair pair) throws Exception {

		installBouncyCastleProviderIfNecessary();

		X509V1CertificateGenerator certGen = new X509V1CertificateGenerator();

		certGen.setSerialNumber(BigInteger.valueOf(1));
		certGen.setIssuerDN(new X500Principal("CN=Test CA Certificate"));
		certGen.setNotBefore(new Date(System.currentTimeMillis()));
		certGen.setNotAfter(new Date(System.currentTimeMillis() + VALIDITY_PERIOD));
		certGen.setSubjectDN(new X500Principal("CN=Test CA Certificate"));
		certGen.setPublicKey(pair.getPublic());
		certGen.setSignatureAlgorithm("SHA1WithRSAEncryption");

		return certGen.generate(pair.getPrivate(), BC_PROVIDER);
	}

	public static X509Certificate generateIntermediateCert(PublicKey intKey, PrivateKey caKey, X509Certificate caCert)
			throws Exception {

		installBouncyCastleProviderIfNecessary();

		X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();

		certGen.setSerialNumber(BigInteger.valueOf(1));
		certGen.setIssuerDN(caCert.getSubjectX500Principal());
		certGen.setNotBefore(new Date(System.currentTimeMillis()));
		certGen.setNotAfter(new Date(System.currentTimeMillis() + VALIDITY_PERIOD));
		certGen.setSubjectDN(new X500Principal("CN=Test Intermediate Certificate"));
		certGen.setPublicKey(intKey);
		certGen.setSignatureAlgorithm("SHA1WithRSAEncryption");

		certGen.addExtension(X509Extensions.AuthorityKeyIdentifier, false, new AuthorityKeyIdentifierStructure(caCert));
		certGen.addExtension(X509Extensions.SubjectKeyIdentifier, false, new SubjectKeyIdentifierStructure(intKey));
		certGen.addExtension(X509Extensions.BasicConstraints, true, new BasicConstraints(0));
		certGen.addExtension(X509Extensions.KeyUsage, true, new KeyUsage(KeyUsage.digitalSignature
				| KeyUsage.keyCertSign | KeyUsage.cRLSign));

		return certGen.generate(caKey, BC_PROVIDER);
	}

	public static X509Certificate generateEndEntityCert(PublicKey entityKey, PrivateKey caKey, X509Certificate caCert)
			throws Exception {

		installBouncyCastleProviderIfNecessary();

		X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();

		certGen.setSerialNumber(BigInteger.valueOf(1));
		certGen.setIssuerDN(caCert.getSubjectX500Principal());
		certGen.setNotBefore(new Date(System.currentTimeMillis()));
		certGen.setNotAfter(new Date(System.currentTimeMillis() + VALIDITY_PERIOD));
		certGen.setSubjectDN(new X500Principal("CN=Test End Certificate"));
		certGen.setPublicKey(entityKey);
		certGen.setSignatureAlgorithm("SHA1WithRSAEncryption");

		certGen.addExtension(X509Extensions.AuthorityKeyIdentifier, false, new AuthorityKeyIdentifierStructure(caCert));
		certGen.addExtension(X509Extensions.SubjectKeyIdentifier, false, new SubjectKeyIdentifierStructure(entityKey));
		certGen.addExtension(X509Extensions.BasicConstraints, true, new BasicConstraints(false));
		certGen.addExtension(X509Extensions.KeyUsage, true, new KeyUsage(KeyUsage.digitalSignature
				| KeyUsage.keyEncipherment));

		return certGen.generate(caKey, BC_PROVIDER);
	}

	public static KeyPair generateRSAKeyPair() throws Exception {

		installBouncyCastleProviderIfNecessary();

		KeyPairGenerator kpGen = KeyPairGenerator.getInstance("RSA", BC_PROVIDER);

		kpGen.initialize(1024, new SecureRandom());

		return kpGen.generateKeyPair();
	}

}
