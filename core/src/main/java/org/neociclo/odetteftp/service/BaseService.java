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
package org.neociclo.odetteftp.service;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
abstract class BaseService {

    private Set<Object> instanceManagedBeans = new HashSet<Object>();
	private boolean loggingDisabled;

    public void disableLogging() {
    	loggingDisabled = true;
    }

    public void enableLogging() {
    	loggingDisabled = false;
    }

	public boolean isLoggingDisabled() {
		return loggingDisabled;
	}

    protected void setManaged(Object bean) {
    	instanceManagedBeans.add(bean);
    }

    protected void unsetManaged(Object bean) {
    	instanceManagedBeans.remove(bean);
    }

    protected boolean isManaged(Object bean) {
    	return instanceManagedBeans.contains(bean);
    }
}
