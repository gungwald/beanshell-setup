import com.sun.jna.platform.win32.Shell32Util;
import com.sun.jna.platform.win32.ShlObj;

import bsh.CallStack;
import bsh.Interpreter;


public class getAllUsersStartupFolder {

    public static String invoke(Interpreter env, CallStack stack, String message) {
        return Shell32Util.getFolderPath(ShlObj.CSIDL_COMMON_STARTUP);
    }
}
