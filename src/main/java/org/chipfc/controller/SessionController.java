package org.chipfc.controller;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.chipfc.core.OperationMode;
import org.chipfc.core.SerialDataListener;
import org.chipfc.core.SerialEngine;
import org.chipfc.core.SerialTtyConnector;
import org.chipfc.model.SessionConfig;
import org.chipfc.view.panels.SessionTabPanel;

import lombok.extern.slf4j.Slf4j;

/**
 * Controller class managing the lifecycle of a serial session,
 * including connection handling and data flow between hardware and UI.
 */
@Slf4j
public class SessionController implements SerialDataListener {

    private final SessionTabPanel tabView;
    private final SessionConfig config;

    private final SerialEngine engine;
    private SerialTtyConnector ttyConnector;

    public SessionController(SessionTabPanel tabView, SessionConfig config) {
        this.tabView = tabView;
        this.config = config;
        this.engine = new SerialEngine();
        this.engine.setListener(this);

        initConnection();
        initUartActions();
    }

    /**
     * Initializes the serial connection.
     */
    private void initConnection() {
        if (engine.connect(config.getPortName(), config.getBaudRate())) {
            try {
                ttyConnector = new SerialTtyConnector(engine);
                ttyConnector.setMode("UART".equals(config.getMode()) ? OperationMode.UART : OperationMode.TERMINAL);

                tabView.getTerminalWidget().setTtyConnector(ttyConnector);
                tabView.getTerminalWidget().start();

            } catch (Exception ex) {
                log.error("Failed to initialize JediTerm for port: {}", config.getPortName(), ex);
            }
        } else {
            if (!engine.isOpen()) {
                JOptionPane.showMessageDialog(tabView,
                        "Could not open port: " + config.getPortName(), "Connection Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Configures button actions if in UART mode.
     */
    private void initUartActions() {
        if ("UART".equals(config.getMode()) && tabView.getBtnSend() != null) {
            tabView.getBtnSend().addActionListener(e -> {
                String text = tabView.getTxtUartInput().getText();
                if (!text.isEmpty()) {
                    // Send text as bytes with a newline character
                    engine.sendData((text + "\r\n").getBytes());
                    tabView.getTxtUartInput().setText("");
                }
            });
        }
    }

    /**
     * Cleans up resources when the session is closed.
     */
    public void close() {
        log.info("Closing connection for session: {}", config.getDisplayName());

        // Close TTY connector
        if (ttyConnector != null) {
            ttyConnector.close();
        }

        // Disconnect Serial Engine
        if (engine != null) {
            engine.removeListener();
            engine.disconnect();
        }
    }

    @Override
    public void onDataReceived(byte[] data) {
        if (ttyConnector != null) {
            ttyConnector.feedRawData(data);
        }
    }

    @Override
    public void onConnectionError(String errorMessage) {
        SwingUtilities.invokeLater(() -> {
            if (ttyConnector != null) {
                ttyConnector.close();
            }
            JOptionPane.showMessageDialog(tabView, errorMessage, "Connection Lost", JOptionPane.WARNING_MESSAGE);
        });
    }
}
