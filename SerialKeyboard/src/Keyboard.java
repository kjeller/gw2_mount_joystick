import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.INPUT;

import static com.sun.jna.platform.win32.WinUser.KEYBDINPUT.*;
import static java.awt.event.InputEvent.*;
import static java.awt.event.KeyEvent.*;

/**
 * Class used for generating low-level keyboard inputs in Windows
 */
public class Keyboard {

    /**
     * Returns virtual code of first modifier found in modifier mask
     * @return VC of modifier
     */
    private static int maskToVC(int modifier) {
        if(modifier == ALT_DOWN_MASK) { return VK_ALT; }
        if(modifier == CTRL_DOWN_MASK) { return VK_CONTROL; }
        if(modifier == SHIFT_DOWN_MASK) { return VK_SHIFT; }
        return 0;
    }

    //TODO Add method to support more than one modifier at a time

    /**
     * Sends a keyboard input (down and up) for the given character value (virtual keycode and modifiers).
     * @param code virtual keycode that will be translated to scancode
     */
    public static void sendKeyInput(int code, int modifiers) {
        //Override virtual code with (hardware) scan code based on keyboard layout
        code = User32.INSTANCE.MapVirtualKeyEx(code,
                User32.MAPVK_VK_TO_VSC_EX,
                User32.INSTANCE.GetKeyboardLayout(0));

        modifiers = User32.INSTANCE.MapVirtualKeyEx(maskToVC(modifiers),
                User32.MAPVK_VK_TO_VSC_EX,
                User32.INSTANCE.GetKeyboardLayout(0));

        sendScanCodeEvent(code, modifiers);
    }

    /**
     * Sends a keyboard scan event (up and down in succession)
     * @param keyScanCode the scancode of a key that will be sent
     */
    private static void sendScanCodeEvent (int keyScanCode) {
        INPUT input = new INPUT();
        // Common properties
        input.input.setType("ki");
        input.type = new WinDef.DWORD(WinUser.INPUT.INPUT_KEYBOARD);
        input.input.ki.time = new WinDef.DWORD(0);
        input.input.ki.wVk = new WinDef.WORD(0);

        // Not really needed. Can be used to identify generated keyboard inputs
        input.input.ki.dwExtraInfo = new BaseTSD.ULONG_PTR(0xBAADC0FE);

        // "keyDown": Key
        input.input.ki.wScan = new WinDef.WORD(keyScanCode);
        input.input.ki.dwFlags = new WinDef.DWORD(KEYEVENTF_SCANCODE); // default key down
        User32.INSTANCE.SendInput(new WinDef.DWORD(1), (WinUser.INPUT[]) input.toArray(1), input.size());

        // "keyUp" : Key
        input.input.ki.dwFlags = new WinDef.DWORD(KEYEVENTF_SCANCODE | KEYEVENTF_KEYUP);
        User32.INSTANCE.SendInput(new WinDef.DWORD(1), (WinUser.INPUT[]) input.toArray(1), input.size());
    }

    /**
     * Sends a keyboard scan event (up and down in succession)
     * @param keyScanCode the scancode of a key that will be sent
     * @param modifierScanCode the modifier scanCode that will be sent (0 when no modifiers are used)
     */
    private static void sendScanCodeEvent (int keyScanCode, int modifierScanCode) {
        INPUT input = new INPUT();
        // Common properties
        input.input.setType("ki");
        input.type = new WinDef.DWORD(WinUser.INPUT.INPUT_KEYBOARD);
        input.input.ki.time = new WinDef.DWORD(0);
        input.input.ki.wVk = new WinDef.WORD(0);

        // Not really needed. Can be used to identify generated keyboard inputs
        input.input.ki.dwExtraInfo = new BaseTSD.ULONG_PTR(0xBAADC0FE);

        // "keyDown": Modifier
        input.input.ki.wScan = new WinDef.WORD(modifierScanCode);
        input.input.ki.dwFlags = new WinDef.DWORD(KEYEVENTF_SCANCODE); // default key down
        User32.INSTANCE.SendInput(new WinDef.DWORD(1), (WinUser.INPUT[]) input.toArray(1), input.size());

        // "keyDown": Key
        input.input.ki.wScan = new WinDef.WORD(keyScanCode);
        input.input.ki.dwFlags = new WinDef.DWORD(KEYEVENTF_SCANCODE); // default key down
        User32.INSTANCE.SendInput(new WinDef.DWORD(1), (WinUser.INPUT[]) input.toArray(1), input.size());

        // "keyUp" : Key
        input.input.ki.dwFlags = new WinDef.DWORD(KEYEVENTF_SCANCODE | KEYEVENTF_KEYUP);
        User32.INSTANCE.SendInput(new WinDef.DWORD(1), (WinUser.INPUT[]) input.toArray(1), input.size());

        // "keyUp": Modifier
        input.input.ki.wScan = new WinDef.WORD(modifierScanCode);
        input.input.ki.dwFlags = new WinDef.DWORD(KEYEVENTF_SCANCODE | KEYEVENTF_KEYUP);
        User32.INSTANCE.SendInput(new WinDef.DWORD(1), (WinUser.INPUT[]) input.toArray(1), input.size());
    }
}
