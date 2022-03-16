import com.alteredmechanism.win32.Wtsapi32;

import bsh.CallStack;
import bsh.Interpreter;

import com.sun.jna.platform.win32.BaseTSD.DWORD_PTR;

public class notifyUser {

    public static void invoke(Interpreter env, CallStack stack, String message) {
        String title = "Message from " + System.getProperty("user.name");
        DWORD_PTR response = new DWORD_PTR();
        
        Wtsapi32.INSTANCE.WTSSendMessage(
                Wtsapi32.WTS_CURRENT_SERVER_HANDLE,
                Wtsapi32.WTS_CURRENT_SESSION,
                title, 
                title.length(), 
                message, 
                message.length(),
                0,
                0,
                response,
                false);
    }

}

