package com.alteredmechanism.beanshell;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

import static com.sun.jna.platform.win32.Advapi32Util.registryCreateKey;
import static com.sun.jna.platform.win32.Advapi32Util.registryDeleteKey;
import static com.sun.jna.platform.win32.Advapi32Util.registryKeyExists;
import static com.sun.jna.platform.win32.Advapi32Util.registrySetStringValue;
import static com.sun.jna.platform.win32.Advapi32Util.registrySetExpandableStringValue;
import static com.sun.jna.platform.win32.WinReg.HKEY_CLASSES_ROOT;

public class BeanShellFileAssociator {

	public static final NativeLong SHCNE_ASSOCCHANGED = new NativeLong(0x8000000);
	public static final int SHCNF_IDLIST = 0;

	public static final String BEANSHELL_FILE_EXTENSION = ".bsh";

	public static final String BEANSHELL_PROG_ID = "BeanShell.Script.2";
	public static final String BEANSHELL_FRIENDLY_TYPE_NAME = "BeanShell Script";
	public static final String BEANSHELL_INFO_TIP = "BeanShell is a scripting language for the Java platform.";
	public static final String BEANSHELL_COMMAND_ACTION_TEXT = "Run";
	public static final String BEANSHELL_CONTENT_TYPE = "text/plain";

	public String installDir;
	public String defaultIcon;
	public String command;

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
			System.out.println("Setting up file association for .bsh extension.");
			BeanShellFileAssociator associator = new BeanShellFileAssociator(args[0]);
			associator.disassociate();
			associator.associate();
			associator.notifySystemOfAssociationChange();
			System.out.println("File association setup complete.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public interface Shell32 extends Library {
		public void SHChangeNotify(NativeLong event, int flags, Pointer item1, Pointer item2);
	}

	public BeanShellFileAssociator(String installDirArg) {
		installDir = installDirArg;
		defaultIcon = installDir + "\\icons\\beany.ico";
		command = "\"" + installDir + "\\bin\\beanshell.bat\" %1";
	}

	public void disassociate() {
		// TODO - Determine if deletion of subkeys is needed
		String[] keys = new String[] { BEANSHELL_PROG_ID + "\\DefaultIcon", BEANSHELL_PROG_ID, BEANSHELL_FILE_EXTENSION };
		for (String key : keys) {
			if (registryKeyExists(HKEY_CLASSES_ROOT, key)) {
				System.out.printf("Deleting key HKEY_CLASSES_ROOT\\%s%n", key);
				registryDeleteKey(HKEY_CLASSES_ROOT, key);
			}
		}
	}

	public void notifySystemOfAssociationChange() {
		System.out.println("Notifying system of file association change.");
		Shell32 shell = (Shell32) Native.load(Shell32.class);
		shell.SHChangeNotify(SHCNE_ASSOCCHANGED, SHCNF_IDLIST, Pointer.NULL, Pointer.NULL);
		System.out.println("Notification complete.");
	}

	public void associate() {
		defineBeanShellProgramType();
		defineBeanShellCommand();
		createBshFileExtensionAssociation();
	}

	public void defineBeanShellProgramType() {
		registryCreateKey(HKEY_CLASSES_ROOT, BEANSHELL_PROG_ID);
		registrySetStringValue(HKEY_CLASSES_ROOT, BEANSHELL_PROG_ID, null, BEANSHELL_FRIENDLY_TYPE_NAME);
		registrySetExpandableStringValue(HKEY_CLASSES_ROOT, BEANSHELL_PROG_ID, "FriendlyTypeName", BEANSHELL_FRIENDLY_TYPE_NAME);
		registrySetStringValue(HKEY_CLASSES_ROOT, BEANSHELL_PROG_ID, "InfoTip", BEANSHELL_INFO_TIP);
		registryCreateKey(HKEY_CLASSES_ROOT, BEANSHELL_PROG_ID, "DefaultIcon");
		registrySetExpandableStringValue(HKEY_CLASSES_ROOT, BEANSHELL_PROG_ID + "\\DefaultIcon", null, defaultIcon);
	}

	public void defineBeanShellCommand() {
		registryCreateKey(HKEY_CLASSES_ROOT, BEANSHELL_PROG_ID, "shell");
		registryCreateKey(HKEY_CLASSES_ROOT, BEANSHELL_PROG_ID + "\\shell", BEANSHELL_COMMAND_ACTION_TEXT);
		registrySetStringValue(HKEY_CLASSES_ROOT, BEANSHELL_PROG_ID + "\\shell\\" + BEANSHELL_COMMAND_ACTION_TEXT, "command", command);
	}

	public void createBshFileExtensionAssociation() {
		registryCreateKey(HKEY_CLASSES_ROOT, BEANSHELL_FILE_EXTENSION);
		registrySetStringValue(HKEY_CLASSES_ROOT, BEANSHELL_FILE_EXTENSION, null, BEANSHELL_PROG_ID);
		registrySetStringValue(HKEY_CLASSES_ROOT, BEANSHELL_FILE_EXTENSION, "Content Type", BEANSHELL_CONTENT_TYPE);
		registrySetStringValue(HKEY_CLASSES_ROOT, BEANSHELL_FILE_EXTENSION, "PerceivedType", "text");
	}
}
