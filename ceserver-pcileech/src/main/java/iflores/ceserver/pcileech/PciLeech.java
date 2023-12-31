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

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

public class PciLeech {

    private static final JnaPciLeech _jnaPciLeech = Native.load(Settings.ADAPTER_DLL_NAME, JnaPciLeech.class);

    private PciLeech() {
    }

    @NotNull
    public static List<Integer> getPids() {
        long pcPIDs_malloc = Native.malloc(8);
        try {
            Pointer pcPIDs = new Pointer(pcPIDs_malloc);
            pcPIDs.clear(8);
            if (!_jnaPciLeech.PidList(Pointer.NULL, pcPIDs)) {
                throw new RuntimeException("Failed to get pid number.");
            }
            int bufferSize = (int)pcPIDs.getLong(0) * 4;
            long pdwPIDs_malloc = Native.malloc(bufferSize);
            try {
                Pointer pdwPIDs = new Pointer(pdwPIDs_malloc);
                pdwPIDs.clear(bufferSize);
                if (!_jnaPciLeech.PidList(pdwPIDs, pcPIDs)) {
                    throw new RuntimeException("Failed to get pids.");
                }
                List<Integer> results = new ArrayList<>();
                long numResults = pcPIDs.getLong(0);
                for (long i = 0; i < numResults; i++) {
                    results.add(pdwPIDs.getInt(i * 4));
                }
                return results;
            } finally {
                Native.free(pdwPIDs_malloc);
            }
        } finally {
            Native.free(pcPIDs_malloc);
        }
    }
    
    public static List<VadInfo> getVad(int pid, boolean identifyModules) {
        List<VadInfo> vadInfos = new ArrayList<>();
        if(_jnaPciLeech.Map_GetVadW_init(pid, identifyModules) == false) {
            throw new RuntimeException("Failed to init Vad list.");
        }

        long entrySize = _jnaPciLeech.GetVADEntrySize();
        long vadCount = _jnaPciLeech.Map_GetVadW_Count();
        //Main.log("getVad", "VAD Count=" + vadCount);

        final int PART_SIZE = 200;
        int haveGot = 0;
       
        while(haveGot < vadCount)
        {
            int count = (int)vadCount - haveGot;
            count = (count > PART_SIZE) ? PART_SIZE : count;
            int size = count * (int)entrySize;
                    
            long pVadMap_malloc = Native.malloc(size);
            Pointer pVadMap = new Pointer(pVadMap_malloc);
            pVadMap.clear(size);
            VMMDLL_MAP_VADENTRY map = new VMMDLL_MAP_VADENTRY(pVadMap);
                
            _jnaPciLeech.Map_GetVadW_Get(map, haveGot, count);

            VMMDLL_MAP_VADENTRY[] entries = (VMMDLL_MAP_VADENTRY[]) map.toArray(count);
            for (VMMDLL_MAP_VADENTRY entry : entries) {
                VadInfo vadInfo = new VadInfo(
                        entry.wszText.toString(),
                        entry.vaStart,
                        entry.vaEnd,
                        entry._dword0,
                        entry._dword1
                );
                vadInfos.add(vadInfo);
            }
            Native.free(pVadMap_malloc);
            haveGot += count;
        }
        return vadInfos;

    }
    //#define VMMDLL_PROCESS_INFORMATION_OPT_STRING_PATH_KERNEL           1
    //#define VMMDLL_PROCESS_INFORMATION_OPT_STRING_PATH_USER_IMAGE       2
    //#define VMMDLL_PROCESS_INFORMATION_OPT_STRING_CMDLINE               3

    public static String getProcessExecutableName(int pid) {
        return _jnaPciLeech.ProcessGetInformationString(
                pid,
                2
        );
    }
    
    public static byte[] readMemory(int pid, long address, int size, long flags) {
        if (size <= 0 || address == 0) {
            return new byte[0];
        }
        if (size > 1024 * 1024 * 1024) {
            throw new IllegalArgumentException();
        }

        long buffer_malloc = Native.malloc(size);
        Pointer pBuffer = new Pointer(buffer_malloc);
        long pcbReadOpt_malloc = Native.malloc(4);
        Pointer pcbReadOpt = new Pointer(pcbReadOpt_malloc);

        Pointer readPointer = pBuffer;
        int bytesRemain = size;
        try {
            while (true) {
                boolean result = _jnaPciLeech.MemReadEx(
                        pid,
                        address,
                        readPointer,
                        bytesRemain,
                        pcbReadOpt,
                        flags
                );
                if (!result) {
                    throw new RuntimeException("Failed to read memory.");
                }
                int bytesRead = pcbReadOpt.getInt(0);
                bytesRemain -= bytesRead;
                if (bytesRemain <= 0 || bytesRead <= 0) { //if read complete or cannot read
                    break;
                }
                address += bytesRead;
                readPointer = readPointer.share(bytesRead);
            }
            int totalRead = size - bytesRemain;
            byte[] buf = new byte[totalRead];
            pBuffer.read(0L, buf, 0, totalRead);
            return buf;
        } finally {
            Native.free(pcbReadOpt_malloc);
            Native.free(buffer_malloc);
        }

    }

    public static void writeMemory(int pid, long address, byte[] bytes) {
        if (address < 0) {
            throw new IllegalArgumentException();
        }
        int size = bytes.length;
        long buffer_malloc = Native.malloc(size);
        Pointer pb = new Pointer(buffer_malloc);
        pb.write(0, bytes, 0, size);
        try {
            boolean result = _jnaPciLeech.MemWrite(
                    pid,
                    address,
                    pb,
                    size
            );
            if (!result) {
                throw new RuntimeException("Failed to write memory.");
            }
        } finally {
            Native.free(buffer_malloc);
        }
    }

    public static boolean initialize(String[] args) {
        return _jnaPciLeech.Initialize(args.length, args);
    }

}
