import com.fazecast.jSerialComm.SerialPort;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.InputStream;
import java.util.ArrayList;

public class SerialThread extends Thread {
    private SerialPort sp;
    private InputStream is;
    private Robot r;
    private ArrayList<KeyEvent> keyList;

    public SerialThread(SerialPort sp, ArrayList<KeyEvent> keyList) {
        this.sp = sp;
        this.is = sp.getInputStream();
        this.keyList = keyList;
    }

    public void start() {
        try {
            r = new Robot();
            r.setAutoDelay(40);
            r.setAutoWaitForIdle(true);
            System.out.println("Robot started");
        } catch (AWTException e) {
            e.printStackTrace();
        }
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
        int key = Integer.parseInt(String.valueOf(k))-1;
        // Make sure key is set
        if(keyList.get(key) == null && key > -1 && key < SerialKeyboard.TOTAL_SLOTS) { return; }

        //.. otherwise generate press/release
        int keyCode = keyList.get(key).getKeyCode();
        r.keyPress(keyCode);
        r.keyRelease(keyCode);
    }
}
