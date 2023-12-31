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

import javax.swing.*;
import java.text.SimpleDateFormat;

public class Main {

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS");
    private static String lastMessage = "";
    private static int sameMessageCount = 0;

    public static void main(String[] args) {
        Settings settings = Settings.load();
        SwingUtilities.invokeLater(
                () -> {
                    MainFrame f = new MainFrame(settings);
                    f.setVisible(true);
                }
        );
    }


    public static void log(Object from, String message) {
        synchronized (System.out) {
            message = (from == null ? "---SYSTEM---" : from.toString()) 
                    + ": " + message;

            if(lastMessage.equals(message)) {
                System.out.print("+");
                sameMessageCount++;

                if((sameMessageCount % 81) == 0){
                    System.out.println();
                }
            } else {
                if(sameMessageCount >= 2) {
                    System.out.println(" " + sameMessageCount + " times.");
                }
                lastMessage = message;
                sameMessageCount = 1;

                System.out.println(
                        "["
                        + SIMPLE_DATE_FORMAT.format(System.currentTimeMillis())
                        + "] "
                        + message
                );
            }
        }
    }

    public static String getLastPathComponent(String path) {
        if (path == null) {
            return "";
        }
        int idx = path.lastIndexOf('\\');
        if (idx >= 0) {
            path = path.substring(idx + 1);
        }
        return path;
    }

}
