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
package org.neociclo.odetteftp.util;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class CommandFormatConstants {

    /**
     * Carriage return string used in some command. The specification determine
     * it could be defined as 0x0a or 0x08.
     */
    public static final String PROTOCOL_CARRIAGE_RETURN = "\r";

    /** Change Direction command field. */
    public static final String CDCMD_FIELD = "CDCMD";

    /** Set Credit command field. */
    public static final String CDTCMD_FIELD = "CDTCMD";
    public static final String CDTRSV1_FIELD = "CDTRSV1";

    /** End-to-End Response command field. */
    public static final String EERPCMD_FIELD = "EERPCMD";
    public static final String EERPDSN_FIELD = "EERPDSN";
    public static final String EERPRSV1_FIELD = "EERPRSV1";
    public static final String EERPDATE_FIELD = "EERPDATE";
    public static final String EERPTIME_FIELD = "EERPTIME";
    public static final String EERPUSER_FIELD = "EERPUSER";
    public static final String EERPDEST_FIELD = "EERPDEST";
    public static final String EERPORIG_FIELD = "EERPORIG";
    public static final String EERPHSHL_FIELD = "EERPHSHL";
    public static final String EERPHSH_FIELD = "EERPHSH";
    public static final String EERPSIGL_FIELD = "EERPSIGL";
    public static final String EERPSIG_FIELD = "EERPSIG";

    /** Start Session Identification command field. */
    public static final String SSIDCMD_FIELD = "SSIDCMD";
    public static final String SSIDLEV_FIELD = "SSIDLEV";
    public static final String SSIDCODE_FIELD = "SSIDCODE";
    public static final String SSIDPSWD_FIELD = "SSIDPSWD";
    public static final String SSIDSDEB_FIELD = "SSIDSDEB";
    public static final String SSIDSR_FIELD = "SSIDSR";
    public static final String SSIDCMPR_FIELD = "SSIDCMPR";
    public static final String SSIDREST_FIELD = "SSIDREST";
    public static final String SSIDSPEC_FIELD = "SSIDSPEC";
    public static final String SSIDCRED_FIELD = "SSIDCRED";
    public static final String SSIDRSV1_FIELD = "SSIDRSV1";
    public static final String SSIDAUTH_FIELD = "SSIDAUTH";
    public static final String SSIDUSER_FIELD = "SSIDUSER";
    public static final String SSIDCR_FIELD = "SSIDCR";

    /** Start Session Ready Message command field. */
    public static final String SSRMCMD_FIELD = "SSRMCMD";
    public static final String SSRMMSG_FIELD = "SSRMMSG";
    public static final String SSRMCR_FIELD = "SSRMCR";

    public static final String SSRMMSG_VALUE = "ODETTE FTP READY";

    /** Ready to Receive command field. */
    public static final String RTRCMD_FIELD = "RTRCMD";

    /** End File Identification command field. */
    public static final String EFIDCMD_FIELD = "EFIDCMD";
    public static final String EFIDRCNT_FIELD = "EFIDRCNT";
    public static final String EFIDUCNT_FIELD = "EFIDUCNT";

    /** End File Negative Answer command field. */
    public static final String EFNACMD_FIELD = "EFNACMD";
    public static final String EFNAREAS_FIELD = "EFNAREAS";
    public static final String EFNAREASL_FIELD = "EFNAREASL";
    public static final String EFNAREAST_FIELD = "EFNAREAST";

    /** End File Positive Answer command field. */
    public static final String EFPACMD_FIELD = "EFPACMD";
    public static final String EFPACD_FIELD = "EFPACD";

    /** End Session Identification command field. */
    public static final String ESIDCMD_FIELD = "ESIDCMD";
    public static final String ESIDREAS_FIELD = "ESIDREAS";
    public static final String ESIDREASL_FIELD = "ESIDREASL";
    public static final String ESIDREAST_FIELD = "ESIDREAST";
    public static final String ESIDCR_FIELD = "ESIDCR";

    /** Start File Identification command field. */
    public static final String SFIDCMD_FIELD = "SFIDCMD";
    public static final String SFIDDSN_FIELD = "SFIDDSN";
    public static final String SFIDRSV1_FIELD = "SFIDRSV1";
    public static final String SFIDDATE_FIELD = "SFIDDATE";
    public static final String SFIDTIME_FIELD = "SFIDTIME";
    public static final String SFIDUSER_FIELD = "SFIDUSER";
    public static final String SFIDDEST_FIELD = "SFIDDEST";
    public static final String SFIDORIG_FIELD = "SFIDORIG";
    public static final String SFIDFMT_FIELD = "SFIDFMT";
    public static final String SFIDLRECL_FIELD = "SFIDRECL";
    public static final String SFIDFSIZ_FIELD = "SFIDFSIZ";
    public static final String SFIDOSIZ_FIELD = "SFIDOSIZ";
    public static final String SFIDREST_FIELD = "SFIDREST";
    public static final String SFIDSEC_FIELD = "SFIDSEC";
    public static final String SFIDCIPH_FIELD = "SFIDCIPH";
    public static final String SFIDCOMP_FIELD = "SFIDCOMP";
    public static final String SFIDENV_FIELD = "SFIDENV";
    public static final String SFIDSIGN_FIELD = "SFIDSIGN";
    public static final String SFIDDESCL_FIELD = "SFIDDESCL";
    public static final String SFIDDESC_FIELD = "SFIDDESC";

    /** Start File Negative Answer command field. */
    public static final String SFNACMD_FIELD = "SFNACMD";
    public static final String SFNAREAS_FIELD = "SFNAREAS";
    public static final String SFNARRTR_FIELD = "SFNARRTR";
    public static final String SFNAREASL_FIELD = "SFNAREASL";
    public static final String SFNAREAST_FIELD = "SFNAREAST";

    /** Start File Positive Answer command field. */
    public static final String SFPACMD_FIELD = "SFPACMD";
    public static final String SFPAACNT_FIELD = "SFPAACNT";

    /**  Negative End Response command field. */
    public static final String NERPCMD_FIELD = "NERPCMD";
    public static final String NERPDSN_FIELD = "NERPDSN";
    public static final String NERPRSV1_FIELD = "NERPRSV1";
    public static final String NERPDATE_FIELD = "NERPDATE";
    public static final String NERPTIME_FIELD = "NERPTIME";
    public static final String NERPDEST_FIELD = "NERPDEST";
    public static final String NERPORIG_FIELD = "NERPORIG";
    public static final String NERPCREA_FIELD = "NERPCREA";
    public static final String NERPREAS_FIELD = "NERPREAS";
    public static final String NERPREASL_FIELD = "NERPREASL";
    public static final String NERPREAST_FIELD = "NERPREAST";
    public static final String NERPHSHL_FIELD = "NERPHSHL";
    public static final String NERPHSH_FIELD = "NERPHSH";
    public static final String NERPSIGL_FIELD = "NERPSIGL";
    public static final String NERPSIG_FIELD = "NERPSIG";

    /** Secure Change Direction command field. */
    public static final String SECDCMD_FIELD = "SECDCMD";

    /** Authentication Challenge Response command field. */
    public static final String AURPCMD_FIELD = "AURPCMD";
    public static final String AURPRSP_FIELD = "AURPRSP";

    /** Authentication Challenge command field. */
    public static final String AUCHCMD_FIELD = "AUCHCMD";
    public static final String AUCHCHLL_FIELD = "AUCHCHLL";
    public static final String AUCHCHAL_FIELD = "AUCHCHAL";

}
