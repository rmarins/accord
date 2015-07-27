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
package org.neociclo.odetteftp.protocol.v14;

import static org.neociclo.odetteftp.util.CommandFormatConstants.*;
import static org.neociclo.odetteftp.protocol.CommandFormat.Field.ALPHANUMERIC_TYPE;
import static org.neociclo.odetteftp.protocol.CommandFormat.Field.CR_TYPE;
import static org.neociclo.odetteftp.protocol.CommandFormat.Field.FIXED_FORMAT;
import static org.neociclo.odetteftp.protocol.CommandFormat.Field.NUMERIC_TYPE;
import static org.neociclo.odetteftp.protocol.CommandFormat.Field.VARIABLE_FORMAT;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.CD;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.CDT;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.EERP;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.EFID;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.EFNA;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.EFPA;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.ESID;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.NERP;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.RTR;
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
public enum ReleaseFormatVer14 implements CommandFormat {

    /** Change Direction command format. */
    CD_V14(CD, new Field[] { new Field(0, CDCMD_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 1) }),

    /** Set Credit command format. */
    CDT_V14(CDT, new Field[] { new Field(0, CDTCMD_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 1),
            new Field(1, CDTRSV1_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 2) }),

    /** End-to-End Response command format. */
    EERP_V14(EERP, new Field[] { new Field(0, EERPCMD_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 1),
            new Field(1, EERPDSN_FIELD, VARIABLE_FORMAT, ALPHANUMERIC_TYPE, 26),
            new Field(27, EERPRSV1_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 3),
            new Field(30, EERPDATE_FIELD, VARIABLE_FORMAT, ALPHANUMERIC_TYPE, 8),
            new Field(38, EERPTIME_FIELD, VARIABLE_FORMAT, ALPHANUMERIC_TYPE, 10),
            new Field(48, EERPUSER_FIELD, VARIABLE_FORMAT, ALPHANUMERIC_TYPE, 8),
            new Field(56, EERPDEST_FIELD, VARIABLE_FORMAT, ALPHANUMERIC_TYPE, 25),
            new Field(81, EERPORIG_FIELD, VARIABLE_FORMAT, ALPHANUMERIC_TYPE, 25) }),

    /** End File command format. */
    EFID_V14(EFID, new Field[] { new Field(0, EFIDCMD_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 1),
            new Field(1, EFIDRCNT_FIELD, VARIABLE_FORMAT, NUMERIC_TYPE, 9),
            new Field(10, EFIDUCNT_FIELD, VARIABLE_FORMAT, NUMERIC_TYPE, 12) }),

    /** End File Negative Answer command format. */
    EFNA_V14(EFNA, new Field[] { new Field(0, EFNACMD_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 1),
            new Field(1, EFNAREAS_FIELD, FIXED_FORMAT, NUMERIC_TYPE, 2) }),

    /** End File Positive Answer command format. */
    EFPA_V14(EFPA, new Field[] { new Field(0, EFPACMD_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 1),
            new Field(1, EFPACD_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 1) }),

    /** End Session command format. */
    ESID_V14(ESID, new Field[] { new Field(0, ESIDCMD_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 1),
            new Field(1, ESIDREAS_FIELD, FIXED_FORMAT, NUMERIC_TYPE, 2),
            new Field(3, ESIDCR_FIELD, FIXED_FORMAT, CR_TYPE, 1) }),

    /** Negative End Response command format. */
    NERP_V14(NERP, new Field[] { new Field(0, NERPCMD_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 1),
            new Field(1, NERPDSN_FIELD, VARIABLE_FORMAT, ALPHANUMERIC_TYPE, 26),
            new Field(27, NERPRSV1_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 6),
            new Field(33, NERPDATE_FIELD, VARIABLE_FORMAT, ALPHANUMERIC_TYPE, 8),
            new Field(41, NERPTIME_FIELD, VARIABLE_FORMAT, ALPHANUMERIC_TYPE, 10),
            new Field(51, NERPDEST_FIELD, VARIABLE_FORMAT, ALPHANUMERIC_TYPE, 25),
            new Field(76, NERPORIG_FIELD, VARIABLE_FORMAT, ALPHANUMERIC_TYPE, 25),
            new Field(101, NERPCREA_FIELD, VARIABLE_FORMAT, ALPHANUMERIC_TYPE, 25),
            new Field(126, NERPREAS_FIELD, FIXED_FORMAT, NUMERIC_TYPE, 2) }),

