/*
 * This file is part of ceserver-pcileech by Isabella Flores
 *
 * Copyright © 2021-2022 Isabella Flores
 *
 * It is licensed to you under the terms of the
 * GNU Affero General Public License, Version 3.0.
 * Please see the file LICENSE for more information.
 */

package iflores.ceserver.pcileech;

import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.WString;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public interface JnaPciLeech extends Library {

    boolean Initialize(int argc, String[] argv);

    boolean MemReadEx(
            int dwPID,
            long qwVA,
            Pointer pb,
            int cb,
            Pointer pcbReadOpt,
            long flags
    );

    boolean MemWrite(
            int dwPID,
            long qwVA,
            Pointer pb,
            int cb
    );

    boolean PidList(
            Pointer pPIDs,
            Pointer pcPIDs
    );

///////////////////////////////////////////////////////
    boolean Map_GetVadW_init(
            int dwPID,
            boolean fIdentifyModules
    );

    long Map_GetVadW_Count(); //element count of pMap

    boolean Map_GetVadW_Get( //get pMap
            VMMDLL_MAP_VADENTRY pMap,
            long start,
            long count
    );

    long GetVADEntrySize();
//////////////////////////////////////////////////////

    String ProcessGetInformationString(
            int dwPID,
            int fOptionString
    );

    long ProcessGetModuleBaseW(
            int dwPID,
            WString wszModuleName
    );

}