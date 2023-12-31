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

public interface VmmDllFlags {

    // do not use the data cache (force reading from memory acquisition device)
    long NOCACHE = 0x0001;
    // zero pad failed physical memory reads and report success if read within range of physical memory.
    long ZEROPAD_ON_FAIL = 0x0002;
    // force use of cache - fail non-cached pages - only valid for reads, invalid with NOCACHE/ZEROPAD_ON_FAIL.
    long FORCECACHE_READ = 0x0008;
    // do not try to retrieve memory from paged out memory from pagefile/compressed (even if possible)
    long NOPAGING = 0x0010;
    // do not try to retrieve memory from paged out memory if read would incur additional I/O (even if possible).
    long NOPAGING_IO = 0x0020;
    // do not write back to the data cache upon successful read from memory acquisition device.
    long NOCACHEPUT = 0x0100;
    // only fetch from the most recent active cache region when reading.
    long CACHE_RECENT_ONLY = 0x0200;
    // do not perform additional predictive page reads (default on smaller requests).
    long NO_PREDICTIVE_READ = 0x0400;

}
