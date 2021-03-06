import com.fazecast.jSerialComm.SerialPort;

import java.awt.event.KeyEvent;
import java.io.InputStream;
import java.util.ArrayList;

public class SerialDecodeThread extends Thread {
    private final InputStream is;
    private final ArrayList<KeyCommand> keyList;

    public SerialDecodeThread(SerialPort sp, ArrayList<KeyCommand> keyList) {
        this.is = sp.getInputStream();
        this.keyList = keyList;
    }

    public void start() {
        //...
        super.start();
    }

    public void run() {
        try {
            while(true) {
                if (is.available() > 0) {
                    char k = (char)is.read();
                    System.out.println(k);
                    decodeMsg(k);
                }
            }
        } catch (Exception e) {}
    }

    public void decodeMsg(char k) {
        int key = Integer.parseInt(String.valueOf(k))-1; // key index
        KeyCommand keyCmd = keyList.get(key);

        // Make sure key is set
        if(keyCmd == null || !(key > -1 && key < SerialKeyboardGUI.TOTAL_SLOTS)) { return; }

        //.. otherwise generate press/release event
        int keyCode = keyCmd.getExtendedKeyCode();
        int modifiers = keyCmd.getModifiersEx();
        System.out.printf("Virtual code: %d\n", keyCode);
        Keyboard.sendKeyInput(keyCode, modifiers);
        System.out.println("keyboard input sent");
    }
}
