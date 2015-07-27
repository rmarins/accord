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
package org.neociclo.odetteftp.protocol.v20;

import static org.neociclo.odetteftp.util.CommandFormatConstants.*;

import static org.neociclo.odetteftp.protocol.CommandFormat.Field.ALPHANUMERIC_TYPE;
import static org.neociclo.odetteftp.protocol.CommandFormat.Field.CR_TYPE;
import static org.neociclo.odetteftp.protocol.CommandFormat.Field.BINARY_TYPE;
import static org.neociclo.odetteftp.protocol.CommandFormat.Field.ENCODED_TYPE;
import static org.neociclo.odetteftp.protocol.CommandFormat.Field.FIXED_FORMAT;
import static org.neociclo.odetteftp.protocol.CommandFormat.Field.NUMERIC_TYPE;
import static org.neociclo.odetteftp.protocol.CommandFormat.Field.VARIABLE_FORMAT;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.AUCH;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.AURP;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.CD;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.CDT;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.EERP;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.EFID;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.EFNA;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.EFPA;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.ESID;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.NERP;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.RTR;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.SECD;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.SFID;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.SFNA;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.SFPA;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.SSID;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.SSRM;

import org.neociclo.odetteftp.protocol.CommandFormat;
import org.neociclo.odetteftp.protocol.CommandIdentifier;

/**
 * @author Rafael Marins
 */
public enum ReleaseFormatVer20 implements CommandFormat {

    /** Change Direction command format. */
    CD_V20(CD, new Field[] { new Field(0, CDCMD_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 1) }),

    /** Set Credit command format. */
    CDT_V20(CDT, new Field[] { new Field(0, CDTCMD_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 1),
            new Field(1, CDTRSV1_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 2)
    }),

    /** End-to-End Response command format. */
    EERP_V20(EERP, new Field[] { new Field(0, EERPCMD_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 1),
            new Field(1, EERPDSN_FIELD, VARIABLE_FORMAT, ALPHANUMERIC_TYPE, 26),
            new Field(27, EERPRSV1_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 3),
            new Field(30, EERPDATE_FIELD, VARIABLE_FORMAT, ALPHANUMERIC_TYPE, 8),
            new Field(38, EERPTIME_FIELD, VARIABLE_FORMAT, ALPHANUMERIC_TYPE, 10),
            new Field(48, EERPUSER_FIELD, VARIABLE_FORMAT, ALPHANUMERIC_TYPE, 8),
            new Field(56, EERPDEST_FIELD, VARIABLE_FORMAT, ALPHANUMERIC_TYPE, 25),
            new Field(81, EERPORIG_FIELD, VARIABLE_FORMAT, ALPHANUMERIC_TYPE, 25),
            new Field(106, EERPHSHL_FIELD, VARIABLE_FORMAT, BINARY_TYPE, 2),               // Virtual File hash length
            new Field(108, EERPHSH_FIELD, VARIABLE_FORMAT, BINARY_TYPE, EERPHSHL_FIELD),        // Virtual File hash
            new Field(EERPHSH_FIELD, EERPSIGL_FIELD, VARIABLE_FORMAT, BINARY_TYPE, 2),         // EERP signature length
            new Field(EERPSIGL_FIELD, EERPSIG_FIELD, VARIABLE_FORMAT, BINARY_TYPE, EERPSIGL_FIELD)  // Virtual File hash
    }),


    /** End File command format. */
    EFID_V20(EFID, new Field[] { new Field(0, EFIDCMD_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 1),
            new Field(1, EFIDRCNT_FIELD, VARIABLE_FORMAT, NUMERIC_TYPE, 17),
            new Field(18, EFIDUCNT_FIELD, VARIABLE_FORMAT, NUMERIC_TYPE, 17) }),

    /** End File Negative Answer command format. */
    EFNA_V20(EFNA, new Field[] { new Field(0, EFNACMD_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 1),
            new Field(1, EFNAREAS_FIELD, FIXED_FORMAT, NUMERIC_TYPE, 2),
            new Field(3, EFNAREASL_FIELD, VARIABLE_FORMAT, NUMERIC_TYPE, 3),        // Answer Reason Text Length
            new Field(6, EFNAREAST_FIELD, FIXED_FORMAT, ENCODED_TYPE, EFNAREASL_FIELD)  // Answer Reason Text
    }),

    /** End File Positive Answer command format. */
    EFPA_V20(EFPA, new Field[] { new Field(0, EFPACMD_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 1),
            new Field(1, EFPACD_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 1)
    }),

    /** End Session command format. */
    ESID_V20(ESID, new Field[] { new Field(0, ESIDCMD_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 1),
            new Field(1, ESIDREAS_FIELD, FIXED_FORMAT, NUMERIC_TYPE, 2),
            new Field(3, ESIDREASL_FIELD, VARIABLE_FORMAT, NUMERIC_TYPE, 3),         // Answer Reason Text Length
            new Field(6, ESIDREAST_FIELD, FIXED_FORMAT, ENCODED_TYPE, ESIDREASL_FIELD),  // Answer Reason Text
            new Field(ESIDREAST_FIELD, ESIDCR_FIELD, FIXED_FORMAT, CR_TYPE, 1)
    }),

