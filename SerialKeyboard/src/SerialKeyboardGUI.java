import com.fazecast.jSerialComm.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
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
    // which is then used in SerialThread to generate keypresses
    private ArrayList<KeyCommand> keyList;

    // Dropdown menus
    JComboBox<SerialPort> portDropdown;

    // Buttons
    private JButton connectButton;
    private JButton saveButton;
    private JButton loadButton;

    // Fields for data entry
    private JFormattedTextField baudrateField;
    private JTextField[] slotField;

    //Other
    private JFileChooser fileChooser;

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

        // Labels to identify fields
        JLabel baudrateLabel = new JLabel("Baudrate:");
        baudrateField = new JFormattedTextField();
        baudrateField.setValue(baudrate);
        baudrateField.setColumns(10);
        baudrateField.addPropertyChangeListener(this);

        // Card for keymappings
        JPanel card2 = new JPanel(new GridBagLayout()); // For keymapping
        GridBagConstraints gbc2 = new GridBagConstraints();

        slotField = new JTextField[TOTAL_SLOTS];
        JLabel[] slotLabel = new JLabel[TOTAL_SLOTS];
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


            // Add key press event for every field, tie it to a KeyEvent in keyList
            final int finalIndex = i;
            slotField[i].addKeyListener(new KeyAdapter() {
                private final int index = finalIndex;
                @Override
                public void keyPressed(KeyEvent e) {
                    setKeyToFieldText(e.getModifiersEx(), e.getKeyCode(), (JTextField) e.getSource());
                    keyList.set(index, new KeyCommand(e)); // update keylist with new KeyEvent
                }
            });
        }

        loadButton = new JButton("Load");
        saveButton = new JButton("Save");


        /* Grid layout configuration below
        * ================================*/

        // Create serial configuration grid
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


        // Create keymap grid
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

        gbc2.gridy++;
        gbc2.gridx = 0;
        card2.add(loadButton, gbc2);
        gbc2.gridx++;
        card2.add(saveButton, gbc2);


        // Note: Maybe add serial monitor as its own tab

        addEventListeners();

        tabs.addTab(SERIAL_PANEL, card1);
        tabs.addTab(KEYMAPPING_PANEL, card2);

        pane.add(tabs);
    }

    /**
     * Adds event listeners to components.
     * Note: addActionListener() calls moved here for better readability
     */
    public void addEventListeners() {
        portDropdown.addActionListener(e -> {
            serialPort = (SerialPort) portDropdown.getSelectedItem();
            if(serialPort != null)
                System.out.printf("Selected serialport %s\n", serialPort.getSystemPortName());
        });

        /* Button toggles between "Connect" and "Disconnect" depending
         * on the state of the selected serial port.
         *
         * A thread that reads and decodes serial input is started if successfully connected.
         */
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
                                serialPort.getSystemPortName()),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
            } else {
                System.out.println("Connected!");
                connectButton.setText(DISCONNECT);
                thread = new SerialDecodeThread(serialPort, keyList);
                thread.start();
                connected = true;
            }
        });


        saveButton.addActionListener(e -> {
            fileChooser = new JFileChooser(new File("."));
            fileChooser.setSelectedFile(new File("save.kmap"));
            fileChooser.setFileFilter(new FileNameExtensionFilter("keymap file", "kmap"));

            if(fileChooser.showSaveDialog(new JFrame()) == JFileChooser.APPROVE_OPTION) {
                File f = fileChooser.getSelectedFile();
                if(saveFile(f)) {
                    System.out.printf("Saved keymap to file %s\n", f);
                } else {
                    System.out.printf("Could not save keymap to file %s\n", f);

                    JOptionPane.showMessageDialog(new JFrame(),
                            String.format("Could not save keymap to file \"%s\"",
                                    f),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
            else {
                System.out.println("No Selection ");
            }
        });

        loadButton.addActionListener(e ->{
            fileChooser = new JFileChooser(new File("."));
            if(fileChooser.showOpenDialog(new JFrame()) == JFileChooser.APPROVE_OPTION) {
                File f = fileChooser.getSelectedFile();

                if(loadFile(f)) {
                    System.out.printf("Loaded keymap from file %s\n", f);
                } else {
                    System.out.printf("Could not load keymap from file %s\n", f);

                    JOptionPane.showMessageDialog(new JFrame(),
                            String.format("Could not load keymap from file \"%s\"",
                                    f),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
            else {
                System.out.println("No Selection ");
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
     * Sets text field with key text including modifers. This method is used when updating
     * fields after a key press or loading a file.
     * Adds modifier string with an "+" to key press if there is one e.g. "Alt + G"
     */
    void setKeyToFieldText(int modifiersEx, int keyCode, JTextField textField) {
        String modifiers = KeyEvent.getModifiersExText(modifiersEx);
        String key = KeyEvent.getKeyText(keyCode);

        String keyPlusModifier = !modifiers.isEmpty() ? modifiers + " + " + key : key;
        textField.setText(keyPlusModifier);
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
     * Writes array of KeyEvent (keyList) to a file
     * @param file to be written to
     * @return true if successful
     */
    boolean saveFile(File file) {
        ObjectOutputStream oos;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(file));
            oos.writeObject(keyList);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Reads file into array KeyEvent array (keyList)
     * @param file to be read from
     * @return true if successful
     */
    boolean loadFile(File file) {
        ObjectInputStream ois;
        try {
            ois = new ObjectInputStream(new FileInputStream(file));

            // Found no good way to ensure type safety
            @SuppressWarnings("unchecked")
            ArrayList<KeyCommand> temp = (ArrayList<KeyCommand>) ois.readObject();
            if(temp == null) {
                return false;
            }

            keyList = temp;

            // Update every keymap field with loaded file
            for(int i = 0; i < TOTAL_SLOTS; i++) {
                KeyCommand cmd = keyList.get(i);
                if(cmd == null)
                    continue;
                setKeyToFieldText(cmd.getModifiersEx(), cmd.getExtendedKeyCode(), slotField[i]);
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


}