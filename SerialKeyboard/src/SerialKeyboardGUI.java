import com.fazecast.jSerialComm.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

/**
 * A serial keyboard used for decoding predefined serial signals
 * @author Karl Strålman
 */
public class SerialKeyboardGUI implements PropertyChangeListener  {

    public static final String SERIAL_PANEL = "Serial";
    public static final String KEYMAPPING_PANEL = "Keymappings";
    public static final String CONNECT = "Connect";
    public static final String DISCONNECT = "Disconnect";
    public static final int TOTAL_SLOTS = 8;

    private SerialDecodeThread thread; // reference to thread
    private SerialPort serialPort; // serialport that can be selected from dropdown
    private int baudrate = 115200;
    private boolean connected = false;

    // Keymap that maps slotFields to an event,
    // which is used in SerialThread to generate keypresses
    private ArrayList<KeyEvent> keyList;

    // Dropdown menus
    JComboBox<SerialPort> portDropdown;

    // Buttons
    private JButton connectButton;

    // Labels to identify fields
    private JLabel baudrateLabel;
    private JLabel[] slotLabel;

    // Fields for data entry
    private JFormattedTextField baudrateField;
    private JTextField[] slotField;

    private void addComponentstoPane(Container pane) {
        JTabbedPane tabs = new JTabbedPane();

        // Card for serial settings
        JPanel card1 = new JPanel(new GridBagLayout()); // For serial settings
        GridBagConstraints gbc1 = new GridBagConstraints();

        // Uneditable dropdown list of selectable com ports
        portDropdown = new JComboBox<>(SerialPort.getCommPorts());
        portDropdown.setRenderer(new SerialPortCellRenderer());
        portDropdown.setSelectedIndex(0); // default selected
        serialPort = (SerialPort) portDropdown.getSelectedItem(); // prevents null error when trying to connect later

        connectButton = new JButton(CONNECT);

        baudrateLabel = new JLabel("Baudrate:");
        baudrateField = new JFormattedTextField();
        baudrateField.setValue(baudrate);
        baudrateField.setColumns(10);
        baudrateField.addPropertyChangeListener(this);

        // Card for keymappings
        JPanel card2 = new JPanel(new GridBagLayout()); // For keymapping
        GridBagConstraints gbc2 = new GridBagConstraints();

        slotField = new JTextField[TOTAL_SLOTS];
        slotLabel = new JLabel[TOTAL_SLOTS];
        keyList = new ArrayList<>();

        // Add labels and fields for keymap slots
        for (int i = 0; i < TOTAL_SLOTS; i++) {

            // Create label for slot 1-8
            slotLabel[i] = new JLabel(String.format("Slot %d", i+1));

            // Create field for slot 1-8
            slotField[i] = new JTextField("Not set", 10);

            // KeyEvent is assigned in keyPressed() event
            keyList.add(null);

            slotField[i].setEditable(false);

            final int finalIndex = i;
            slotField[i].addKeyListener(new KeyAdapter() {
                private final int index = finalIndex;
                @Override
                public void keyPressed(KeyEvent e) {
                    JTextField textField = (JTextField) e.getSource();
                    String modifiers = KeyEvent.getModifiersExText(e.getModifiersEx());
                    String key = KeyEvent.getKeyText(e.getKeyCode());
                    String keyPlusModifier = !modifiers.isEmpty() ? modifiers + " + " + key : key;
                    textField.setText(keyPlusModifier);
                    keyList.set(index, e); // update keylist
                }
            });
        }

        // Add constraints to gridbag
        gbc1.gridx = 0;
        gbc1.gridy = 0;
        gbc1.insets = new Insets(4, 4, 4, 4);
        gbc1.anchor = GridBagConstraints.WEST;
        card1.add(portDropdown, gbc1); //0, 0
        gbc1.gridx = 1;
        card1.add(connectButton, gbc1); //1, 0
        gbc1.gridx = 0;
        gbc1.gridy = 1;
        gbc1.anchor = GridBagConstraints.NORTHWEST;
        card1.add(baudrateLabel, gbc1); // 0, 1
        gbc1.gridx = 1;
        card1.add(baudrateField, gbc1); // 1, 1

        gbc2.gridx = 0;
        gbc2.gridy = 0;
        gbc2.insets = new Insets(4, 4, 4, 4);
        gbc2.anchor = GridBagConstraints.WEST;

        for (int i = 0; i < TOTAL_SLOTS; i++) {
            gbc2.gridx = 0;
            card2.add(slotLabel[i], gbc2);
            gbc2.gridx = 1;
            card2.add(slotField[i], gbc2);
            gbc2.gridy++;
        }

        // Note: Maybe add serial monitor as its own tab

        addEventListeners();

        tabs.addTab(SERIAL_PANEL, card1);
        tabs.addTab(KEYMAPPING_PANEL, card2);

        pane.add(tabs);
    }

    public void addEventListeners() {
        portDropdown.addActionListener(e -> {
            serialPort = (SerialPort) portDropdown.getSelectedItem();
            System.out.printf("Selected serialport %s\n", serialPort.getSystemPortName());
        });

        connectButton.addActionListener(e -> {
            if(connected) {
                thread.interrupt();
                connectButton.setText(CONNECT);
                connected = false;
                return;
            }
            if(!openSerialPort()) {
                JOptionPane.showMessageDialog(new JFrame(),
                        String.format("Could not connect to \"%s\"",
                                serialPort.getSystemPortName(),
                                JOptionPane.ERROR_MESSAGE));
            } else {
                System.out.println("Connected!");
                connectButton.setText(DISCONNECT);
                thread = new SerialDecodeThread(serialPort, keyList);
                thread.start();
                connected = true;
            }
        });

    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Object source = evt.getSource();
        if(source == baudrateField) {
            baudrate = ((Number)baudrateField.getValue()).intValue();
            System.out.printf("Baud set to %d\n", baudrate);
        }
    }

    /**
     * Tries to open selected serial port
     * @return if serial port is open
     */
    boolean openSerialPort() {
        if(serialPort == null)
            return false;

        serialPort.setComPortParameters(baudrate, 8, 1, 0);
        if(!serialPort.openPort()) {
            System.err.printf("Could not open %s\n", serialPort);
            return false;
        }
        return true;
    }

    /**
     * Creates and shows GUI.
     * Invoke this from event dispatch thread for thread safety.
     */
    private static void initGUI() {
        JFrame f = new JFrame("SerialKeyboard");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        SerialKeyboardGUI sk = new SerialKeyboardGUI();
        sk.addComponentstoPane(f.getContentPane());

        f.pack();
        f.setVisible(true);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException |
                IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        // Let special EDT create UI
        javax.swing.SwingUtilities.invokeLater(SerialKeyboardGUI::initGUI);
    }
}