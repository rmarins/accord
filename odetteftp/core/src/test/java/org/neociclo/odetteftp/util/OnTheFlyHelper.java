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

import javax.security.auth.x500.X500PrivateCredential;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v1CertificateBuilder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

/**
 * @author Rafael Marins
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
		
		ContentSigner sigGen = new JcaContentSignerBuilder("SHA1WithRSAEncryption").setProvider(BC_PROVIDER).build(pair.getPrivate()); 
		SubjectPublicKeyInfo subPubKeyInfo = new SubjectPublicKeyInfo(new DefaultSignatureAlgorithmIdentifierFinder().find("SHA1withRSA"), pair.getPublic().getEncoded());
        X509v1CertificateBuilder v1CertGen = new X509v1CertificateBuilder(
		        new X500Name("CN=Test CA Certificate"),
		        BigInteger.ONE,
		        new Date(System.currentTimeMillis()),
		        new Date(System.currentTimeMillis() + VALIDITY_PERIOD),
		        new X500Name("CN=Test CA Certificate"),
		        subPubKeyInfo );
        X509CertificateHolder certHolder = v1CertGen.build(sigGen);
		X509Certificate cert = ( new JcaX509CertificateConverter() 
		        ).getCertificate( certHolder ); 
		
		return cert;
	}

	public static X509Certificate generateIntermediateCert(PublicKey intKey, PrivateKey caKey, X509Certificate caCert)
			throws Exception {

		installBouncyCastleProviderIfNecessary();
		
        ContentSigner sigGen = new JcaContentSignerBuilder("SHA1WithRSAEncryption").setProvider(BC_PROVIDER).build(caKey); 
        SubjectPublicKeyInfo subPubKeyInfo = new SubjectPublicKeyInfo(new DefaultSignatureAlgorithmIdentifierFinder().find("SHA1withRSA"), intKey.getEncoded());
        X509v3CertificateBuilder v3CertGen = new X509v3CertificateBuilder(
                new JcaX509CertificateHolder(caCert).getSubject(),
                BigInteger.ONE,
                new Date(System.currentTimeMillis()),
                new Date(System.currentTimeMillis() + VALIDITY_PERIOD),
                new X500Name("CN=Test Intermediate Certificate"),
                subPubKeyInfo );

        JcaX509ExtensionUtils extensionUtil = new JcaX509ExtensionUtils();
        v3CertGen.addExtension(Extension.authorityKeyIdentifier, false, extensionUtil.createAuthorityKeyIdentifier(caCert));
        v3CertGen.addExtension(Extension.subjectKeyIdentifier, false, extensionUtil.createSubjectKeyIdentifier(intKey));
		v3CertGen.addExtension(Extension.basicConstraints, true, new BasicConstraints(0));
		v3CertGen.addExtension(Extension.keyUsage, true, new KeyUsage(KeyUsage.digitalSignature
				| KeyUsage.keyCertSign | KeyUsage.cRLSign));

        X509CertificateHolder certHolder = v3CertGen.build(sigGen);
        X509Certificate cert = ( new JcaX509CertificateConverter() 
                ).getCertificate( certHolder ); 
        
        return cert;
	}

	public static X509Certificate generateEndEntityCert(PublicKey entityKey, PrivateKey caKey, X509Certificate caCert)
			throws Exception {

		installBouncyCastleProviderIfNecessary();
 
		ContentSigner sigGen = new JcaContentSignerBuilder("SHA1WITHRSA").setProvider(BC_PROVIDER).build(caKey); 
        SubjectPublicKeyInfo subPubKeyInfo = SubjectPublicKeyInfo.getInstance(entityKey.getEncoded());    
        X509v3CertificateBuilder v3CertGen = new X509v3CertificateBuilder(
                new JcaX509CertificateHolder(caCert).getSubject(),
                BigInteger.ONE,
                new Date(System.currentTimeMillis()),
                new Date(System.currentTimeMillis() + VALIDITY_PERIOD),
                new X500Name("CN=Test End Certificate"),
                subPubKeyInfo );

        JcaX509ExtensionUtils extensionUtil = new JcaX509ExtensionUtils();
        v3CertGen.addExtension(Extension.authorityKeyIdentifier, false, extensionUtil.createAuthorityKeyIdentifier(caCert));
        v3CertGen.addExtension(Extension.subjectKeyIdentifier, false, extensionUtil.createSubjectKeyIdentifier(entityKey));
        v3CertGen.addExtension(Extension.basicConstraints, true, new BasicConstraints(false));
        v3CertGen.addExtension(Extension.keyUsage, true, new KeyUsage(KeyUsage.digitalSignature
                | KeyUsage.keyEncipherment));

        X509CertificateHolder certHolder = v3CertGen.build(sigGen);
        X509Certificate cert = ( new JcaX509CertificateConverter() 
                ).getCertificate( certHolder ); 
        
		return cert;
	}

	public static KeyPair generateRSAKeyPair() throws Exception {

		installBouncyCastleProviderIfNecessary();

		KeyPairGenerator kpGen = KeyPairGenerator.getInstance("RSA", BC_PROVIDER);

		kpGen.initialize(1024, new SecureRandom());

		return kpGen.generateKeyPair();
	}

}
