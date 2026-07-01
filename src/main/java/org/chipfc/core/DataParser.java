package org.chipfc.core;

import java.awt.Color;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Timer;

/**
 * Parses raw serial data into formatted text or HEX strings,
 * including ANSI color code processing for terminal mode.
 */
public class DataParser {

    private final StringBuilder buffer = new StringBuilder();
    private Color currentColor = null;
    private final BiConsumer<String, Color> onParsed;

    // Current operational mode
    private OperationMode currentMode = OperationMode.TERMINAL;

    // Flush timer to handle incomplete lines (e.g., shell prompts)
    private final Timer flushTimer;

    private static final Pattern ANSI_PATTERN = Pattern.compile("\u001B\\[([0-9;]*)m");

    public DataParser(BiConsumer<String, Color> onParsed) {
        this.onParsed = onParsed;

        // Timer setup: Flushes the buffer if no data is received for 50ms
        flushTimer = new Timer(50, e -> flushBuffer());
        flushTimer.setRepeats(false);
    }

    /**
     * Updates the operational mode and clears the buffer.
     */
    public void setMode(OperationMode mode) {
        this.currentMode = mode;
        this.buffer.setLength(0);
        this.currentColor = null;
    }

    /**
     * Appends raw data to the buffer and triggers parsing or UART formatting.
     */
    public void appendRawData(byte[] data) {
        if (currentMode == OperationMode.UART) {
            // UART Mode: Process as raw Binary/HEX
            String hexString = bytesToHex(data);
            onParsed.accept(hexString, new Color(255, 184, 108));
            return;
        }

        // Terminal Mode: Process as Text and handle ANSI codes
        String text = new String(data);
        buffer.append(text);

        processBuffer();

        // Restart idle timer
        flushTimer.restart();
    }

    private void processBuffer() {
        int newlineIndex;
        while ((newlineIndex = buffer.indexOf("\n")) != -1) {
            String completeLine = buffer.substring(0, newlineIndex + 1);
            buffer.delete(0, newlineIndex + 1);
            completeLine = cleanUnwantedAnsiCodes(completeLine);
            parseAndEmitLine(completeLine);
        }
    }

    /**
     * Forces the buffer to flush when the stream is idle.
     */
    private void flushBuffer() {
        if (buffer.length() > 0) {
            String line = buffer.toString();
            line = cleanUnwantedAnsiCodes(line);
            parseAndEmitLine(line);
            buffer.setLength(0);
        }
    }

    /**
     * Removes non-color ANSI escape codes and control characters.
     */
    private String cleanUnwantedAnsiCodes(String text) {
        // Remove CSI codes that are not color-related
        text = text.replaceAll("\u001B\\[[0-9;?]*[a-ln-zA-Z]", "");

        // Remove standalone ANSI escape sequences
        text = text.replaceAll("\u001B[78c]", "");

        // Clear garbage control characters
        // This Regex line will clear all characters from 0x00 to 0x1F.
        // - Including: Bell character (0x07)
        // - Excluding: \b (0x08), \t (0x09), \n (0x0A), \r (0x0D), ESC (0x1B)
        // text = text.replaceAll("[\\x00-\\x07\\x0B-\\x0C\\x0E-\\x1A\\x1C-\\x1F]", "");

        return text;
    }

    /**
     * Parses a single line for ANSI color codes and emits the styled text.
     */
    private void parseAndEmitLine(String line) {
        Matcher matcher = ANSI_PATTERN.matcher(line);
        int lastEnd = 0;

        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                onParsed.accept(line.substring(lastEnd, matcher.start()), currentColor);
            }
            updateCurrentColor(matcher.group(1));
            lastEnd = matcher.end();
        }

        if (lastEnd < line.length()) {
            onParsed.accept(line.substring(lastEnd), currentColor);
        }
    }

    private void updateCurrentColor(String code) {
        if (code == null || code.isEmpty()) {
            currentColor = null;
            return;
        }

        String[] parts = code.split(";");
        for (String part : parts) {
            if (part.startsWith("0") && part.length() > 1) {
                part = part.substring(1);
            }

            switch (part) {
                case "0":
                    currentColor = null;
                case "1":
                    // Bold
                    break;
                case "30":
                    currentColor = Color.GRAY;
                    break;
                case "31":
                    currentColor = new Color(255, 85, 85);
                    break; // Red
                case "32":
                    currentColor = new Color(80, 250, 123);
                    break; // Green
                case "33":
                    currentColor = new Color(241, 250, 140);
                    break; // Yellow
                case "34":
                    currentColor = new Color(139, 233, 253);
                    break; // Blue
                case "35":
                    currentColor = new Color(255, 121, 198);
                    break; // Magenta
                case "36":
                    currentColor = new Color(139, 233, 253);
                    break; // Cyan
                case "37":
                    currentColor = Color.WHITE;
                    break;

                // Bright colors (90-97)
                case "90":
                    currentColor = Color.LIGHT_GRAY;
                    break;
                case "91":
                    currentColor = new Color(255, 100, 100);
                    break;
                case "92":
                    currentColor = new Color(100, 255, 100);
                    break;
                case "93":
                    currentColor = new Color(255, 255, 100);
                    break;
                case "94":
                    currentColor = new Color(150, 200, 255);
                    break;
                case "95":
                    currentColor = new Color(255, 150, 255);
                    break;
                case "96":
                    currentColor = new Color(150, 255, 255);
                    break;
                case "97":
                    currentColor = Color.WHITE;
                    break;
            }
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString();
    }
}
