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

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;
import org.jetbrains.annotations.NotNull;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import static iflores.ceserver.pcileech.CommandConstants.*;
import static iflores.ceserver.pcileech.Win32Constants.TH32CS_SNAPMODULE;
import static iflores.ceserver.pcileech.Win32Constants.TH32CS_SNAPPROCESS;

public class ClientHandler extends Thread {

    private static final Map<Integer, SelectedProcess> _openProcesses = new HashMap<>();
    private static final Map<Integer, Object> _handlesById = new HashMap<>();
    private static final ReentrantLock _handleLock = new ReentrantLock();
    private static final Map<Integer, ClientHandler> _clients = new HashMap<>();
    private static int _nextHandleId = 1;
    private static int _nextClientId = 1;
    private final SocketChannel _socketChannel;
    private final int _clientId;
    private final String _clientIdString;

    public ClientHandler(SocketChannel socketChannel) throws IOException {
        _clientId = generateClientId();
        _clientIdString = "Client-" + String.format("%05d", _clientId);
        _socketChannel = socketChannel;
        String remoteAddress = ((InetSocketAddress) socketChannel.getRemoteAddress()).getAddress().getHostAddress();
        setName(this + " [" + remoteAddress + "]");
        log("Connection from " + remoteAddress);
    }

    private int generateClientId() {
        int clientId;
        while (true) {
            clientId = _nextClientId++;
            if (clientId > 99999) {
                clientId = 1;
            }
            synchronized (_clients) {
                if (_clients.putIfAbsent(clientId, this) == null) {
                    return clientId;
                }
            }
        }
    }

    private void log(String message) {
        Main.log(this, message);
    }

    @Override
    @NotNull
    public String toString() {
        return _clientIdString;
    }

    @Override
    public void run() {
        try {
            //noinspection InfiniteLoopStatement
            while (true) {
                ByteBuffer commandbuf = ByteBuffer.allocate(1);
                readFully(commandbuf);
                byte command = commandbuf.get(0);
                handleCommand(command);
            }
        } catch (EOFException ex) {
            // closed connection
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            synchronized (_clients) {
                _clients.remove(_clientId);
            }
            try {
                _socketChannel.close();
            } catch (Throwable t) {
                t.printStackTrace();
            }
            log("Connection closed.");
        }
    }

