/**
 * Neociclo Accord, Open Source B2Bi Middleware
 * Copyright (C) 2005-2009 Neociclo, http://www.neociclo.com
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
package org.neociclo.odetteftp.security;

import java.security.cert.X509Certificate;

import javax.security.auth.callback.Callback;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class PartnerCertificateCallback implements Callback {

    private String userCode;

    private X509Certificate certificate;

    public PartnerCertificateCallback(String oid) {
        super();
        this.userCode = oid;
    }

    public String getUserCode() {
        return userCode;
    }

    public X509Certificate getCertificate() {
        return certificate;
    }

    public void setCertificate(X509Certificate cert) {
        this.certificate = cert;
    }

}
