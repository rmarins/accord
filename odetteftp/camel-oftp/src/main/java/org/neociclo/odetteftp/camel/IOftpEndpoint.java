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
package org.neociclo.odetteftp.camel;

import java.util.Observable;
import java.util.concurrent.Executor;

import org.apache.camel.Endpoint;
import org.apache.camel.ExchangePattern;
import org.jboss.netty.util.Timer;

/**
 * @author Rafael Marins
 */
interface IOftpEndpoint extends Endpoint {

	void setTimer(Timer timer);

	OftpSettings getSettings();

	Executor getBossExecutor();

	Executor getWorkerExecutor();

	Timer getTimer();

	OftpBinding getBinding();

	ExchangePattern getExchangePattern();

	IOftpConsumer getConsumer();

	OftpOperations getOperations();

	Observable getOutgoingRequestsObservable();

}
