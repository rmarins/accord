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
package org.neociclo.filetransfer.oftp;

import org.neociclo.accord.core.IContainer;
import org.neociclo.accord.filetransfer.spi.IFileTransferProvider;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class OdetteFtpProvider implements IFileTransferProvider {

    public static final String CONTAINER_TYPE_FACTORY_NAME = "accord.odette-ftp";

    public static final String[] SUPPORTED_URI_SCHEMES = { "oftp", "oftps" };

    public IContainer createContainer() {
        return new OdetteFtpContainer();
    }

    public String getContainerTypeFactoryName() {
        return CONTAINER_TYPE_FACTORY_NAME;
    }

    public String[] getSupportedUriSchemes() {
        return SUPPORTED_URI_SCHEMES;
    }

}