    /** Negative End Response command format. */
    NERP_V20(NERP, new Field[] { new Field(0, NERPCMD_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 1),
            new Field(1, NERPDSN_FIELD, VARIABLE_FORMAT, ALPHANUMERIC_TYPE, 26),
            new Field(27, NERPRSV1_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 6),
            new Field(33, NERPDATE_FIELD, VARIABLE_FORMAT, NUMERIC_TYPE, 8),
            new Field(41, NERPTIME_FIELD, VARIABLE_FORMAT, NUMERIC_TYPE, 10),
            new Field(51, NERPDEST_FIELD, VARIABLE_FORMAT, ALPHANUMERIC_TYPE, 25),
            new Field(76, NERPORIG_FIELD, VARIABLE_FORMAT, ALPHANUMERIC_TYPE, 25),
            new Field(101, NERPCREA_FIELD, VARIABLE_FORMAT, ALPHANUMERIC_TYPE, 25),
            new Field(126, NERPREAS_FIELD, FIXED_FORMAT, NUMERIC_TYPE, 2),
            new Field(128, NERPREASL_FIELD, VARIABLE_FORMAT, NUMERIC_TYPE, 3),                     // Reason Text Length
            new Field(131, NERPREAST_FIELD, FIXED_FORMAT, ENCODED_TYPE, NERPREASL_FIELD),          // Reason Text
            new Field(NERPREAST_FIELD, NERPHSHL_FIELD, VARIABLE_FORMAT, BINARY_TYPE, 2),           // Virtual File hash length
            new Field(NERPHSHL_FIELD, NERPHSH_FIELD, FIXED_FORMAT, BINARY_TYPE, NERPHSHL_FIELD),  // Virtual File hash
            new Field(NERPHSH_FIELD, NERPSIGL_FIELD, VARIABLE_FORMAT, BINARY_TYPE, 2),            // NERP signature length
            new Field(NERPSIGL_FIELD, NERPSIG_FIELD, FIXED_FORMAT, BINARY_TYPE, NERPSIGL_FIELD)   // NERP signature
    }),

    /** Ready To Receive command format. */
    RTR_V20(RTR, new Field[] { new Field(0, RTRCMD_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 1) }),

    /** Start File command format. */
    SFID_V20(SFID, new Field[] { new Field(0, SFIDCMD_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 1),
            new Field(1, SFIDDSN_FIELD, VARIABLE_FORMAT, ALPHANUMERIC_TYPE, 26),
            new Field(27, SFIDRSV1_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 3),
            new Field(30, SFIDDATE_FIELD, VARIABLE_FORMAT, ALPHANUMERIC_TYPE, 8),
            new Field(38, SFIDTIME_FIELD, VARIABLE_FORMAT, ALPHANUMERIC_TYPE, 10),
            new Field(48, SFIDUSER_FIELD, VARIABLE_FORMAT, ALPHANUMERIC_TYPE, 8),
            new Field(56, SFIDDEST_FIELD, VARIABLE_FORMAT, ALPHANUMERIC_TYPE, 25),
            new Field(81, SFIDORIG_FIELD, VARIABLE_FORMAT, ALPHANUMERIC_TYPE, 25),
            new Field(106, SFIDFMT_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 1),
            new Field(107, SFIDLRECL_FIELD, VARIABLE_FORMAT, NUMERIC_TYPE, 5),
            new Field(112, SFIDFSIZ_FIELD, VARIABLE_FORMAT, NUMERIC_TYPE, 13),
            new Field(125, SFIDOSIZ_FIELD, VARIABLE_FORMAT, NUMERIC_TYPE, 13),          // Original File Size, 1K blocks
            new Field(138, SFIDREST_FIELD, VARIABLE_FORMAT, NUMERIC_TYPE, 17),
            new Field(155, SFIDSEC_FIELD, FIXED_FORMAT, NUMERIC_TYPE, 2),               // Security Level
            new Field(157, SFIDCIPH_FIELD, FIXED_FORMAT, NUMERIC_TYPE, 2),              // Cipher suite selection
            new Field(159, SFIDCOMP_FIELD, FIXED_FORMAT, NUMERIC_TYPE, 1),              // File compression algorithm
            new Field(160, SFIDENV_FIELD, FIXED_FORMAT, NUMERIC_TYPE, 1),               // File enveloping format
            new Field(161, SFIDSIGN_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 1),         // Signed EERP request
            new Field(162, SFIDDESCL_FIELD, VARIABLE_FORMAT, NUMERIC_TYPE, 3),          // Virtual File Description length
            new Field(165, SFIDDESC_FIELD, VARIABLE_FORMAT, ENCODED_TYPE, SFIDDESCL_FIELD)  // Virtual File Description
    }),