    private void handleCommand(byte command) throws IOException {
        if(Settings.DEBUG) {
            log(cmdToString(command));
        }
        switch (command) {
            case CMD_GETVERSION: {
                String versionString = "CHEATENGINE Network 2.1";
                byte[] versionBytes = versionString.getBytes(StandardCharsets.UTF_8);
                ByteBuffer result = ByteBuffer.allocate(5 + versionBytes.length);
                result.order(ByteOrder.LITTLE_ENDIAN);
                result.putInt(0, 2);
                result.put(4, (byte) versionBytes.length);
                result.put(5, versionBytes);
                writeFully(result);
                break;
            }
            case CMD_GETABI: {
                ByteBuffer result = ByteBuffer.allocate(1);
                result.order(ByteOrder.LITTLE_ENDIAN);
                writeFully(result);
                break;
            }
            case CMD_CREATETOOLHELP32SNAPSHOT: {
                int dwFlags = readInt();
                int th32ProcessID = readInt();
                int handleId;
                if ((dwFlags & TH32CS_SNAPPROCESS) != 0) {
                    ToolHelp32Snapshot_Processes snapshot = new ToolHelp32Snapshot_Processes();
                    handleId = generateHandleId(snapshot);
                } else if ((dwFlags & TH32CS_SNAPMODULE) != 0) {
                    SelectedProcess selectedProcess = new SelectedProcess(th32ProcessID);
                    ToolHelp32Snapshot_Modules snapshot = new ToolHelp32Snapshot_Modules(selectedProcess);
                    handleId = generateHandleId(snapshot);
                } else {
                    log("WARNING: Unhandled argument to CMD_CREATETOOLHELP32SNAPSHOT: " + dwFlags);
                    handleId = 0;
                }
                ByteBuffer result = ByteBuffer.allocate(4);
                result.order(ByteOrder.LITTLE_ENDIAN);
                result.putInt(0, handleId);
                writeFully(result);
                break;
            }
            case CMD_PROCESS32FIRST:
            case CMD_PROCESS32NEXT: {
                int handleId = readInt();
                ToolHelp32Snapshot_Processes snapshot = (ToolHelp32Snapshot_Processes) getHandle(handleId);
                if (snapshot != null) {
                    if (command == CMD_PROCESS32FIRST) {
                        snapshot.restartProcessInfo();
                    }
                    while (snapshot.hasNextProcessInfo()) {
                        ProcessInfo processInfo = snapshot.nextProcessInfo();
                        if (processInfo.getName() != null && !processInfo.getName().isEmpty()) {
                            writeCeProcessEntry(true, processInfo.getPid(), processInfo.getName());
                            return;
                        }
                    }
                }
                writeCeProcessEntry(false, 0, "");
                break;
            }
            case CMD_CLOSEHANDLE: {
                _handleLock.lock();
                try {
                    int handleId = readInt();
                    _handlesById.remove(handleId);
                    _openProcesses.remove(handleId);
                } finally {
                    _handleLock.unlock();
                }
                ByteBuffer buf = ByteBuffer.allocate(4);
                buf.order(ByteOrder.LITTLE_ENDIAN);
                buf.putInt(0, 1);
                writeFully(buf);
                break;
            }
            case CMD_OPENPROCESS: {
                int pid = readInt();
                int handleId = 0;
                try {
                    SelectedProcess selectedProcess = new SelectedProcess(pid);
                    _handleLock.lock();
                    try {
                        handleId = generateHandleId(selectedProcess);
                        _openProcesses.put(handleId, selectedProcess);
                    } finally {
                        _handleLock.unlock();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                ByteBuffer buf = ByteBuffer.allocate(4);
                buf.order(ByteOrder.LITTLE_ENDIAN);
                buf.putInt(0, handleId);
                writeFully(buf);
                break;
            }
            case CMD_READPROCESSMEMORY: {
                ByteBuffer buf = ByteBuffer.allocate(17);
                buf.order(ByteOrder.LITTLE_ENDIAN);
                readFully(buf);
                final int handleId = buf.getInt();
                final long address = buf.getLong();
                final int size = buf.getInt();
                final byte compress = buf.get();
                SelectedProcess selectedProcess = getOpenProcess(handleId);
                if (compress != 0) {
                    throw new IllegalArgumentException("Compression not yet supported");
                }
                if (selectedProcess != null) {
                    byte[] bytes = selectedProcess.readMemory(address, size);
                    ByteBuffer memoryBuf = ByteBuffer.allocate(bytes.length + 4);
                    if(Settings.DEBUG) {
                        log("success add="+Long.toHexString(address) +",size="+size+",returnSize="+bytes.length);
                    }
                    memoryBuf.order(ByteOrder.LITTLE_ENDIAN);
                    memoryBuf.putInt(bytes.length);
                    memoryBuf.put(bytes);
                    memoryBuf.flip();
                    writeFully(memoryBuf);
                } else {
                    if(Settings.DEBUG) {
                        log("failed add="+Long.toHexString(address) +",size="+size);
                    }
                    ByteBuffer memoryBuf = ByteBuffer.allocate(4);
                    memoryBuf.order(ByteOrder.LITTLE_ENDIAN);
                    memoryBuf.putInt(0);
                    memoryBuf.flip();
                    writeFully(memoryBuf);
                }
                break;
            }
            case CMD_WRITEPROCESSMEMORY: {
                ByteBuffer buf = ByteBuffer.allocate(16);
                buf.order(ByteOrder.LITTLE_ENDIAN);
                readFully(buf);
                int handleId = buf.getInt();
                long address = buf.getLong();
                int size = buf.getInt();
                ByteBuffer memoryBuf = ByteBuffer.allocate(size);
                memoryBuf.order(ByteOrder.LITTLE_ENDIAN);
                readFully(memoryBuf);
                SelectedProcess selectedProcess = getOpenProcess(handleId);
                if (selectedProcess != null) {
                    selectedProcess.writeMemory(address, memoryBuf.array());
                }
                ByteBuffer responseBuffer = ByteBuffer.allocate(4);
                responseBuffer.order(ByteOrder.LITTLE_ENDIAN);
                responseBuffer.putInt(0, 0);
                writeFully(responseBuffer);
                break;
            }
            case CMD_GETARCHITECTURE: {
                WinBase.SYSTEM_INFO si = new WinBase.SYSTEM_INFO();
                Kernel32.INSTANCE.GetSystemInfo(si);
                int architecture = si.processorArchitecture.pi.wProcessorArchitecture.intValue();
                byte result = switch (architecture) {
                    case 0 ->
                            // x86
                            0;
                    case 9 ->
                            // x64 (AMD or Intel)
                            1;
                    case 5 ->
                            // ARM
                            2;
                    case 12 ->
                            // ARM64
                            3;
                    default -> throw new RuntimeException("Unsupported architecture: #" + architecture);
                };
                ByteBuffer buf = ByteBuffer.allocate(1);
                buf.order(ByteOrder.LITTLE_ENDIAN);
                buf.put(0, result);
                writeFully(buf);
                break;
            }
            case CMD_MODULE32FIRST:
            case CMD_MODULE32NEXT: {
                int handleId = readInt();
                ToolHelp32Snapshot_Modules toolHelp32SnapshotModules = (ToolHelp32Snapshot_Modules) getHandle(handleId);
                if (toolHelp32SnapshotModules != null) {
                    if (command == CMD_MODULE32FIRST) {
                        toolHelp32SnapshotModules.restartModuleInfo();
                    }
                    if (toolHelp32SnapshotModules.hasNextModuleInfo()) {
                        MemoryRegion<VadInfo> memoryRegion = toolHelp32SnapshotModules.nextModuleInfo();
                        long moduleStart = memoryRegion.getRegionStart();
                        long moduleSize = memoryRegion.getRegionSize();
                        String moduleName = memoryRegion.getUserObject().getName();
                        if(moduleName == null || moduleName.isEmpty()) {
                            moduleName = "unknown-module-" + moduleStart;
                        }

                        if(Settings.DEBUG) {
                            log("Module=" + moduleName + ",start=" + moduleStart + ",size=" + moduleSize);
                        }
                        writeCeModuleEntry(true, moduleStart, moduleSize, moduleName);
                        return;
                    }
                }
                writeCeModuleEntry(false, 0L, 0L, "");
                break;
            }
            case CMD_GETSYMBOLLISTFROMFILE: {
                int symbolPathSize = readInt();
                ByteBuffer buf = ByteBuffer.allocate(symbolPathSize);
                readFully(buf);
                ByteBuffer response = ByteBuffer.allocate(8);
                response.order(ByteOrder.LITTLE_ENDIAN);
                writeFully(response);
                break;
            }
            case CMD_VIRTUALQUERYEX:
            case CMD_GETREGIONINFO: {
                int handleId = readInt();
                long address = readLong();
                SelectedProcess selectedProcess = (SelectedProcess) getHandle(handleId);
                String name = null;
                ByteBuffer response = ByteBuffer.allocate(25);
                response.order(ByteOrder.LITTLE_ENDIAN);
                if (selectedProcess == null) {
                    log("WARNING: Handle not found: " + handleId);
                } else {
                    MemoryRegion<VadInfo> memoryRange = selectedProcess.getMemoryMap().getMemoryRegionContaining(address);
                    if (memoryRange != null) {
                        long rangeStart = memoryRange.getRegionStart();
                        long rangeEnd = memoryRange.getRegionEnd();
                        VadInfo vadInfo = memoryRange.getUserObject();
                        name = vadInfo == null ? "" : vadInfo.getName();
                        response.put((byte) 1); // result
                        response.putInt(vadInfo == null ? 1 : vadInfo.getWin32Protection());
                        response.putInt(vadInfo == null ? 0 : vadInfo.getWin32Type());
                        response.putLong(rangeStart); // base address
                        response.putLong(rangeEnd - rangeStart + 1); // size
                        response.flip();
                    }
                }
                writeFully(response);
                if (command == CMD_GETREGIONINFO) {
                    if (name == null) {
                        name = "";
                    }
                    byte[] nameBytes = name.getBytes(StandardCharsets.ISO_8859_1);
                    int numBytes = Math.min(name.length(), 200);
                    ByteBuffer buf = ByteBuffer.allocate(1 + numBytes);
                    buf.order(ByteOrder.LITTLE_ENDIAN);
                    buf.put((byte) numBytes);
                    buf.put(nameBytes, 0, numBytes);
                    buf.flip();
                    writeFully(buf);
                }
                break;
            }
            case CMD_VIRTUALQUERYEXFULL: {
                int handleId = readInt();
                @SuppressWarnings("unused")
                byte flags = readByte();
                SelectedProcess selectedProcess = (SelectedProcess) getHandle(handleId);
                MemoryMap<VadInfo> memoryMap = selectedProcess.getMemoryMap();
                ByteBuffer response = ByteBuffer.allocate(4 + (memoryMap.getRegionCount() * 24));
                response.order(ByteOrder.LITTLE_ENDIAN);
                response.putInt(memoryMap.getRegionCount());
                for (MemoryRegion<VadInfo> memoryRegion : memoryMap) {
                    response.putLong(memoryRegion.getRegionStart());
                    response.putLong(memoryRegion.getRegionSize());
                    response.putInt(memoryRegion.getUserObject().getWin32Protection());
                    response.putInt(memoryRegion.getUserObject().getWin32Type());
                }
                if (response.hasRemaining()) {
                    throw new IllegalStateException();
                }
                response.flip();
                writeFully(response);
                break;
            }
            default:
                throw new RuntimeException("Got unknown command: " + cmdToString(command));
        }
    }

    private SelectedProcess getOpenProcess(int handleId) {
        SelectedProcess selectedProcess;
        _handleLock.lock();
        try {
            selectedProcess = _openProcesses.get(handleId);
        } finally {
            _handleLock.unlock();
        }
        return selectedProcess;
    }

    private Object getHandle(int handleId) {
        _handleLock.lock();
        try {
            return _handlesById.get(handleId);
        } finally {
            _handleLock.unlock();
        }
    }

    private int generateHandleId(Object handle) {
        _handleLock.lock();
        try {
            int handleId;
            do {
                handleId = _nextHandleId++;
            }
            while (handleId == 0 || _handlesById.putIfAbsent(handleId, handle) != null);
            return handleId;
        } finally {
            _handleLock.unlock();
        }
    }

    private byte readByte() throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(1);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        readFully(buf);
        return buf.get(0);
    }

    private int readInt() throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        readFully(buf);
        return buf.getInt(0);
    }

