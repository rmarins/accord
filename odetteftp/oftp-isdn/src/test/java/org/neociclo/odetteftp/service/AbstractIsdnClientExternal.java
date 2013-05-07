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
package org.neociclo.odetteftp.service;

import static org.neociclo.odetteftp.util.IsdnConstants.*;

import net.sourceforge.jcapi.message.parameter.AdditionalInfo;
import net.sourceforge.jcapi.message.parameter.sub.B3Configuration;
import net.sourceforge.jcapi.message.parameter.sub.BChannelInformation;

import org.neociclo.capi20.parameter.B3Protocol;
import org.neociclo.capi20.parameter.CompatibilityInformationProfile;
import org.neociclo.isdn.CapiFactory;
import org.neociclo.isdn.RemoteCapiFactory;
import org.neociclo.isdn.netty.channel.IsdnChannel;
import org.neociclo.isdn.netty.channel.IsdnChannelConfig;
import org.neociclo.isdn.netty.channel.IsdnConfigurator;
import org.neociclo.odetteftp.oftplet.OftpletFactory;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public abstract class AbstractIsdnClientExternal extends BaseClientExternalTestCase {

	@Override
	protected Client createClient(OftpletFactory factory) {

        String rcapiHost = System.getProperty("rcapi.server");
        int rcapiPort = Integer.valueOf(
        		System.getProperty("rcapi.port", String.valueOf(DEFAULT_RCAPI_PORT)));
        String rcapiUser = System.getProperty("rcapi.user");
        String rcapiPswd = System.getProperty("rcapi.password");

        if (rcapiHost == null || "".equals(rcapiHost.trim())) {
        	throw new IllegalArgumentException("rcapi.server");
        }

        // set up the CAPI intermediate layer
        CapiFactory capi = new RemoteCapiFactory(rcapiHost, rcapiPort, rcapiUser, rcapiPswd);

        // set up the OFTP Isdn client
        IsdnClient isdnClient = new IsdnClient(capi, factory);
        isdnClient.setIsdnConfigurator(new IsdnConfigurator() {
            public void configureChannel(IsdnChannel channel) {
                IsdnChannelConfig config = channel.getConfig();

				config.setMaxLogicalConnection(1);
				config.setMaxBDataBlocks(2);
				config.setMaxBDataLen(512);

				config.setCompatibilityInformationProfile(CompatibilityInformationProfile.UNRESTRICTED_DIGITAL);
				config.setB3(B3Protocol.X25_DTE_DTE);

				B3Configuration b3config = new B3Configuration(B3Protocol.TRANSPARENT.getBitField());
				b3config.setModuloMode(B3Configuration.MODULOMODE_NORMAL);
				b3config.setWindowSize(2);
				config.setB3Config(b3config);

				AdditionalInfo info = new AdditionalInfo();
				BChannelInformation bChannelInfo = new BChannelInformation();
				info.setBinfo(bChannelInfo);
				config.setAdditionalInfo(info);

            }
        });

        return isdnClient;
	}

    protected void connect() throws Exception {
    	connect(true);
    }

    protected void connect(boolean await) throws Exception {

        String calledAddr = System.getProperty("oftp.isdn.called-addr");
        String callingAddr = System.getProperty("oftp.isdn.calling-addr");

        ((IsdnClient) client).connect(calledAddr, callingAddr, await);
    }


}
