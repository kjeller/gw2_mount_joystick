import com.fazecast.jSerialComm.*;

import javax.swing.*;
import java.awt.*;

public class SerialKeyboard {

    public static final String SERIAL_PANEL = "Serial";
    public static final String KEYMAPPING_PANEL = "Keymappings";

    public SerialPort serialPort; // serialport that can be selected from dropdown
    public int baudrate = 115200;

    /**
     * Creates and shows GUI.
     * Invoke this from event dispatch thread for thread safety.
     */
    private static void initGUI() {
        JFrame f = new JFrame("SerialKeyboard");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        SerialKeyboard sk = new SerialKeyboard();
        sk.addComponentstoPane(f.getContentPane());

        f.pack();
        f.setVisible(true);
    }

    private void addComponentstoPane(Container pane) {
        JTabbedPane tabs = new JTabbedPane();

        // Create the "card" settings
        JPanel card1 = new JPanel(); // For serial settings

        // Uneditable dropdown list of selectable com ports
        JComboBox<SerialPort> portDropdown = new JComboBox<>(SerialPort.getCommPorts());
        portDropdown.setRenderer(new SerialPortCellRenderer());
        portDropdown.setSelectedIndex(0); // default selected
        serialPort = (SerialPort) portDropdown.getSelectedItem(); // prevents null error when trying to connect later
        portDropdown.addActionListener(e -> {
            serialPort = (SerialPort) portDropdown.getSelectedItem();
            System.out.printf("Selected serialport %s\n", serialPort.getSystemPortName());
        });
        card1.add(portDropdown);

        JButton connectButton = new JButton("Connect");
        connectButton.addActionListener(e -> {
            if(!openSerialPort()) {
                JOptionPane.showMessageDialog(new JFrame(),
                        String.format("Could not connect to \"%s\"",
                                serialPort.getSystemPortName(),
                                JOptionPane.ERROR_MESSAGE));
            } else {
                System.out.println("Connected!");
            }
        });
        card1.add(connectButton);

        JPanel card2 = new JPanel(); // For keymapping
        card2.add(new TextField("TextField", 20));
        // Add fields for all keymappings that can be made

        tabs.addTab(SERIAL_PANEL, card1);
        tabs.addTab(KEYMAPPING_PANEL, card2);

        pane.add(tabs);
    }

    /**
     * Tries to open selected serial port
     * @return
     */
    boolean openSerialPort() {
        if(serialPort == null)
            return false;

        serialPort.setComPortParameters(115200, 8, 1, 0); // Suggestion: baudrate could be configured?
        if(!serialPort.openPort()) {
            System.err.printf("Could not open %s\n", serialPort);
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        // Let special EDT create UI
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                initGUI();
            }
        });
    }
}