    /** Start File Negative Answer command format. */
    SFNA_V20(SFNA, new Field[] { new Field(0, SFNACMD_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 1),
            new Field(1, SFNAREAS_FIELD, FIXED_FORMAT, NUMERIC_TYPE, 2),
            new Field(3, SFNARRTR_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 1),
            new Field(4, SFNAREASL_FIELD, VARIABLE_FORMAT, NUMERIC_TYPE, 3),
            new Field(7, SFNAREAST_FIELD, VARIABLE_FORMAT, ENCODED_TYPE, SFNAREASL_FIELD)
    }),

    /** Start File Positive Answer command format. */
    SFPA_V20(SFPA, new Field[] { new Field(0, SFPACMD_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 1),
            new Field(1, SFPAACNT_FIELD, VARIABLE_FORMAT, NUMERIC_TYPE, 17)
    }),

    /** Start Session command format. */
    SSID_V20(SSID, new Field[] { new Field(0, SSIDCMD_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 1),
            new Field(1, SSIDLEV_FIELD, FIXED_FORMAT, NUMERIC_TYPE, 1),
            new Field(2, SSIDCODE_FIELD, VARIABLE_FORMAT, ALPHANUMERIC_TYPE, 25),
            new Field(27, SSIDPSWD_FIELD, VARIABLE_FORMAT, ALPHANUMERIC_TYPE, 8),
            new Field(35, SSIDSDEB_FIELD, VARIABLE_FORMAT, NUMERIC_TYPE, 5),
            new Field(40, SSIDSR_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 1),
            new Field(41, SSIDCMPR_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 1),
            new Field(42, SSIDREST_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 1),
            new Field(43, SSIDSPEC_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 1),
            new Field(44, SSIDCRED_FIELD, VARIABLE_FORMAT, NUMERIC_TYPE, 3),
            new Field(47, SSIDAUTH_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 1),
            new Field(48, SSIDRSV1_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 4),
            new Field(52, SSIDUSER_FIELD, VARIABLE_FORMAT, ALPHANUMERIC_TYPE, 8),
            new Field(60, SSIDCR_FIELD, FIXED_FORMAT, CR_TYPE, 1)
    }),

    /** Start Session Ready Message command format. */
    SSRM_V20(SSRM, new Field[] { new Field(0, SSRMCMD_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 1),
            new Field(1, SSRMMSG_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 17),
            new Field(18, SSRMCR_FIELD, FIXED_FORMAT, CR_TYPE, 1)
    }),

    /** Security Change Direction. */
    SECD_V20(SECD, new Field[] { new Field(0, SECDCMD_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 1) }),

    /** Authentication Challenge. */
    AUCH_V20(AUCH, new Field[] { new Field(0, AUCHCMD_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 1),
            new Field(1, AUCHCHLL_FIELD, VARIABLE_FORMAT, BINARY_TYPE, 2),          // Challenge Length
            new Field(3, AUCHCHAL_FIELD, VARIABLE_FORMAT, BINARY_TYPE, AUCHCHLL_FIELD)  // Challenge
    }),

    /** Authentication Response. */
    AURP_V20(AURP, new Field[] { new Field(0, AURPCMD_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 1),
            new Field(1, AURPRSP_FIELD, FIXED_FORMAT, BINARY_TYPE, 20)   // Response
    });


    /**
     * Return the Command Format definition given a specific Command identifier
     * code of ODETTE-FTP version 2.0.
     * 
     * @param identifier
     *        One of the <code>CommandIdentifier</code> static attribute.
     * @return The corresponding Command Format definition.
     */
    public static CommandFormat getFormat(CommandIdentifier identifier) {

        CommandFormat found = null;

        for (CommandFormat cf : ReleaseFormatVer20.values()) {
            if (identifier == cf.getIdentifier()) {
                found = cf;
                break;
            }
        }

        if (found == null) {
            throw new IllegalArgumentException("Illegal Command Format identifier: " + identifier);
        }

        return found;
    }

    private Field[] fields;

    private CommandIdentifier identifier;

    private ReleaseFormatVer20(CommandIdentifier identifier, Field[] fields) {
        this.identifier = identifier;
        this.fields = fields;
    }

    public Field getField(String name) {

        Field found = null;

        for (Field ff : fields) {
            if (ff.getName() == name) {
                found = ff;
                break;
            }
        }

        if (found == null) {
            throw new IllegalArgumentException("Illegal Field name: " + name);
        }

        return found;
    }

    public String[] getFieldNames() {

        String[] names = new String[fields.length];

        for (int i = 0; i < fields.length; i++)
            names[i] = fields[i].getName();

        return names;

    }

    public CommandIdentifier getIdentifier() {
        return identifier;
    }

    /**
     * On protocol version 2.0 implementation the command size must be computed
     * dynamically based on 'dynamic fields' definition.
     *
     * @return Return -1 as it shouldn't be used on v2.0
     */
    public int getSize() {
        return -1;
    }

}
