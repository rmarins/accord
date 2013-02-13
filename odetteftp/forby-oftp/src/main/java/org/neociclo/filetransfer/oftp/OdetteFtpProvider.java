/**
 * Neociclo Accord, Open Source B2Bi Middleware
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
