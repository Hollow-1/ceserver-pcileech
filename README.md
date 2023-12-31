# ceserver-pcileech
A modified version of ceserver-pcileech , added some features...
This project is based on ceserver-pcileech by Isabella Flores, I have added the following features:
- [Support MemProcFS v5.8.1]
- [Support Cheat Engine v7.4]
- [Support Vmware]

# How to use?
1. Install [ceserver-pcileech-1.5.msi], Cheat Engine v7.4, and download MemProcFS v5.8.1
2. Copy [ceserver_adapter.dll] to your MemProcFS folder.(THIS IS IMPORTANT)
3. Continue with "Running the Server" section below

# How to build?
* The precompiled executable file is already provided, but you can also build it yourself.

1. Install jdk-17.0.1, apache-maven-3.8.4, WiX Toolset v3.11  (Other versions should work as well, but I haven't tested).
2. Edit the [scripts\env.cmd] file and set the environment.
3. Run [scripts\make-exe.cmd] to build jar and msi, the first build will take some time to download resoureces.
4. If everything is ok, you will find the [ceserver-pcileech-1.5.msi] file in the [target\jpackage-out] folder.

The following is the original project description. repository had deleted by the way.

# ceserver-pcileech

Ceserver-pcileech allows using Cheat Engine against a remote machine, without the need to install ANY software on that
remote machine. It was developed independently from the Cheat Engine software by DarkByte and PCILeech by Ulf Frisk, and
is not affiliated with either.

All Cheat Engine functions may not be available. Currently implemented is the ability to:

* Connect to a Process
* Read Memory
* Write Memory
* Search Memory
* Browse Memory
* View Module Listing
* Generate Pointer Map
* Pointer Scan

Other functions may or may not work (likely the latter).

# Terminology

* "Source": The machine running Cheat Engine and PCILeech.
* "Target": The machine running the process to be inspected/altered.

# Prerequisites

* Two machines running Windows
* [MemProcFS](https://github.com/ufrisk/MemProcFS) running on the source machine (part of the PCILeech ecosystem by Ulf
  Frisk)
* Additional requirements, including possibly the purchase of a hardware FPGA card if you choose to go that route. See
  the [PCILeech documentation](https://github.com/ufrisk/pcileech/blob/master/readme.md) for your particular use case.

# Installation

1. Download [the latest ceserver-pcileech.msi](https://github.com/isabellaflores/ceserver-pcileech/releases) from Github
2. Run the installation package, and follow the instructions
3. Continue with "Running the Server" section below

# Running the Server

1. Double-click the ceserver-pcileech icon created by the installer
4. Configure the server in the window that appears
5. Press the "Start Server" button
6. The server will now be listening on the default port, 52736

# Connecting to the server

1. Open Cheat Engine
2. File -> Open Process
3. Click 'Network'
4. Type 'localhost' in the 'Host' field
5. Click 'Connect' and select a process to open

# Contributing to ceserver-pcileech

Thank you for your interest in contributing to ceserver-pcileech!

To submit your changes to me, please [create a pull request](https://github.com/isabellaflores/ceserver-pcileech/pulls),
and I will personally review your submission. If it is accepted, you will receive credit for your submission. If you'd
like your submission to be anonymous or pseudonymous, please let me know.

You can also email me at [isabella99flores@gmail.com](mailto:isabella99flores@gmail.com)
