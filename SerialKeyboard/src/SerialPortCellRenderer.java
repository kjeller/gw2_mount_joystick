import com.fazecast.jSerialComm.SerialPort;

import javax.swing.*;
import java.awt.*;

/**
 * Since fazecast.SerialPort toString() defaults to getPortDescription() and not getSystemPortName(), which is
 * not useful to have in a dropdown menu, and since SerialPort is declared as final,
 * I can't just override toString(). Therefore this class is necessary.
 * **/
public class SerialPortCellRenderer extends JLabel implements ListCellRenderer<SerialPort> {

    @Override
    public Component getListCellRendererComponent(JList<? extends SerialPort> list,
                                                  SerialPort value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {

        setText(value.getSystemPortName());
        return this;
    }
}
