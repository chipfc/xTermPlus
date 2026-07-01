package org.chipfc.view.panels;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.chipfc.model.SessionConfig;
import org.chipfc.view.DarkThemeSettings;

import com.jediterm.terminal.ui.JediTermWidget;

/**
 * Panel component representing a single session tab.
 * Includes a terminal widget and optional UART control components.
 */
public class SessionTabPanel extends JPanel {

    private final JediTermWidget terminalWidget;
    private JTextField txtUartInput;
    private JButton btnSend;
    private final SessionConfig config;

    public SessionTabPanel(SessionConfig config) {
        this.config = config;
        setLayout(new BorderLayout());

        // Initialize and add the terminal widget to the center
        terminalWidget = new JediTermWidget(new DarkThemeSettings());
        add(terminalWidget, BorderLayout.CENTER);

        // If in UART mode, append a toolbar for input and sending data
        if ("UART".equals(config.getMode())) {
            JPanel bottomPanel = new JPanel(new BorderLayout(5, 0));
            bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            txtUartInput = new JTextField();
            txtUartInput.setToolTipText("Enter text or HEX string to send to the device...");
            btnSend = new JButton("Send");

            bottomPanel.add(txtUartInput, BorderLayout.CENTER);
            bottomPanel.add(btnSend, BorderLayout.EAST);

            add(bottomPanel, BorderLayout.SOUTH);
        }
    }

    public JediTermWidget getTerminalWidget() {
        return terminalWidget;
    }

    public JTextField getTxtUartInput() {
        return txtUartInput;
    }

    public JButton getBtnSend() {
        return btnSend;
    }

    public SessionConfig getConfig() {
        return config;
    }
}
