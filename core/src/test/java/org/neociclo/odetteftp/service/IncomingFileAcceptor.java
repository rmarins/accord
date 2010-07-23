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
package org.neociclo.odetteftp.service;

import java.util.Map;

import org.neociclo.odetteftp.protocol.VirtualFile;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public interface IncomingFileAcceptor {

    /**
     * <b>Params</b>
     * <ul>
     * <li>saveToFile: destination file</li>
     * <li>restartOffset (optional): restart file receive block/record offset</li>
     * </ul>
     * 
     * @param incomingFile
     * @param params
     * @return
     */
    boolean accept(VirtualFile incomingFile, Map<String, Object> params);

}
