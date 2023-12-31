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

public interface CommandConstants {

    byte CMD_GETVERSION = 0;

    //==========Unused cmds
    byte CMD_CLOSECONNECTION = 1;
    byte CMD_TERMINATESERVER = 2;
    //==========End

    byte CMD_OPENPROCESS = 3;
    byte CMD_CREATETOOLHELP32SNAPSHOT = 4;
    byte CMD_PROCESS32FIRST = 5;
    byte CMD_PROCESS32NEXT = 6;
    byte CMD_CLOSEHANDLE = 7;
    byte CMD_VIRTUALQUERYEX = 8;
    byte CMD_READPROCESSMEMORY = 9;
    byte CMD_WRITEPROCESSMEMORY = 10;

    //==========Unused cmds
    byte CMD_STARTDEBUG = 11;
    byte CMD_STOPDEBUG = 12;
    byte CMD_WAITFORDEBUGEVENT = 13;
    byte CMD_CONTINUEFROMDEBUGEVENT = 14;
    byte CMD_SETBREAKPOINT = 15;
    byte CMD_REMOVEBREAKPOINT = 16;
    byte CMD_SUSPENDTHREAD = 17;
    byte CMD_RESUMETHREAD = 18;
    byte CMD_GETTHREADCONTEXT = 19;
    byte CMD_SETTHREADCONTEXT = 20;
    //==========End

    byte CMD_GETARCHITECTURE = 21;
    byte CMD_MODULE32FIRST = 22;
    byte CMD_MODULE32NEXT = 23;

    byte CMD_GETSYMBOLLISTFROMFILE = 24;

    //==========Unused cmds
    byte CMD_LOADEXTENSION = 25;

    byte CMD_ALLOC = 26;
    byte CMD_FREE = 27;
    byte CMD_CREATETHREAD = 28;
    byte CMD_LOADMODULE = 29;
    byte CMD_SPEEDHACK_SETSPEED = 30;
    //==========End

    byte CMD_VIRTUALQUERYEXFULL = 31;
    byte CMD_GETREGIONINFO = 32;
    byte CMD_GETABI = 33;

    //==========Unused cmds
    byte CMD_AOBSCAN = (byte) 200;
    byte CMD_COMMANDLIST2 = (byte) 255;
    //==========End
    
    static String cmdToString(byte cmd) {
        switch(cmd) {
            case CMD_GETVERSION:
                return "CMD_GETVERSION";
            case CMD_CLOSECONNECTION:
                return "CMD_CLOSECONNECTION";
            case CMD_TERMINATESERVER:
                return "CMD_TERMINATESERVER";
            case CMD_OPENPROCESS:
                return "CMD_OPENPROCESS";
            case CMD_CREATETOOLHELP32SNAPSHOT:
                return "CMD_CREATETOOLHELP32SNAPSHOT";
            case CMD_PROCESS32FIRST:
                return "CMD_PROCESS32FIRST";

            case CMD_PROCESS32NEXT:
                return "CMD_PROCESS32NEXT";
            case CMD_CLOSEHANDLE:
                return "CMD_CLOSEHANDLE";
            case CMD_VIRTUALQUERYEX:
                return "CMD_VIRTUALQUERYEX";
            case CMD_READPROCESSMEMORY:
                return "CMD_READPROCESSMEMORY";
            case CMD_WRITEPROCESSMEMORY:
                return "CMD_WRITEPROCESSMEMORY";

            case CMD_STARTDEBUG:
                return "CMD_STARTDEBUG";
            case CMD_STOPDEBUG:
                return "CMD_STOPDEBUG";
            case CMD_WAITFORDEBUGEVENT:
                return "CMD_WAITFORDEBUGEVENT";
            case CMD_CONTINUEFROMDEBUGEVENT:
                return "CMD_CONTINUEFROMDEBUGEVENT";
            case CMD_SETBREAKPOINT:
                return "CMD_SETBREAKPOINT";

            case CMD_REMOVEBREAKPOINT:
                return "CMD_REMOVEBREAKPOINT";
            case CMD_SUSPENDTHREAD:
                return "CMD_SUSPENDTHREAD";
            case CMD_RESUMETHREAD:
                return "CMD_RESUMETHREAD";
            case CMD_GETTHREADCONTEXT:
                return "CMD_GETTHREADCONTEXT";
            case CMD_SETTHREADCONTEXT:
                return "CMD_SETTHREADCONTEXT";

            case CMD_GETARCHITECTURE:
                return "CMD_GETARCHITECTURE";
            case CMD_MODULE32FIRST:
                return "CMD_MODULE32FIRST";
            case CMD_MODULE32NEXT:
                return "CMD_MODULE32NEXT";
            case CMD_GETSYMBOLLISTFROMFILE:
                return "CMD_GETSYMBOLLISTFROMFILE";
            case CMD_LOADEXTENSION:
                return "CMD_LOADEXTENSION";

            case CMD_ALLOC:
                return "CMD_ALLOC";
            case CMD_FREE:
                return "CMD_FREE";
            case CMD_CREATETHREAD:
                return "CMD_CREATETHREAD";
            case CMD_LOADMODULE:
                return "CMD_LOADMODULE";
            case CMD_SPEEDHACK_SETSPEED:
                return "CMD_SPEEDHACK_SETSPEED";
            case CMD_VIRTUALQUERYEXFULL:
                return "CMD_VIRTUALQUERYEXFULL";
            case CMD_GETREGIONINFO:
                return "CMD_GETREGIONINFO";
            case CMD_GETABI:
                return "CMD_GETABI";
            case CMD_AOBSCAN:
                return "CMD_AOBSCAN";
            case CMD_COMMANDLIST2:
                return "CMD_COMMANDLIST2";
    
            default:
                Main.log("cmdToString","Undefined command: "+cmd);
                return "";
        }

    }
}

