/**
 * Neociclo Accord, Open Source B2B Integration Suite
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
package org.neociclo.odetteftp.examples.server;

import static org.neociclo.capi20.parameter.B3Protocol.X25_DTE_DTE;
import static org.neociclo.capi20.parameter.CompatibilityInformationProfile.UNRESTRICTED_DIGITAL;
import static org.neociclo.odetteftp.examples.server.SimpleServerHelper.getUserConfigFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import net.sourceforge.jcapi.message.parameter.AdditionalInfo;
import net.sourceforge.jcapi.message.parameter.sub.B3Configuration;
import net.sourceforge.jcapi.message.parameter.sub.BChannelInformation;

import org.neociclo.isdn.CapiFactory;
import org.neociclo.isdn.RemoteCapiFactory;
import org.neociclo.isdn.netty.channel.IsdnChannel;
import org.neociclo.isdn.netty.channel.IsdnChannelConfig;
import org.neociclo.isdn.netty.channel.IsdnConfigurator;
import org.neociclo.odetteftp.OdetteFtpSession;
import org.neociclo.odetteftp.OdetteFtpVersion;
import org.neociclo.odetteftp.TransferMode;
import org.neociclo.odetteftp.protocol.v20.CipherSuite;
import org.neociclo.odetteftp.security.MappedCallbackHandler;
import org.neociclo.odetteftp.security.PasswordAuthenticationCallback;
import org.neociclo.odetteftp.security.PasswordCallback;
import org.neociclo.odetteftp.service.IsdnServer;
import org.neociclo.odetteftp.support.OdetteFtpConfiguration;
import org.neociclo.odetteftp.support.OftpletEventListenerAdapter;
import org.neociclo.odetteftp.support.PasswordHandler;
import org.neociclo.odetteftp.support.PropertiesBasedConfiguration;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class IsdnSimpleServer {

	private static final File SERVER_DIR = new File(".", "simpleserver-data");

	public static void main(String[] args) throws Exception {

		if (args.length != 3 && args.length != 5) {
			System.out.println("Illegal number of arguments were provided.");
			printUsageHelp();
			System.exit(1);
		}

		String bindingMsn = args[0];
		String rcapiHost = args[1];
		int rcapiPort = Integer.parseInt(args[2]);

		String rcapiUser = null;
		String rcapiPassword = null;
		if (args.length == 5) {
			rcapiUser = args[3];
			rcapiPassword = args[4];
		}

		OdetteFtpConfiguration config = createInitialServerConfig();

		MappedCallbackHandler serverSecurityHandler = new MappedCallbackHandler();

		//
		// add server password authentication handler based on the users
		// properties file
		//
		serverSecurityHandler.addHandler(PasswordAuthenticationCallback.class,
				new SimpleServerAuthenticationHandler(SERVER_DIR));

		//
		// add password callback which tells the library to reply with server
		// side identification and password
		//
		serverSecurityHandler.addHandler(PasswordCallback.class,
				new PasswordHandler("O0055MYSERVERID", "MYPASSWD"));

		SimpleServerOftpletFactory factory = new SimpleServerOftpletFactory(SERVER_DIR, config, serverSecurityHandler,
				new OftpletEventListenerAdapter() {

			@Override
			public void configure(OdetteFtpSession session) {
				// setup custom parameters specific to this user configuration
				String userCode = session.getUserCode();
				File configFile = getUserConfigFile(SERVER_DIR, userCode);
				PropertiesBasedConfiguration customConfig = new PropertiesBasedConfiguration();

				try {
					customConfig.load(new FileInputStream(configFile));
					customConfig.setup(session);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

        // set up the CAPI intermediate layer
        CapiFactory capi = new RemoteCapiFactory(rcapiHost, rcapiPort, rcapiUser, rcapiPassword);

		IsdnServer server = new IsdnServer(bindingMsn, capi, factory);
		server.setIsdnConfigurator(new IsdnConfigurator() {
			public void configureChannel(IsdnChannel channel) {
                IsdnChannelConfig config = channel.getConfig();

                config.setMaxLogicalConnection(1);
                config.setMaxBDataBlocks(7);
                config.setMaxBDataLen(4096);

                config.setCompatibilityInformationProfile(UNRESTRICTED_DIGITAL);
                config.setB3(X25_DTE_DTE);

                B3Configuration b3config = new B3Configuration(X25_DTE_DTE.getBitField());
                config.setB3Config(b3config);

                AdditionalInfo info = new AdditionalInfo();
                BChannelInformation bChannelInfo = new BChannelInformation();
                info.setBinfo(bChannelInfo);
                config.setAdditionalInfo(info);
			}
		});

		server.start();

		System.out.println("Press Ctrl+C to stop.");
	}

	private static void printUsageHelp() {
		System.out.println();
		System.out.println("Accord Odette FTP Examples");
		System.out.println("--------------------------");
		System.out.println("usage:");
		System.out.println("    IsdnSimpleServer <bindingMsn> <rcapiHost> <rcapiPort> [<rcapiUser> <rcapiPassword>]");
		System.out.println();
	}

	private static OdetteFtpConfiguration createInitialServerConfig() {
		OdetteFtpConfiguration c = new OdetteFtpConfiguration();

		c.setTransferMode(TransferMode.BOTH);
		c.setVersion(OdetteFtpVersion.OFTP_V14);
		c.setDataExchangeBufferSize(512);
		c.setWindowSize(4);

		c.setUseSecureAuthentication(false);
		c.setCipherSuiteSelection(CipherSuite.NO_CIPHER_SUITE_SELECTION);

		return c;
	}

	public static URL getResource(String name) {
        return Thread.currentThread().getContextClassLoader().getResource(name);
    }

    public static File getResourceFile(String name) throws URISyntaxException {
        return new File(getResource(name).toURI());
    }

}
