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
	private boolean explicitUseHeapBufferFactory = true;

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

	public boolean isExplicitUseHeapBufferFactory() {
		return explicitUseHeapBufferFactory;
	}

	public void setExplicitUseHeapBufferFactory(boolean heapChannelBufferFactory) {
		this.explicitUseHeapBufferFactory = heapChannelBufferFactory;
	}

    
}
