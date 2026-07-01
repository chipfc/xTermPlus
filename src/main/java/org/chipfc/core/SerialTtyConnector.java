package org.chipfc.core;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.jediterm.terminal.Questioner;
import com.jediterm.terminal.TtyConnector;

import lombok.extern.slf4j.Slf4j;

/**
 * Bridges the SerialEngine data to the JediTerm UI component via a Piped
 * stream.
 * Handles mode-specific data formatting and keyword highlighting.
 */
@Slf4j
public class SerialTtyConnector implements TtyConnector {

    private final SerialEngine engine;

    // Data piping streams
    private final PipedOutputStream pipeOut;
    private final InputStreamReader pipeInReader;

    private boolean isConnected = false;
    private OperationMode currentMode = OperationMode.TERMINAL;

    // Map for keyword-based syntax highlighting
    private final Map<String, String> keywordColors = new HashMap<>();

    public SerialTtyConnector(SerialEngine engine) throws IOException {
        this.engine = engine;

        // Initialize input and output pipes
        this.pipeOut = new PipedOutputStream();
        PipedInputStream pipeIn = new PipedInputStream(this.pipeOut);

        // Convert byte stream to character stream for JediTerm
        this.pipeInReader = new InputStreamReader(pipeIn, StandardCharsets.UTF_8);

        // Initialize keyword highlighting map with ANSI color codes
        // \033[31m: Red, \033[32m: Green, \033[33m: Yellow, \033[35m: Magenta,
        // \033[36m: Cyan
        keywordColors.put("ERROR", "\033[31m");
        keywordColors.put("FAIL", "\033[31m");
        keywordColors.put("FAILED", "\033[31m");
        keywordColors.put("WARN", "\033[33m");
        keywordColors.put("WARNING", "\033[33m");
        keywordColors.put("SUCCESS", "\033[32m");
        keywordColors.put("OK", "\033[32m");
        keywordColors.put("ROOT", "\033[35m");

        keywordColors.put("CPU", "\033[31m");
        keywordColors.put("MMC", "\033[32m");
        keywordColors.put("HW_VER", "\033[33m");
    }

    public void setMode(OperationMode mode) {
        this.currentMode = mode;
    }

    /**
     * Receives raw data from hardware and pushes it into the pipe.
     */
    public void feedRawData(byte[] data) {
        try {
            if (isConnected) {
                if (currentMode == OperationMode.UART) {
                    // UART Mode: Format as Hex string
                    String hexString = formatBytesToHex(data);
                    pipeOut.write(hexString.getBytes(StandardCharsets.UTF_8));
                } else {
                    // Terminal Mode: Process keyword highlighting
                    String text = new String(data, StandardCharsets.UTF_8);

                    for (Map.Entry<String, String> entry : keywordColors.entrySet()) {
                        String keyword = entry.getKey();
                        String colorCode = entry.getValue();
                        String resetCode = "\033[0m"; // Reset to default theme color

                        // Regex word boundary match
                        text = text.replaceAll("\\b(" + keyword + ")\\b", colorCode + "$1" + resetCode);
                    }

                    pipeOut.write(text.getBytes(StandardCharsets.UTF_8));
                }
                pipeOut.flush();
            }
        } catch (IOException e) {
            log.error("Error pushing data into JediTerm pipe.", e);
        }
    }

    private String formatBytesToHex(byte[] data) {
        StringBuilder sb = new StringBuilder();
        sb.append("\033[33m"); // Set color to Orange

        for (int i = 0; i < data.length; i++) {
            byte b = data[i];

            // Append Hex representation
            sb.append(String.format("%02X ", b));

            // Line break logic for 0x0A or 0x0D
            if (b == 0x0A || b == 0x0D) {
                boolean isPair = (i + 1 < data.length &&
                        ((b == 0x0D && data[i + 1] == 0x0A) ||
                                (b == 0x0A && data[i + 1] == 0x0D)));

                if (!isPair) {
                    sb.append("\r\n");
                }
            }
        }

        sb.append("\033[0m"); // Reset color
        return sb.toString();
    }

    // ====================================================================
    // JEDITERM INTERFACE IMPLEMENTATIONS
    // ====================================================================

    @SuppressWarnings("removal")
    @Override
    public boolean init(Questioner q) {
        isConnected = true;
        return true;
    }

    @Override
    public void close() {
        isConnected = false;
        try {
            pipeOut.close();
            pipeInReader.close();
            engine.disconnect();
        } catch (IOException e) {
            log.error("Error closing JediTerm pipe.", e);
        }
    }

    @Override
    public String getName() {
        return "xTerm+ Serial Connection";
    }

    @Override
    public int read(char[] buf, int offset, int len) throws IOException {
        return pipeInReader.read(buf, offset, len);
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        if (currentMode == OperationMode.TERMINAL) {
            engine.sendData(bytes);
        } else {
            log.info("UART mode: Tx locked.");
        }
    }

    @Override
    public void write(String string) throws IOException {
        if (currentMode == OperationMode.TERMINAL) {
            engine.sendData(string.getBytes(StandardCharsets.UTF_8));
        } else {
            log.info("UART mode: Tx locked.");
        }
    }

    @Override
    public int waitFor() throws InterruptedException {
        while (isConnected) {
            Thread.sleep(100);
        }
        return 0;
    }

    @Override
    public boolean ready() throws IOException {
        return pipeInReader.ready();
    }

    @Override
    public boolean isConnected() {
        return isConnected;
    }
}
