package org.chipfc.view.dialogs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.chipfc.model.SessionConfig;

/**
 * A dialog for creating or editing session configurations.
 */
public class SessionDialog extends JDialog {

    private JTextField txtDisplayName;
    private JComboBox<String> cbPortName;
    private JComboBox<Integer> cbBaudRate;
    private JComboBox<String> cbMode;

    private JButton btnSave;
    private JButton btnCancel;

    private boolean isApproved = false;
    private SessionConfig config;

    /**
     * Initializes the dialog.
     * 
     * @param parent         The parent frame to center the dialog on.
     * @param existingConfig The configuration to edit, or null if creating a new
     *                       one.
     */
    public SessionDialog(Frame parent, SessionConfig existingConfig) {
        // Modal = true locks the main application window until the dialog is closed
        super(parent, existingConfig == null ? "New Session" : "Edit Session", true);

        // Initialize object: clone if editing, create new if empty
        if (existingConfig == null) {
            this.config = new SessionConfig();
        } else {
            this.config = new SessionConfig();
            this.config.setId(existingConfig.getId());
            this.config.setDisplayName(existingConfig.getDisplayName());
            this.config.setPortName(existingConfig.getPortName());
            this.config.setBaudRate(existingConfig.getBaudRate());
            this.config.setMode(existingConfig.getMode());
        }

        initComponents();
        loadDataToForm();

        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        formPanel.add(new JLabel("Display Name:"));
        txtDisplayName = new JTextField(15);
        formPanel.add(txtDisplayName);

        formPanel.add(new JLabel("Port Name:"));
        // Editable to allow custom input (e.g., /dev/ttyUSB0)
        cbPortName = new JComboBox<>(new String[] { "COM1", "COM2", "COM3", "COM4", "/dev/ttyUSB0" });
        cbPortName.setEditable(true);
        formPanel.add(cbPortName);

        formPanel.add(new JLabel("Baud Rate:"));
        cbBaudRate = new JComboBox<>(new Integer[] { 9600, 19200, 38400, 57600, 115200, 921600 });
        formPanel.add(cbBaudRate);

        formPanel.add(new JLabel("Mode:"));
        cbMode = new JComboBox<>(new String[] { "TERMINAL", "UART" });
        formPanel.add(cbMode);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnSave = new JButton("Save");
        btnCancel = new JButton("Cancel");

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        // Attach event listeners
        btnSave.addActionListener(e -> {
            saveDataFromForm();
            isApproved = true;
            dispose();
        });

        btnCancel.addActionListener(e -> {
            isApproved = false;
            dispose();
        });

        // Assemble the layout
        setLayout(new BorderLayout());
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    // Populate UI from the configuration object (used when editing)
    private void loadDataToForm() {
        txtDisplayName.setText(config.getDisplayName());
        cbPortName.setSelectedItem(config.getPortName());
        cbBaudRate.setSelectedItem(config.getBaudRate());
        cbMode.setSelectedItem(config.getMode());
    }

    // Capture UI input into the configuration object (used when saving)
    private void saveDataFromForm() {
        config.setDisplayName(txtDisplayName.getText().trim());
        config.setPortName(cbPortName.getSelectedItem().toString().trim());
        config.setBaudRate((Integer) cbBaudRate.getSelectedItem());
        config.setMode(cbMode.getSelectedItem().toString());
    }

    /**
     * Returns the result after the dialog is closed.
     * 
     * @return The session configuration if saved, or null if canceled.
     */
    public SessionConfig getSessionConfig() {
        if (isApproved) {
            return config;
        }
        return null;
    }
}
