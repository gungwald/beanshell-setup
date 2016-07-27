package com.alteredmechanism.beanshell;
import ca.beq.util.win32.registry.RegistryKey;
import ca.beq.util.win32.registry.RegistryValue;
import ca.beq.util.win32.registry.RootKey;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

public class BeanShellFileAssociator {

    public static final NativeLong SHCNE_ASSOCCHANGED = new NativeLong(0x8000000);
    public static final int SHCNF_IDLIST = 0;

    private String installDir = "C:\\Program Files\\BeanShell";
    private String progID = "BeanShell.Script.2";
    private String friendlyTypeName = "BeanShell Script";
    private String infoTip = "BeanShell is a scripting language for the Java platform.";
    private String defaultIcon = installDir + "\\icons\\beany.ico";
    private String command = installDir + "\\bin\\beanshell.bat %1";
    private String commandActionText = "Run BeanShell Script";
    private String contentType = "text/plain";

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
        progIDKey = new RegistryKey(RootKey.HKEY_CLASSES_ROOT, progID);
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

    public void notifySystemOfAssociationChange() {
        System.out.println("Notifying system of file association change.");
        Shell32 shell = (Shell32) Native.loadLibrary("Shell32", Shell32.class);
        shell.SHChangeNotify(SHCNE_ASSOCCHANGED, SHCNF_IDLIST, Pointer.NULL, Pointer.NULL);
        System.out.println("Notification complete.");
    }

    public void associate() {
        System.out.println("Creating registry key: " + progIDKey.toString());
        progIDKey.create();
        RegistryValue friendlyTypeValue = new RegistryValue(friendlyTypeName);
        progIDKey.setValue(friendlyTypeValue);

        progIDKey.createSubkey("FriendlyTypeName").setValue(new RegistryValue(friendlyTypeName));
        progIDKey.createSubkey("InfoTip").setValue(new RegistryValue(infoTip));
        progIDKey.createSubkey("DefaultIcon").setValue(new RegistryValue(defaultIcon));
        RegistryKey shellKey = progIDKey.createSubkey("shell");
        RegistryKey actionTextKey = shellKey.createSubkey(commandActionText);
        actionTextKey.createSubkey("command").setValue(new RegistryValue(command));

        System.out.println("Creating registry key: " + extKey.toString());
        extKey.create();
        extKey.setValue(new RegistryValue(progID));
        extKey.createSubkey("Content Type").setValue(new RegistryValue(contentType));
        System.out.println("Done setting registry keys");

        notifySystemOfAssociationChange();
    }

}
