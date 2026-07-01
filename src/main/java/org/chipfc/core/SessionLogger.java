package org.chipfc.core;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Handles session logging to disk.
 * Supports different modes for logging terminal output versus raw UART data.
 */
public class SessionLogger {
    private BufferedWriter writer;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private OperationMode currentMode = OperationMode.TERMINAL;
    private boolean isNewLineUart = true; // Flag for tracking line breaks in UART mode

    // State machine for filtering ANSI escape sequences in Terminal mode
    private enum LogState {
        NORMAL, ESCAPE, CSI
    }

    private LogState logState = LogState.NORMAL;
    private final StringBuilder lineBuffer = new StringBuilder(); // RAM buffer for terminal lines

    public void setMode(OperationMode mode) {
        this.currentMode = mode;
        this.isNewLineUart = true;
        this.logState = LogState.NORMAL;
        this.lineBuffer.setLength(0); // Clear buffer when switching modes
    }

    /**
     * Initializes the log file and opens the writer.
     */
    public void startLogging() throws IOException {
        String fileName = "session_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                + ".log";
        String logDir = ".logs";
        Path filePath = Paths.get(logDir, fileName);

        writer = new BufferedWriter(new FileWriter(filePath.toString(), true));
        isNewLineUart = true;
        logState = LogState.NORMAL;
        lineBuffer.setLength(0);
    }

    /**
     * Logs incoming data based on the current operation mode.
     */
    public void log(byte[] data) {
        if (writer == null)
            return;
        try {
            if (currentMode == OperationMode.TERMINAL) {
                logTerminalMode(data);
            } else {
                logUartMode(data);
            }
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Logs terminal data, filtering out ANSI escape codes.
     */
    private void logTerminalMode(byte[] data) throws IOException {
        String text = new String(data, StandardCharsets.UTF_8);

        for (char c : text.toCharArray()) {
            switch (logState) {
                case NORMAL:
                    if (c == '\u001B') {
                        logState = LogState.ESCAPE; // Found ESC, prepare to read control codes
                    } else if (c == '\b' || c == 0x7F) {
                        if (lineBuffer.length() > 0) {
                            lineBuffer.deleteCharAt(lineBuffer.length() - 1);
                        }
                    } else if (c == '\r') {
                        // Skip carriage return to avoid duplicate line breaks
                    } else if (c == '\n') {
                        writer.write("[" + LocalDateTime.now().format(dtf) + "] " + lineBuffer.toString() + "\n");
                        lineBuffer.setLength(0);
                    } else {
                        lineBuffer.append(c);
                    }
                    break;

                case ESCAPE:
                    if (c == '[') {
                        logState = LogState.CSI; // Enter ANSI control sequence mode
                    } else {
                        logState = LogState.NORMAL; // Not a valid sequence, return to normal
                    }
                    break;

                case CSI:
                    // Wait for sequence termination (e.g., 'm' in [0;32m)
                    if (c >= 0x40 && c <= 0x7E) {
                        logState = LogState.NORMAL;
                    }
                    break;
            }
        }
    }

    /**
     * Logs raw UART data in HEX format.
     */
    private void logUartMode(byte[] data) throws IOException {
        StringBuilder hexLog = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            byte b = data[i];

            if (isNewLineUart) {
                hexLog.append("[").append(LocalDateTime.now().format(dtf)).append("] ");
                isNewLineUart = false;
            }

            hexLog.append(String.format("%02X ", b));

            // Check for line endings (\r, \n)
            if (b == 0x0A || b == 0x0D) {
                boolean isPair = (i + 1 < data.length &&
                        ((b == 0x0D && data[i + 1] == 0x0A) ||
                                (b == 0x0A && data[i + 1] == 0x0D)));
                if (!isPair) {
                    hexLog.append("\r\n");
                    isNewLineUart = true;
                }
            }
        }
        writer.write(hexLog.toString());
    }

    /**
     * Closes the logger and flushes remaining buffer content.
     */
    public void stopLogging() {
        try {
            if (writer != null) {
                // Flush remaining RAM buffer content
                if (currentMode == OperationMode.TERMINAL && lineBuffer.length() > 0) {
                    writer.write("[" + LocalDateTime.now().format(dtf) + "] " + lineBuffer.toString() + "\n");
                    lineBuffer.setLength(0);
                }
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
