package org.neociclo.accord.odetteftp.camel;

import java.io.File;

import javax.net.ssl.SSLEngine;

public interface SSLEngineFactory {

    SSLEngineFactory setup(String keyStoreFormat, String securityProvider, File keyStoreFile, File trustStoreFile,
            char[] passphrase) throws Exception;

    SSLEngine createClientSSLEngine();

}