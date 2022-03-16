package com.alteredmechanism.beanshell;

import static com.sun.jna.platform.win32.Advapi32Util.registryGetStringValue;
import static com.sun.jna.platform.win32.Advapi32Util.registrySetStringValue;
import static com.sun.jna.platform.win32.Advapi32Util.registryGetExpandableStringValue;
import static com.sun.jna.platform.win32.Advapi32Util.registrySetExpandableStringValue;
import static com.sun.jna.platform.win32.WinReg.HKEY_LOCAL_MACHINE;
import static com.sun.jna.platform.win32.WinDef.LPARAM;
import static com.sun.jna.platform.win32.WinDef.WPARAM;
import static com.sun.jna.platform.win32.WTypes.LPSTR;
import com.sun.jna.platform.win32.User32;
import static com.sun.jna.platform.win32.WinUser.HWND_BROADCAST;
import com.sun.jna.Pointer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;

public class PathUpdater {
	public static final int WM_WININICHANGE = 0x001A;
	public static final int WM_SETTINGCHANGE = WM_WININICHANGE;
	
	public static final String SYSTEM_ENV_KEY_PATH = "SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Environment";
	public static final String PATHEXT = "PATHEXT";
	public static final String PATH = "PATH";
	public static final String PATH_SEPARATOR = System.getProperty("path.separator");

	/**
	 * Executions start here.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			if (args.length == 0) {
				System.err.println("A directory to add to the PATH is a required command line argument.");
				System.exit(1);
			}
			PathUpdater updater = new PathUpdater();
			for (String arg : args) {
				updater.addToSystemPath(arg);
				updater.addToSystemPathExt(".BSH");
				updater.broadcastPathUpdate();
			}
			System.out.println("Path update complete.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void broadcastPathUpdate() {
		final int TIMEOUT = 1000;
		LPSTR whatChanged = new LPSTR("Environment");
		Pointer p = (Pointer) whatChanged.toNative();
		long nativePointerValue = Pointer.nativeValue(p);
		LPARAM env = new LPARAM(nativePointerValue);
		User32.INSTANCE.SendMessageTimeout(HWND_BROADCAST, WM_SETTINGCHANGE, null, env, 0, TIMEOUT, null);
	}

	public void removeFromSystemPath(String pathEnvVarName, String elementValue) {
		removeFromPath(SYSTEM_ENV_KEY_PATH, pathEnvVarName, elementValue);
	}

	public void removeFromPath(String environmentKeyPath, String pathEnvVarName, String elementValue) {
		String pathEnvVarValue = registryGetExpandableStringValue(HKEY_LOCAL_MACHINE, environmentKeyPath, pathEnvVarName);
		// LinkedHashSet preserves insertion order
		LinkedHashSet<String> elements = new LinkedHashSet<String>(Arrays.asList(pathEnvVarValue.split(PATH_SEPARATOR)));
		if (elements.contains(elementValue)) {
			System.out.printf("Deleting %s from environment variable %s%n", elementValue, pathEnvVarName);
			elements.remove(elementValue);
			String pathEnvVarValueWithoutElem = String.join(PATH_SEPARATOR, elements);
			registrySetExpandableStringValue(HKEY_LOCAL_MACHINE, environmentKeyPath, pathEnvVarName, pathEnvVarValueWithoutElem);
		} else {
			System.out.printf("Environment variable %s does not contain %s%n", pathEnvVarName, elementValue);
		}
	}
	
	public void addToSystemPath(String elementValue) {
		addToPath(SYSTEM_ENV_KEY_PATH, elementValue);
	}

	public void addToPath(String environmentKeyPath, String elementValue) {
		String pathEnvVarValue = registryGetExpandableStringValue(HKEY_LOCAL_MACHINE, environmentKeyPath, PATH);
		HashSet<String> elements = new HashSet<String>(Arrays.asList(pathEnvVarValue.split(PATH_SEPARATOR)));
		if (elements.contains(elementValue)) {
			System.out.printf("Environment variable %s already contains %s%n", PATH, elementValue);
		} else {
			if (! pathEnvVarValue.endsWith(PATH_SEPARATOR)) {
				pathEnvVarValue += PATH_SEPARATOR;
			}
			pathEnvVarValue += elementValue;
			System.out.printf("Adding %s to environment variable %s%n", elementValue, PATH);
			registrySetExpandableStringValue(HKEY_LOCAL_MACHINE, environmentKeyPath, PATH, pathEnvVarValue);
		}
	}

	public void addToSystemPathExt(String elementValue) {
		addToPathExt(SYSTEM_ENV_KEY_PATH, elementValue);
	}

	public void addToPathExt(String environmentKeyPath, String elementValue) {
		String pathEnvVarValue = registryGetStringValue(HKEY_LOCAL_MACHINE, environmentKeyPath, PATHEXT);
		HashSet<String> elements = new HashSet<String>(Arrays.asList(pathEnvVarValue.split(PATH_SEPARATOR)));
		if (elements.contains(elementValue)) {
			System.out.printf("Environment variable %s already contains %s%n", PATHEXT, elementValue);
		} else {
			if (! pathEnvVarValue.endsWith(PATH_SEPARATOR)) {
				pathEnvVarValue += PATH_SEPARATOR;
			}
			pathEnvVarValue += elementValue;
			System.out.printf("Adding %s to environment variable %s%n", elementValue, PATHEXT);
			registrySetStringValue(HKEY_LOCAL_MACHINE, environmentKeyPath, PATHEXT, pathEnvVarValue);
		}
	}

}