    private long readLong() throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(8);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        readFully(buf);
        return buf.getLong(0);
    }

    private void writeCeProcessEntry(boolean hasNext, int pid, String processName) throws IOException {
        processName = Main.getLastPathComponent(processName);
        byte[] processNameBytes = processName.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buf = ByteBuffer.allocate(12 + processNameBytes.length);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(hasNext ? 1 : 0);
        buf.putInt(pid);
        buf.putInt(processNameBytes.length);
        buf.put(processNameBytes);
        buf.flip();
        writeFully(buf);
    }

    private void writeCeModuleEntry(boolean hasNext, long moduleBase, long moduleSize, String moduleName) throws IOException {
        if (moduleName == null) {
            moduleName = "";
        }
        moduleName = Main.getLastPathComponent(moduleName);
        if (moduleSize > 0xffffffffL) {
            throw new IllegalArgumentException();
        }
        byte[] moduleNameBytes = moduleName.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buf = ByteBuffer.allocate(24 + moduleNameBytes.length);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(hasNext ? 1 : 0);
        buf.putLong(moduleBase);
        buf.putInt(0); // module "part"
        buf.putInt((int) moduleSize);
        buf.putInt(moduleNameBytes.length);
        buf.put(moduleNameBytes);
        buf.flip();
        writeFully(buf);
    }

    private void writeFully(ByteBuffer result) throws IOException {
        if (result.order() != ByteOrder.LITTLE_ENDIAN) {
            throw new IllegalStateException();
        }
        while (result.hasRemaining()) {
            _socketChannel.write(result);
        }
    }

    private void readFully(ByteBuffer buf) throws IOException {
        while (buf.hasRemaining()) {
            int count = _socketChannel.read(buf);
            if (count < 0) {
                throw new EOFException();
            }
        }
        buf.flip();
    }

}