    /** Ready To Receive command format. */
    RTR_V14(RTR, new Field[] { new Field(0, RTRCMD_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 1) }),

    /** Start File command format. */
    SFID_V14(SFID, new Field[] { new Field(0, SFIDCMD_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 1),
            new Field(1, SFIDDSN_FIELD, VARIABLE_FORMAT, ALPHANUMERIC_TYPE, 26),
            new Field(27, SFIDRSV1_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 3),
            new Field(30, SFIDDATE_FIELD, VARIABLE_FORMAT, ALPHANUMERIC_TYPE, 8),
            new Field(38, SFIDTIME_FIELD, VARIABLE_FORMAT, ALPHANUMERIC_TYPE, 10),
            new Field(48, SFIDUSER_FIELD, VARIABLE_FORMAT, ALPHANUMERIC_TYPE, 8),
            new Field(56, SFIDDEST_FIELD, VARIABLE_FORMAT, ALPHANUMERIC_TYPE, 25),
            new Field(81, SFIDORIG_FIELD, VARIABLE_FORMAT, ALPHANUMERIC_TYPE, 25),
            new Field(106, SFIDFMT_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 1),
            new Field(107, SFIDLRECL_FIELD, VARIABLE_FORMAT, NUMERIC_TYPE, 5),
            new Field(112, SFIDFSIZ_FIELD, VARIABLE_FORMAT, NUMERIC_TYPE, 7),
            new Field(119, SFIDREST_FIELD, VARIABLE_FORMAT, NUMERIC_TYPE, 9) }),

    /** Start File Negative Answer command format. */
    SFNA_V14(SFNA, new Field[] { new Field(0, SFNACMD_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 1),
            new Field(1, SFNAREAS_FIELD, FIXED_FORMAT, NUMERIC_TYPE, 2),
            new Field(3, SFNARRTR_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 1) }),

    /** Start File Positive Answer command format. */
    SFPA_V14(SFPA, new Field[] { new Field(0, SFPACMD_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 1),
            new Field(1, SFPAACNT_FIELD, VARIABLE_FORMAT, NUMERIC_TYPE, 9) }),

    /** Start Session command format. */
    SSID_V14(SSID, new Field[] { new Field(0, SSIDCMD_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 1),
            new Field(1, SSIDLEV_FIELD, FIXED_FORMAT, NUMERIC_TYPE, 1),
            new Field(2, SSIDCODE_FIELD, VARIABLE_FORMAT, ALPHANUMERIC_TYPE, 25),
            new Field(27, SSIDPSWD_FIELD, VARIABLE_FORMAT, ALPHANUMERIC_TYPE, 8),
            new Field(35, SSIDSDEB_FIELD, VARIABLE_FORMAT, NUMERIC_TYPE, 5),
            new Field(40, SSIDSR_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 1),
            new Field(41, SSIDCMPR_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 1),
            new Field(42, SSIDREST_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 1),
            new Field(43, SSIDSPEC_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 1),
            new Field(44, SSIDCRED_FIELD, VARIABLE_FORMAT, NUMERIC_TYPE, 3),
            new Field(47, SSIDRSV1_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 5),
            new Field(52, SSIDUSER_FIELD, VARIABLE_FORMAT, ALPHANUMERIC_TYPE, 8),
            new Field(60, SSIDCR_FIELD, FIXED_FORMAT, CR_TYPE, 1) }),

    /** Start Session Ready Message command format. */
    SSRM_V14(SSRM, new Field[] { new Field(0, SSRMCMD_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 1),
            new Field(1, SSRMMSG_FIELD, FIXED_FORMAT, ALPHANUMERIC_TYPE, 17),
            new Field(18, SSRMCR_FIELD, FIXED_FORMAT, CR_TYPE, 1) });

    /**
     * Return the Command Format definition given a specific Command identifier
     * code of ODETTE-FTP version 1.4.
     * 
     * @param identifier
     *        One of the <code>CommandIdentifier</code> static attribute.
     * @return The corresponding Command Format definition.
     */
    public static CommandFormat getFormat(CommandIdentifier identifier) {

        CommandFormat found = null;

        for (CommandFormat cf : ReleaseFormatVer14.values()) {
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

    private int size;

    private ReleaseFormatVer14(CommandIdentifier identifier, Field[] fields) {
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

    public int getSize() {

        if (size <= 0) {
            size = 0;
            for (String fieldName : getFieldNames()) {
                Field f = getField(fieldName);
                size += f.getSize();
            }
        }

        return size;
    }

}
