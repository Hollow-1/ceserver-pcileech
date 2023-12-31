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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ServerMain {
    
    public static void main(String[] args) {
        try {
            if (args.length != 3) {
                Main.log("ServerMain", "ERROR: Expected 3 command line arguments");
                System.exit(-1);
            }
            int port = Integer.parseInt(args[0]);
            // start thread that ends the process on any input from parent process
            new Thread(() -> {
                try {
                    //noinspection ResultOfMethodCallIgnored
                    System.in.read(); // wait for any data
                } catch (Throwable t) {
                    t.printStackTrace();
                } finally {
                    System.exit(0);
                }
            }).start();
            if (args[1].isEmpty()) {
                throw new RuntimeException("MemProcFS.exe location not specified. Please check settings and try again.");
            }
            File memProcFsExe = new File(args[1]);
            if (!memProcFsExe.exists()) {
                throw new FileNotFoundException("MemProcFS Path does not exist: \n\t'" + memProcFsExe + "'");
            }
            String parentPath = memProcFsExe.getParent();
            File adapterDll = new File(parentPath + "\\" + Settings.ADAPTER_DLL_NAME + ".dll");
            if (!adapterDll.exists()) {
                throw new FileNotFoundException("JNA Library Path does not exist: \n\t'" + adapterDll 
                + "'\n\trefer to README.md for more information.");
            }

            String leechArgs = args[2].trim();
            if(leechArgs.isEmpty()){
                throw new RuntimeException("Arguments is empty, the default args is '"+ Settings.DEFAULT_ARGUMENTS +"'");
            }

            List<String> pciLeechArgs = new ArrayList<>();
            pciLeechArgs.add("");
            pciLeechArgs.addAll(Arrays.asList(leechArgs.split(" +")));
            System.setProperty("jna.library.path", parentPath);

            Main.log("ServerMain", "Initializing PCILeech...");
            boolean result = PciLeech.initialize(pciLeechArgs.toArray(String[]::new));
            if(result == false) {
                throw new RuntimeException("Unable to initialize PCILeech.");
            }

            Main.log("ServerMain", "PCILeech Initialization Complete.");
            runServer(port);

        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            System.exit(-1);
        }
    }

    private static void runServer(int port) throws IOException {
        ServerSocketChannel ss = ServerSocketChannel.open();
        try {
            ss.bind(new InetSocketAddress(port));
        } catch (BindException ex) {
            throw new IOException("Unable to listen on port " + port, ex);
        }
        Main.log("ServerMain", "Server running on port " + port + "...");
        //noinspection InfiniteLoopStatement
        while (true) {
            SocketChannel socketChannel = ss.accept();
            ClientHandler clientHandler = new ClientHandler(socketChannel);
            clientHandler.start();
        }
    }

}
