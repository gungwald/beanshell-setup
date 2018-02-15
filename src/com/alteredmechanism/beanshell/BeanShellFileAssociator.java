package com.alteredmechanism.beanshell;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

// TODO - Remove references to this jar
import ca.beq.util.win32.registry.RegistryKey;
import ca.beq.util.win32.registry.RegistryValue;
import ca.beq.util.win32.registry.RootKey;

public class BeanShellFileAssociator {

    public static final NativeLong SHCNE_ASSOCCHANGED = new NativeLong(0x8000000);
    public static final int SHCNF_IDLIST = 0;

    public static final String BEANSHELL_FILE_EXTENSION = ".bsh";
    
    public static final String BEANSHELL_PROG_ID = "BeanShell.Script.2";
    public static final String BEANSHELL_FRIENDLY_TYPE_NAME = "BeanShell Script";
    public static final String BEANSHELL_INFO_TIP = "BeanShell is a scripting language for the Java platform.";
    public static final String BEANSHELL_COMMAND_ACTION_TEXT = "Run BeanShell Script";
    public static final String BEANSHELL_CONTENT_TYPE = "text/plain";

    public String installDir = "C:\\Program Files\\BeanShell";
    public String defaultIcon = installDir + "\\icons\\beany.ico";
    public String command = installDir + "\\bin\\beanshell.bat %1";

    private RegistryKey progIDKey = null;
    private RegistryKey extKey = null;

    /**
     * Executions start here.
     * 
     * @param args
     */
    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                System.err.println("The install directory is a required command line argument.");
                System.exit(1);
            }
            System.out.println("Setting up file association.");
            BeanShellFileAssociator associator = new BeanShellFileAssociator(args[0]);
            associator.disassociate();
            associator.associate();
            System.out.println("File association setup complete.");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface Shell32 extends Library {
        public void SHChangeNotify(NativeLong event, int flags, Pointer item1, Pointer item2);
    }

    public BeanShellFileAssociator(String installDirArg) {
        if (installDir != null) {
            installDir = installDirArg;
        }
        String nativeLibPath = installDir + "\\lib\\jRegistryKey.dll";
        System.out.println("Initializing native library: " + nativeLibPath);
        RegistryKey.initialize(nativeLibPath);
        progIDKey = new RegistryKey(RootKey.HKEY_CLASSES_ROOT, BEANSHELL_PROG_ID);
        extKey = new RegistryKey(RootKey.HKEY_CLASSES_ROOT, ".bsh");

    }

    public void disassociate() {
        for (RegistryKey key : new RegistryKey[] { progIDKey, extKey }) {
            if (key.exists()) {
                System.out.println("Deleting existing key: " + key.toString());
                key.delete();
            }
        }
    }
    
    public void disassociate2() {
    	// These should throw a Win32Exception type.
    	// TODO - Determine if deletion of subkeys is needed
    	Advapi32Util.registryDeleteKey(WinReg.HKEY_CLASSES_ROOT, BEANSHELL_PROG_ID);
   		Advapi32Util.registryDeleteKey(WinReg.HKEY_CLASSES_ROOT, BEANSHELL_FILE_EXTENSION);
    }

    public void notifySystemOfAssociationChange() {
        System.out.println("Notifying system of file association change.");
        Shell32 shell = (Shell32) Native.loadLibrary("Shell32", Shell32.class);
        shell.SHChangeNotify(SHCNE_ASSOCCHANGED, SHCNF_IDLIST, Pointer.NULL, Pointer.NULL);
        System.out.println("Notification complete.");
    }

    public void associate2() {
    	// TODO - Break the below into multiple methods
    	Advapi32Util.registryCreateKey(WinReg.HKEY_CLASSES_ROOT, BEANSHELL_PROG_ID);
    	Advapi32Util.registrySetStringValue(WinReg.HKEY_CLASSES_ROOT, BEANSHELL_PROG_ID, BEANSHELL_FRIENDLY_TYPE_NAME);
    	Advapi32Util.registrySetStringValue(WinReg.HKEY_CLASSES_ROOT, BEANSHELL_PROG_ID + "\\FriendlyTypeName", BEANSHELL_FRIENDLY_TYPE_NAME);
    	Advapi32Util.registrySetStringValue(WinReg.HKEY_CLASSES_ROOT, BEANSHELL_PROG_ID + "\\InfoTip", BEANSHELL_INFO_TIP);
    	Advapi32Util.registrySetStringValue(WinReg.HKEY_CLASSES_ROOT, BEANSHELL_PROG_ID + "\\DefaultIcon", defaultIcon);
    	Advapi32Util.registryCreateKey(WinReg.HKEY_CLASSES_ROOT, BEANSHELL_PROG_ID, "shell");
    	Advapi32Util.registryCreateKey(WinReg.HKEY_CLASSES_ROOT, BEANSHELL_PROG_ID, "shell\\" + BEANSHELL_COMMAND_ACTION_TEXT);
    	Advapi32Util.registrySetStringValue(WinReg.HKEY_CLASSES_ROOT, BEANSHELL_PROG_ID + "\\shell\\" + BEANSHELL_COMMAND_ACTION_TEXT, "command", command);
    	// TODO - Implement creation of extension keys/values
    }
    	
    public void associate() {
        System.out.println("Creating registry key: " + progIDKey.toString());
        progIDKey.create();
        RegistryValue friendlyTypeValue = new RegistryValue(BEANSHELL_FRIENDLY_TYPE_NAME);
        progIDKey.setValue(friendlyTypeValue);

        progIDKey.createSubkey("FriendlyTypeName").setValue(new RegistryValue(BEANSHELL_FRIENDLY_TYPE_NAME));
        progIDKey.createSubkey("InfoTip").setValue(new RegistryValue(BEANSHELL_INFO_TIP));
        progIDKey.createSubkey("DefaultIcon").setValue(new RegistryValue(defaultIcon));
        RegistryKey shellKey = progIDKey.createSubkey("shell");
        RegistryKey actionTextKey = shellKey.createSubkey(BEANSHELL_COMMAND_ACTION_TEXT);
        actionTextKey.createSubkey("command").setValue(new RegistryValue(command));

        System.out.println("Creating registry key: " + extKey.toString());
        extKey.create();
        extKey.setValue(new RegistryValue(BEANSHELL_PROG_ID));
        extKey.createSubkey("Content Type").setValue(new RegistryValue(BEANSHELL_CONTENT_TYPE));
        System.out.println("Done setting registry keys");

        notifySystemOfAssociationChange();
    }

}
