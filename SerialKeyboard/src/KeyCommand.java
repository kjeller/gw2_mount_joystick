import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.Serializable;

/**
 * This class is a serializable representation of a KeyEvent. It is needed since KeyEvent contains references to
 * non-serializable objects (e.g. SerialPort).
 *
 * @author Karl Strålman
 */
public class KeyCommand implements Serializable {
    private final int keyCode;
    private final char keyChar;
    private final int modifiers;
    private final Object source;
    private final int id;
    private final long when;

    public KeyCommand(KeyEvent ke) throws NullPointerException {
        keyCode = ke.getExtendedKeyCode();
        modifiers = ke.getModifiersEx();
        source = ke.getSource();
        id = ke.getID();
        when = ke.getWhen();
        keyChar = ke.getKeyChar();
    }

    public KeyEvent getKeyEvent() {
        return new KeyEvent((Component) source, id, when, modifiers, keyCode, keyChar);
    }

    public int getExtendedKeyCode() { return keyCode; }
    public int getModifiersEx() { return modifiers; }
    public Object getSource() { return source; }
    public int getID() { return id; }
}
